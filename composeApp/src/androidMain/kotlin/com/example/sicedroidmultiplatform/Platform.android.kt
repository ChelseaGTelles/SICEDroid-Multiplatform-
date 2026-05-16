package com.example.sicedroidmultiplatform

import android.os.Build
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.sicedroidmultiplatform.data.local.SicenetDatabase

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

// Necesitamos el contexto de Android para Room. 
// Una forma común en KMP es usar un objeto singleton inicializado en el MainActivity o un provider.
// Para mantenerlo simple y directo:
private lateinit var appContext: android.content.Context

fun initAndroidContext(context: android.content.Context) {
    appContext = context
}

actual fun createDatabase(): SicenetDatabase {
    val dbFile = appContext.getDatabasePath("sicenet.db")
    return Room.databaseBuilder<SicenetDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    ).setDriver(BundledSQLiteDriver())
        .build()
}
