package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.data.AccesoLoginRequest
import com.example.sicedroidmultiplatform.data.models.*
import com.example.sicedroidmultiplatform.data.network.HttpClientFactory
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.date.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.delay

class SicenetRepository : InterfaceRepository {
    private val client = HttpClientFactory.client
    private var isWarmedUp = false

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private suspend fun ensureSession() {
        if (isWarmedUp) return
        try {
            client.get("https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx")
            isWarmedUp = true
            delay(500)
        } catch (e: Exception) {}
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

            val response: String = client.post("https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx") {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/accesoLogin")
                setBody(soapBody)
            }.body()

            val resultText = extractTagContent(response, "accesoLoginResult")
            val isSuccess = resultText.contains("\"acceso\":true") || resultText == "1"

            if (isSuccess) {
                Result.success(LoginResult(acceso = true, mensaje = "Login correcto"))
            } else {
                Result.success(LoginResult(acceso = false, mensaje = "Matrícula o contraseña incorrecta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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

            val responseBody: String = client.post("https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx") {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/getAlumnoAcademicoWithLineamiento")
                setBody(soapBody)
            }.body()

            val jsonString = extractTagContent(responseBody, "getAlumnoAcademicoWithLineamientoResult")

            if (jsonString.isNotBlank()) {
                val profile = jsonParser.decodeFromString<AlumnoProfile>(jsonString)
                Result.success(profile)
            } else {
                Result.failure(Exception("No se pudo obtener la información del perfil."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extrae el contenido de una etiqueta XML, manejando opcionalmente CDATA y decodificando entidades HTML.
     */
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

        try {
            val url = Url("https://sicenet.surguanajuato.tecnm.mx")
            val cookies = HttpClientFactory.cookieStorage.get(url)

            cookies.forEach { cookie ->
                HttpClientFactory.cookieStorage.addCookie(
                    url,
                    cookie.copy(expires = GMTDate(0))
                )
            }
        } catch (e: Exception) {
            // Ignorar errores
        }
    }

    override suspend fun getAllCalifFinalByAlumnos(
        modEducativo: Int
    ): Result<List<CalifFinalItem>> {

        return Result.failure(
            Exception("Aún no implementado")
        )
    }

    override suspend fun getCalifUnidadesByAlumno():
            Result<List<CalifUnidadItem>> {

        return Result.failure(
            Exception("Aún no implementado")
        )
    }

    override suspend fun getAllKardexConPromedioByAlumno(
        aluLineamiento: Int
    ): Result<List<KardexItem>> {

        return Result.failure(
            Exception("Aún no implementado")
        )
    }

    override suspend fun getCargaAcademicaByAlumno():
            Result<List<CargaItem>> {

        return Result.failure(
            Exception("Aún no implementado")
        )
    }
}
