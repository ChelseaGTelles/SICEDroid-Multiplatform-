package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.data.AccesoLoginRequest
import com.example.sicedroidmultiplatform.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.cookies.* // Importante para la sesión
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class SicenetRepository : InterfaceRepository {

    private val client = HttpClient {
        install(HttpCookies)
    }

    override fun logout() {
        // luego aquí limpias sesión/cookies
    }

    override suspend fun accesoLogin(
        request: AccesoLoginRequest
    ): Result<LoginResult> {

        return try {

            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                    
                    <soap:Body>
                    
                        <accesoLogin xmlns="http://tempuri.org/">
                        
                            <strMatricula>${request.strMatricula}</strMatricula>
                            
                            <strContrasenia>${request.strContrasenia}</strContrasenia>
                            
                            <tipoUsuario>${request.tipoUsuario}</tipoUsuario>
                            
                        </accesoLogin>
                        
                    </soap:Body>
                    
                </soap:Envelope>
            """.trimIndent()

            val response: String =
                client.post(
                    "https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx"
                ) {

                    contentType(
                        ContentType.Text.Xml
                    )

                    header(
                        "SOAPAction",
                        "http://tempuri.org/accesoLogin"
                    )

                    setBody(soapBody)
                }.body()

            val success =
                response.contains("\"acceso\":true") ||
                        response.contains(">1<")

            Result.success(
                LoginResult(
                    acceso = success,
                    mensaje = if (success)
                        "Login correcto"
                    else
                        "Credenciales incorrectas",
                    rawResponse = response
                )
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun getAlumno():
            Result<AlumnoProfile> {

        return Result.failure(
            Exception("Aún no implementado")
        )
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