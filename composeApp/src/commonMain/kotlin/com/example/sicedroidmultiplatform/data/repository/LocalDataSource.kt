package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.data.models.*
import com.example.sicedroidmultiplatform.database.SessionEntity

interface LocalDataSource {
    fun saveProfile(profile: AlumnoProfile)
    fun getProfile(): AlumnoProfile?
    fun saveSession(matricula: String, contrasenia: String, tipoUsuario: String)
    fun getSession(): SessionEntity?
    fun saveCarga(items: List<CargaItem>)
    fun getCarga(): List<CargaItem>
    fun saveKardex(items: List<KardexItem>)
    fun getKardex(): List<KardexItem>
    fun saveCalifUnidades(items: List<CalifUnidadItem>)
    fun getCalifUnidades(): List<CalifUnidadItem>
    fun saveCalifFinales(items: List<CalifFinalItem>)
    fun getCalifFinales(): List<CalifFinalItem>
    fun clearAll()
}
