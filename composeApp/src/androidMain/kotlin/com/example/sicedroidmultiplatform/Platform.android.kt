package com.example.sicedroidmultiplatform

import android.content.Context
import com.example.sicedroidmultiplatform.database.SicenetDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

private lateinit var appContext: Context

fun initAndroidContext(context: Context) {
    appContext = context
}

actual fun createDatabase(): SicenetDatabase {
    val driver = AndroidSqliteDriver(
        schema = SicenetDatabase.Schema,
        context = appContext,
        name = "sicenet.db"
    )

    return SicenetDatabase(driver)
}