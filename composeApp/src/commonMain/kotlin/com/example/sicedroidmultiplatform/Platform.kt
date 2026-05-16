package com.example.sicedroidmultiplatform

import com.example.sicedroidmultiplatform.data.local.SicenetDatabase

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

// Añadimos esto para poder crear la base de datos en cada plataforma
expect fun createDatabase(): SicenetDatabase
