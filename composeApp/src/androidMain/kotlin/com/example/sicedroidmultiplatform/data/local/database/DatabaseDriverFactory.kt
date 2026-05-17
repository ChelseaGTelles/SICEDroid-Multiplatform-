package com.example.sicedroidmultiplatform.data.local.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.sicedroidmultiplatform.database.SicenetDatabase

actual class DatabaseDriverFactory actual constructor(
    private val context: Any?
) {

    actual fun createDriver(): SqlDriver {

        return AndroidSqliteDriver(
            SicenetDatabase.Schema,
            context as Context,
            "sicenet.db"
        )
    }
}