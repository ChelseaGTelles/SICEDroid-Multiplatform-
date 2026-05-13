package com.example.sicedroidmultiplatform.data

object SoapRequests {

    fun loginRequest(
        matricula: String,
        password: String,
        tipoUsuario: String
    ): String {

        return """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">

                <soap:Body>

                    <accesoLogin xmlns="http://tempuri.org/">

                        <strMatricula>$matricula</strMatricula>

                        <strContrasenia>$password</strContrasenia>

                        <tipoUsuario>$tipoUsuario</tipoUsuario>

                    </accesoLogin>

                </soap:Body>

            </soap:Envelope>
        """.trimIndent()
    }
}