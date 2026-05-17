package com.example.sicedroidmultiplatform

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.sicedroidmultiplatform.database.SicenetDatabase

class JVMPlatform : Platform {
    override val name: String =
        "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun createDatabase(): SicenetDatabase {

    val driver = JdbcSqliteDriver("jdbc:sqlite:sicenet.db")

    return SicenetDatabase(driver)
}