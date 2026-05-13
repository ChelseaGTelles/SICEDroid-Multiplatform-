package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.data.AccesoLoginRequest
import com.example.sicedroidmultiplatform.data.models.*
interface InterfaceRepository {
    suspend fun accesoLogin(request: AccesoLoginRequest): Result<LoginResult>
    suspend fun getAlumno(): Result<AlumnoProfile>
    suspend fun getAllCalifFinalByAlumnos(modEducativo: Int): Result<List<CalifFinalItem>>
    suspend fun getCalifUnidadesByAlumno(): Result<List<CalifUnidadItem>>
    suspend fun getAllKardexConPromedioByAlumno(aluLineamiento: Int): Result<List<KardexItem>>
    suspend fun getCargaAcademicaByAlumno(): Result<List<CargaItem>>
    fun logout()
}