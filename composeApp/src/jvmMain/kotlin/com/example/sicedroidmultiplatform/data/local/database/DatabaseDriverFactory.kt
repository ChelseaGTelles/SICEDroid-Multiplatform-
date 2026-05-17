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

        val driver: SqlDriver =
            JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")

        SicenetDatabase.Schema.create(driver)

        return driver
    }
}