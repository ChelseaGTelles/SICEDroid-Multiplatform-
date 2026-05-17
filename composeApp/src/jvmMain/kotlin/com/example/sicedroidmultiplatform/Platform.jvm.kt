package com.example.sicedroidmultiplatform

import com.example.sicedroidmultiplatform.data.local.database.DatabaseDriverFactory
import com.example.sicedroidmultiplatform.database.SicenetDatabase

class JVMPlatform : Platform {
    override val name: String =
        "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun createDatabase(): SicenetDatabase {

    val driverFactory = DatabaseDriverFactory()

    return SicenetDatabase(
        driverFactory.createDriver()
    )
}