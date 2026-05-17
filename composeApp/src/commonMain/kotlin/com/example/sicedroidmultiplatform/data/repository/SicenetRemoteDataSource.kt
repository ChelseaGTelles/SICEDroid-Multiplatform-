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

class SicenetRemoteDataSource : RemoteDataSource {
    private val client = HttpClientFactory.client
    private var isWarmedUp = false
    private val serviceUrl = "https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx"

    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    private suspend fun ensureConnection() {
        if (isWarmedUp) return
        try {
            client.get(serviceUrl).body<String>()
            isWarmedUp = true
        } catch (e: Exception) {
            throw mapNetworkException(e)
        }
    }

    override suspend fun accesoLogin(request: AccesoLoginRequest): Result<LoginResult> {
        return try {
            ensureConnection()
            delay(100)
            var success = performInternalLogin(request.strMatricula, request.strContrasenia, request.tipoUsuario)
            
            if (!success) {
                isWarmedUp = false
                ensureConnection()
                delay(250)
                success = performInternalLogin(request.strMatricula, request.strContrasenia, request.tipoUsuario)
            }

            if (success) {
                isWarmedUp = true
                Result.success(LoginResult(acceso = true, mensaje = "Login correcto"))
            } else {
                isWarmedUp = false
                Result.success(LoginResult(acceso = false, mensaje = "Credenciales incorrectas"))
            }
        } catch (e: Exception) { Result.failure(mapNetworkException(e)) }
    }

    private suspend fun performInternalLogin(matricula: String, contrasenia: String, tipoUsuario: String): Boolean {
        return try {
            val soapBody = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><accesoLogin xmlns="http://tempuri.org/"><strMatricula>$matricula</strMatricula><strContrasenia>$contrasenia</strContrasenia><tipoUsuario>$tipoUsuario</tipoUsuario></accesoLogin></soap:Body></soap:Envelope>"""
            val response: String = client.post(serviceUrl) {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/accesoLogin")
                setBody(soapBody)
            }.body()
            val resultText = extractTagContent(response, "accesoLoginResult")
            resultText.contains("\"acceso\":true") || resultText == "1"
        } catch (e: Exception) { false }
    }

    override suspend fun getAlumno(): Result<AlumnoProfile> {
        return try {
            ensureConnection()
            val response: String = client.post(serviceUrl) {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/getAlumnoAcademicoWithLineamiento")
                setBody("""<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" /></soap:Body></soap:Envelope>""")
            }.body()

            val json = extractTagContent(response, "getAlumnoAcademicoWithLineamientoResult")
            if (json.isNotBlank() && json != "[]") {
                Result.success(jsonParser.decodeFromString<AlumnoProfile>(json))
            } else {
                isWarmedUp = false
                Result.failure(Exception("SESSION_EXPIRED"))
            }
        } catch (e: Exception) { Result.failure(mapNetworkException(e)) }
    }

    override suspend fun getCargaAcademicaByAlumno(): Result<List<CargaItem>> = executeQuery("getCargaAcademicaByAlumno")
    override suspend fun getCalifUnidadesByAlumno(): Result<List<CalifUnidadItem>> = executeQuery("getCalifUnidadesByAlumno")

    override suspend fun getAllCalifFinalByAlumnos(modEducativo: Int): Result<List<CalifFinalItem>> {
        val body = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><getAllCalifFinalByAlumnos xmlns="http://tempuri.org/"><modEducativo>$modEducativo</modEducativo></getAllCalifFinalByAlumnos></soap:Body></soap:Envelope>"""
        return executeQuery("getAllCalifFinalByAlumnos", body)
    }

    override suspend fun getAllKardexConPromedioByAlumno(aluLineamiento: Int): Result<List<KardexItem>> {
        val body = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><getAllKardexConPromedioByAlumno xmlns="http://tempuri.org/"><aluLineamiento>$aluLineamiento</aluLineamiento></getAllKardexConPromedioByAlumno></soap:Body></soap:Envelope>"""
        return try {
            ensureConnection()
            val response: String = client.post(serviceUrl) {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/getAllKardexConPromedioByAlumno")
                setBody(body)
            }.body()
            val json = extractTagContent(response, "getAllKardexConPromedioByAlumnoResult")
            if (json == "[]") return Result.success(emptyList())
            if (json.isNotBlank()) Result.success(jsonParser.decodeFromString<KardexResponse>(json).lstKardex)
            else { isWarmedUp = false; Result.failure(Exception("SESSION_EXPIRED")) }
        } catch (e: Exception) { Result.failure(mapNetworkException(e)) }
    }

    private suspend inline fun <reified T> executeQuery(method: String, customBody: String? = null): Result<List<T>> {
        return try {
            ensureConnection()
            val soapBody = customBody ?: """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><$method xmlns="http://tempuri.org/" /></soap:Body></soap:Envelope>"""
            val response: String = client.post(serviceUrl) {
                header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                header("SOAPAction", "http://tempuri.org/$method")
                setBody(soapBody)
            }.body()
            val json = extractTagContent(response, "${method}Result")
            if (json == "[]") Result.success(emptyList())
            else if (json.isNotBlank()) Result.success(jsonParser.decodeFromString<List<T>>(json))
            else { isWarmedUp = false; Result.failure(Exception("SESSION_EXPIRED")) }
        } catch (e: Exception) { Result.failure(mapNetworkException(e)) }
    }

    private fun mapNetworkException(e: Exception): Exception {
        val msg = (e.message ?: e.toString()).lowercase()
        val connectionKeywords = listOf(
            "resolve", "connect", "timeout", "network", "host", 
            "internet", "unable", "acc", "refused", "reachable", "failed",
            "no address", "address", "connection", "http"
        )
        
        return if (connectionKeywords.any { msg.contains(it) }) {
            Exception("Sin conexión")
        } else {
            e
        }
    }

    private fun extractTagContent(xml: String, tagName: String): String {
        var content = xml.substringAfter("<$tagName>", "").substringBefore("</$tagName>", "")
        if (content.contains("<![CDATA[")) content = content.substringAfter("<![CDATA[", "").substringBefore("]]>", "")
        return content.replace("&quot;", "\"").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").trim()
    }

    override suspend fun logout() {
        isWarmedUp = false
        try {
            val url = Url("https://sicenet.surguanajuato.tecnm.mx")
            HttpClientFactory.cookieStorage.get(url).forEach { cookie ->
                HttpClientFactory.cookieStorage.addCookie(url, cookie.copy(expires = GMTDate(0)))
            }
        } catch (e: Exception) { }
    }
}
