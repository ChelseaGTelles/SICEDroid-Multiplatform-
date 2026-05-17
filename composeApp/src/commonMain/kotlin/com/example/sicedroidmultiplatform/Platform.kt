package com.example.sicedroidmultiplatform

import com.example.sicedroidmultiplatform.database.SicenetDatabase

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun createDatabase(): SicenetDatabase