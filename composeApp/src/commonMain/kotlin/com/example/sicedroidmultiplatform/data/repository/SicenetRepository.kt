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

    private suspend fun ensureSession() {
        if (isWarmedUp) return
        try {
            client.get(serviceUrl)
            isWarmedUp = true
            delay(500)
        } catch (e: Exception) {
            // No propagamos el error aquí, permitimos que el post falle si no hay red
        }
    }

    override suspend fun accesoLogin(request: AccesoLoginRequest): Result<LoginResult> {
        return try {
            ensureSession()

            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <accesoLogin xmlns="http://tempuri.org/">
                      <strMatricula>${request.strMatricula}</strMatricula>
                      <strContrasenia>${request.strContrasenia}</strContrasenia>
                      <tipoUsuario>${request.tipoUsuario}</tipoUsuario>
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
            val isSuccess = resultText.contains("\"acceso\":true") || resultText == "1"

            if (isSuccess) {
                localRepository.saveSession(request.strMatricula, request.strContrasenia, request.tipoUsuario)
                Result.success(LoginResult(acceso = true, mensaje = "Login correcto"))
            } else {
                Result.success(LoginResult(acceso = false, mensaje = "Matrícula o contraseña incorrecta"))
            }
        } catch (e: Exception) {
            val savedSession = localRepository.getSession()
            if (savedSession != null &&
                savedSession.matricula == request.strMatricula &&
                savedSession.contrasenia == request.strContrasenia) {
                Result.success(LoginResult(acceso = true, mensaje = "Modo Offline: Login correcto"))
            } else {
                Result.failure(Exception("No hay conexión y no existen datos guardados.", e))
            }
        }
    }

    override suspend fun getAlumno(): Result<AlumnoProfile> {
        return try {
            ensureSession()
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" />
                  </soap:Body>
                </soap:Envelope>
            """.trimIndent()

            val responseBody: String = client.post(serviceUrl) {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/getAlumnoAcademicoWithLineamiento")
                setBody(soapBody)
            }.body()

            val jsonString = extractTagContent(responseBody, "getAlumnoAcademicoWithLineamientoResult")

            if (jsonString.isNotBlank()) {
                val profile = jsonParser.decodeFromString<AlumnoProfile>(jsonString)
                localRepository.saveProfile(profile)
                Result.success(profile)
            } else {
                Result.failure(Exception("No se pudo obtener la información del perfil."))
            }
        } catch (e: Exception) {
            val cached = localRepository.getProfile()
            if (cached != null) Result.success(cached) else Result.failure(e)
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
            if (jsonString.isNotBlank()) {
                val items = jsonParser.decodeFromString<List<CalifFinalItem>>(jsonString)
                localRepository.saveCalifFinales(items)
                Result.success(items)
            } else {
                Result.success(emptyList())
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
                  <soap:Body>
                    <getCalifUnidadesByAlumno xmlns="http://tempuri.org/" />
                  </soap:Body>
                </soap:Envelope>
            """.trimIndent()

            val response: String = client.post(serviceUrl) {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/getCalifUnidadesByAlumno")
                setBody(soapBody)
            }.body()

            val jsonString = extractTagContent(response, "getCalifUnidadesByAlumnoResult")
            
            if (jsonString.isNotBlank()) {
                val items = jsonParser.decodeFromString<List<CalifUnidadItem>>(jsonString)
                localRepository.saveCalifUnidades(items)
                Result.success(items)
            } else {
                Result.success(emptyList())
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
            
            if (jsonString.isNotBlank()) {
                val responseObj = jsonParser.decodeFromString<KardexResponse>(jsonString)
                localRepository.saveKardex(responseObj.lstKardex)
                Result.success(responseObj.lstKardex)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            val cached = localRepository.getKardex()
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
        }
    }

    override suspend fun getCargaAcademicaByAlumno(): Result<List<CargaItem>> {
        return try {
            ensureSession()
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <getCargaAcademicaByAlumno xmlns="http://tempuri.org/" />
                  </soap:Body>
                </soap:Envelope>
            """.trimIndent()

            val response: String = client.post(serviceUrl) {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/getCargaAcademicaByAlumno")
                setBody(soapBody)
            }.body()

            val jsonString = extractTagContent(response, "getCargaAcademicaByAlumnoResult")
            if (jsonString.isNotBlank()) {
                val items = jsonParser.decodeFromString<List<CargaItem>>(jsonString)
                localRepository.saveCarga(items)
                Result.success(items)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            val cached = localRepository.getCarga()
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
        }
    }

    private fun extractTagContent(xml: String, tagName: String): String {
        var content = xml.substringAfter("<$tagName>", "")
            .substringBefore("</$tagName>", "")

        if (content.isEmpty()) return ""

        if (content.contains("<![CDATA[")) {
            content = content.substringAfter("<![CDATA[", "").substringBefore("]]>", "")
        }

        return content
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }

    override suspend fun logout() {
        isWarmedUp = false
        localRepository.clearAll()
        try {
            val url = Url(baseUrl)
            val cookies = HttpClientFactory.cookieStorage.get(url)
            cookies.forEach { cookie ->
                HttpClientFactory.cookieStorage.addCookie(
                    url,
                    cookie.copy(expires = GMTDate(0))
                )
            }
        } catch (e: Exception) {}
    }
}
