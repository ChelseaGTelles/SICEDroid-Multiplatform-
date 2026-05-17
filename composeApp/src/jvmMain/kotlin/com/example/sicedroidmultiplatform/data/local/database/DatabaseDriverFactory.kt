package com.example.sicedroidmultiplatform.data.local.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.sicedroidmultiplatform.database.SicenetDatabase
import java.io.File

actual class DatabaseDriverFactory actual constructor(
    context: Any?
) {

    actual fun createDriver(): SqlDriver {

        val databasePath =
            File(System.getProperty("user.home"), "sicenet.db")

        val driver =
            JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")

        driver.execute(
            null,
            "PRAGMA foreign_keys = ON;",
            0
        )

        try {
            SicenetDatabase.Schema.create(driver)
        } catch (_: Exception) {
            // La BD ya existe
        }

        return driver
    }
}