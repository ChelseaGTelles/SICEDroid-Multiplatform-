package com.example.sicedroidmultiplatform.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

object HttpClientFactory {

    val client = HttpClient(CIO)
}