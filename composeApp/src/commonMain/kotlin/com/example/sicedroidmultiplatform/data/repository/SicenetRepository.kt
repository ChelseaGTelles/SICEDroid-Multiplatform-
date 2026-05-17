package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.getPlatform
import com.example.sicedroidmultiplatform.data.AccesoLoginRequest
import com.example.sicedroidmultiplatform.data.models.*
import com.example.sicedroidmultiplatform.data.network.HttpClientFactory
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.date.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.delay

class SicenetRepository(
    private val localRepository: LocalRepository
) : InterfaceRepository {
    private val client = HttpClientFactory.client
    private var isWarmedUp = false

    private val baseUrl = if (getPlatform().name.contains("Web")) "/api-sicenet" else "https://sicenet.surguanajuato.tecnm.mx"
    private val serviceUrl = "$baseUrl/ws/wsalumnos.asmx"

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private suspend fun performInternalLogin(matricula: String, contrasenia: String, tipoUsuario: String): Boolean {
        return try {
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <accesoLogin xmlns="http://tempuri.org/">
                      <strMatricula>$matricula</strMatricula>
                      <strContrasenia>$contrasenia</strContrasenia>
                      <tipoUsuario>$tipoUsuario</tipoUsuario>
                    </accesoLogin>
                  </soap:Body>
                </soap:Envelope>
            """.trimIndent()

            val response: String = client.post(serviceUrl) {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/accesoLogin")
                setBody(soapBody)
            }.body()

            val resultText = extractTagContent(response, "accesoLoginResult")
            resultText.contains("\"acceso\":true") || resultText == "1"
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun ensureSession() {
        if (isWarmedUp) return
        try {
            // Aseguramos cookies base consumiendo la respuesta inicial
            client.get(serviceUrl).body<String>()
            
            val session = localRepository.getSession()
            if (session != null) {
                // Si ya tenemos sesión guardada, logueamos para calentar.
                // Solo marcamos como warmedUp si el login silencioso tiene éxito.
                if (performInternalLogin(session.matricula, session.contrasenia, session.tipoUsuario)) {
                    isWarmedUp = true
                }
            } else {
                // Si es un login manual, el GET es suficiente para preparar el cliente
                isWarmedUp = true
            }
        } catch (e: Exception) { }
    }

    override suspend fun accesoLogin(request: AccesoLoginRequest): Result<LoginResult> {
        return try {
            // 1. Preparamos el terreno (Cookies)
            ensureSession()
            delay(200)

            // 2. Intentamos el login
            var success = performInternalLogin(request.strMatricula, request.strContrasenia, request.tipoUsuario)
            
            // 3. Si falla (típico falso negativo tras logout), forzamos un refresh de sesión y reintentamos.
            // Esto elimina la necesidad de dar "dos clics" manualmente.
            if (!success) {
                isWarmedUp = false
                ensureSession()
                delay(400) // Damos un poco más de margen al servidor
                success = performInternalLogin(request.strMatricula, request.strContrasenia, request.tipoUsuario)
            }

            if (success) {
                isWarmedUp = true
                localRepository.saveSession(request.strMatricula, request.strContrasenia, request.tipoUsuario)
                Result.success(LoginResult(acceso = true, mensaje = "Login correcto"))
            } else {
                Result.success(LoginResult(acceso = false, mensaje = "Matrícula o contraseña incorrecta"))
            }
        } catch (e: Exception) {
            val savedSession = localRepository.getSession()
            if (savedSession != null && savedSession.matricula == request.strMatricula) {
                Result.success(LoginResult(acceso = true, mensaje = "Modo Offline"))
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getAlumno(): Result<AlumnoProfile> {
        return try {
            ensureSession()
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body><getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" /></soap:Body>
                </soap:Envelope>
            """.trimIndent()

            val responseBody: String = client.post(serviceUrl) {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/getAlumnoAcademicoWithLineamiento")
                setBody(soapBody)
            }.body()

            val jsonString = extractTagContent(responseBody, "getAlumnoAcademicoWithLineamientoResult")
            if (jsonString.isNotBlank() && jsonString != "[]") {
                val profile = jsonParser.decodeFromString<AlumnoProfile>(jsonString)
                localRepository.saveProfile(profile)
                Result.success(profile)
            } else {
                if (isWarmedUp) {
                    isWarmedUp = false
                    ensureSession()
                    return getAlumno()
                }
                throw Exception("Error de sesión")
            }
        } catch (e: Exception) {
            localRepository.getProfile()?.let { Result.success(it) } ?: Result.failure(e)
        }
    }

    override suspend fun getCargaAcademicaByAlumno(): Result<List<CargaItem>> {
        return try {
            ensureSession()
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body><getCargaAcademicaByAlumno xmlns="http://tempuri.org/" /></soap:Body>
                </soap:Envelope>
            """.trimIndent()

            val response: String = client.post(serviceUrl) {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/getCargaAcademicaByAlumno")
                setBody(soapBody)
            }.body()

            val jsonString = extractTagContent(response, "getCargaAcademicaByAlumnoResult")
            if (jsonString.isNotBlank() && jsonString != "[]") {
                val items = jsonParser.decodeFromString<List<CargaItem>>(jsonString)
                localRepository.saveCarga(items)
                Result.success(items)
            } else if (jsonString == "[]") {
                localRepository.saveCarga(emptyList())
                Result.success(emptyList())
            } else {
                if (isWarmedUp) {
                    isWarmedUp = false
                    ensureSession()
                    return getCargaAcademicaByAlumno()
                }
                throw Exception("Respuesta incompleta")
            }
        } catch (e: Exception) {
            val cached = localRepository.getCarga()
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
        }
    }

    override suspend fun getAllCalifFinalByAlumnos(modEducativo: Int): Result<List<CalifFinalItem>> {
        return try {
            ensureSession()
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <getAllCalifFinalByAlumnos xmlns="http://tempuri.org/">
                      <modEducativo>$modEducativo</modEducativo>
                    </getAllCalifFinalByAlumnos>
                  </soap:Body>
                </soap:Envelope>
            """.trimIndent()

            val response: String = client.post(serviceUrl) {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/getAllCalifFinalByAlumnos")
                setBody(soapBody)
            }.body()

            val jsonString = extractTagContent(response, "getAllCalifFinalByAlumnosResult")
            if (jsonString.isNotBlank() && jsonString != "[]") {
                val items = jsonParser.decodeFromString<List<CalifFinalItem>>(jsonString)
                localRepository.saveCalifFinales(items)
                Result.success(items)
            } else if (jsonString == "[]") {
                localRepository.saveCalifFinales(emptyList())
                Result.success(emptyList())
            } else {
                if (isWarmedUp) {
                    isWarmedUp = false
                    ensureSession()
                    return getAllCalifFinalByAlumnos(modEducativo)
                }
                throw Exception("Error de sesión")
            }
        } catch (e: Exception) {
            val cached = localRepository.getCalifFinales()
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
        }
    }

    override suspend fun getCalifUnidadesByAlumno(): Result<List<CalifUnidadItem>> {
        return try {
            ensureSession()
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body><getCalifUnidadesByAlumno xmlns="http://tempuri.org/" /></soap:Body>
                </soap:Envelope>
            """.trimIndent()

            val response: String = client.post(serviceUrl) {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/getCalifUnidadesByAlumno")
                setBody(soapBody)
            }.body()

            val jsonString = extractTagContent(response, "getCalifUnidadesByAlumnoResult")
            if (jsonString.isNotBlank() && jsonString != "[]") {
                val items = jsonParser.decodeFromString<List<CalifUnidadItem>>(jsonString)
                localRepository.saveCalifUnidades(items)
                Result.success(items)
            } else if (jsonString == "[]") {
                localRepository.saveCalifUnidades(emptyList())
                Result.success(emptyList())
            } else {
                if (isWarmedUp) {
                    isWarmedUp = false
                    ensureSession()
                    return getCalifUnidadesByAlumno()
                }
                throw Exception("Error de sesión")
            }
        } catch (e: Exception) {
            val cached = localRepository.getCalifUnidades()
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
        }
    }

    override suspend fun getAllKardexConPromedioByAlumno(aluLineamiento: Int): Result<List<KardexItem>> {
        return try {
            ensureSession()
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <getAllKardexConPromedioByAlumno xmlns="http://tempuri.org/">
                      <aluLineamiento>$aluLineamiento</aluLineamiento>
                    </getAllKardexConPromedioByAlumno>
                  </soap:Body>
                </soap:Envelope>
            """.trimIndent()

            val response: String = client.post(serviceUrl) {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/getAllKardexConPromedioByAlumno")
                setBody(soapBody)
            }.body()

            val jsonString = extractTagContent(response, "getAllKardexConPromedioByAlumnoResult")
            if (jsonString.isNotBlank() && jsonString != "[]") {
                val responseObj = jsonParser.decodeFromString<KardexResponse>(jsonString)
                localRepository.saveKardex(responseObj.lstKardex)
                Result.success(responseObj.lstKardex)
            } else if (jsonString == "[]") {
                localRepository.saveKardex(emptyList())
                Result.success(emptyList())
            } else {
                if (isWarmedUp) {
                    isWarmedUp = false
                    ensureSession()
                    return getAllKardexConPromedioByAlumno(aluLineamiento)
                }
                throw Exception("Error de sesión")
            }
        } catch (e: Exception) {
            val cached = localRepository.getKardex()
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
        }
    }

    private fun extractTagContent(xml: String, tagName: String): String {
        var content = xml.substringAfter("<$tagName>", "").substringBefore("</$tagName>", "")
        if (content.isEmpty()) return ""
        if (content.contains("<![CDATA[")) content = content.substringAfter("<![CDATA[", "").substringBefore("]]>", "")
        return content.replace("&quot;", "\"").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").trim()
    }

    override suspend fun logout() {
        isWarmedUp = false
        localRepository.clearAll()
        try {
            val url = Url(baseUrl)
            HttpClientFactory.cookieStorage.get(url).forEach { cookie ->
                HttpClientFactory.cookieStorage.addCookie(url, cookie.copy(expires = GMTDate(0)))
            }
        } catch (e: Exception) { }
    }
}
