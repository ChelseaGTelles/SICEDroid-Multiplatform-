package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.data.models.AccesoLoginRequest
import com.example.sicedroidmultiplatform.data.models.*
import com.example.sicedroidmultiplatform.database.SessionEntity

interface InterfaceRepository {
    fun getSession(): SessionEntity?
    suspend fun accesoLogin(request: AccesoLoginRequest): Result<LoginResult>
    suspend fun logout()

    fun getCachedProfile(): AlumnoProfile?
    fun getProfileLastUpdated(): String
    suspend fun fetchProfile(): Result<AlumnoProfile>

    fun getCachedCarga(): List<CargaItem>
    fun getCargaLastUpdated(): String
    suspend fun fetchCarga(): Result<List<CargaItem>>

    fun getCachedKardex(): List<KardexItem>
    fun getKardexLastUpdated(): String
    suspend fun fetchKardex(lineamiento: Int): Result<List<KardexItem>>

    fun getCachedUnidades(): List<CalifUnidadItem>
    fun getUnidadesLastUpdated(): String
    suspend fun fetchUnidades(): Result<List<CalifUnidadItem>>

    fun getCachedFinales(): List<CalifFinalItem>
    fun getFinalesLastUpdated(): String
    suspend fun fetchFinales(modEducativo: Int): Result<List<CalifFinalItem>>

}
