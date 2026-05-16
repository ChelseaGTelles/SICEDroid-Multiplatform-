package com.example.sicedroidmultiplatform

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.sicedroidmultiplatform.data.local.SicenetDatabase
import java.io.File

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun createDatabase(): SicenetDatabase {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "sicenet.db")
    return Room.databaseBuilder<SicenetDatabase>(
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver())
        .build()
}
