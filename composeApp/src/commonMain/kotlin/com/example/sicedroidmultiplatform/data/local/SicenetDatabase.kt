package com.example.sicedroidmultiplatform.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sicedroidmultiplatform.data.local.dao.*
import com.example.sicedroidmultiplatform.data.local.entities.*

@Database(
    entities = [
        ProfileEntity::class,
        SicenetDataEntity::class,
        SessionEntity::class,
        CargaEntity::class,
        KardexEntity::class,
        CalifUnidadesEntity::class,
        CalifFinalesEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SicenetDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun sicenetDataDao(): SicenetDataDao
    abstract fun sessionDao(): SessionDao
    abstract fun cargaDao(): CargaDao
    abstract fun kardexDao(): KardexDao
    abstract fun califUnidadesDao(): CalifUnidadesDao
    abstract fun califFinalesDao(): CalifFinalesDao
}
