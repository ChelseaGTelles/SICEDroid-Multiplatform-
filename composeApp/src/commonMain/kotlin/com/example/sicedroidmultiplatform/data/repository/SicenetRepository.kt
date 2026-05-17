package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.data.AccesoLoginRequest
import com.example.sicedroidmultiplatform.data.models.*
import com.example.sicedroidmultiplatform.database.SessionEntity

class SicenetRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : InterfaceRepository {

    override fun getSession(): SessionEntity? = localDataSource.getSession()

    /**
     * Si el servidor responde vacío busca tus credenciales guardadas, inicia sesión automáticamente y reintenta.
     */
    private suspend fun <T> safeRemoteCall(call: suspend () -> Result<T>): Result<T> {
        val result = call()
        
        if (result.isFailure && result.exceptionOrNull()?.message == "SESSION_EXPIRED") {
            val session = localDataSource.getSession()
            if (session != null) {
                // Re-autenticación
                val loginResult = remoteDataSource.accesoLogin(
                    AccesoLoginRequest(session.matricula, session.contrasenia, session.tipoUsuario)
                )

                if (loginResult.isSuccess && loginResult.getOrNull()?.acceso == true) {
                    return call()
                }
            }
        }
        return result
    }

    override suspend fun accesoLogin(request: AccesoLoginRequest): Result<LoginResult> {
        val result = remoteDataSource.accesoLogin(request)
        result.onSuccess {
            if (it.acceso) {
                localDataSource.saveSession(request.strMatricula, request.strContrasenia, request.tipoUsuario)
            }
        }.onFailure {
            val saved = localDataSource.getSession()
            if (saved != null && saved.matricula == request.strMatricula && saved.contrasenia == request.strContrasenia) {
                return Result.success(LoginResult(true, "Modo Offline"))
            }
        }
        return result
    }

    override suspend fun logout() {
        remoteDataSource.logout()
        localDataSource.clearAll()
    }

    override fun getCachedProfile() = localDataSource.getProfile()
    override fun getProfileLastUpdated() = "Local"
    override suspend fun fetchProfile(): Result<AlumnoProfile> {
        return safeRemoteCall { remoteDataSource.getAlumno() }
            .onSuccess { localDataSource.saveProfile(it) }
    }

    override fun getCachedCarga() = localDataSource.getCarga()
    override fun getCargaLastUpdated() = "Local"
    override suspend fun fetchCarga(): Result<List<CargaItem>> {
        return safeRemoteCall { remoteDataSource.getCargaAcademicaByAlumno() }
            .onSuccess { if (it.isNotEmpty()) localDataSource.saveCarga(it) }
    }

    override fun getCachedKardex() = localDataSource.getKardex()
    override fun getKardexLastUpdated() = "Local"
    override suspend fun fetchKardex(lineamiento: Int): Result<List<KardexItem>> {
        return safeRemoteCall { remoteDataSource.getAllKardexConPromedioByAlumno(lineamiento) }
            .onSuccess { if (it.isNotEmpty()) localDataSource.saveKardex(it) }
    }

    override fun getCachedUnidades() = localDataSource.getCalifUnidades()
    override fun getUnidadesLastUpdated() = "Local"
    override suspend fun fetchUnidades(): Result<List<CalifUnidadItem>> {
        return safeRemoteCall { remoteDataSource.getCalifUnidadesByAlumno() }
            .onSuccess { if (it.isNotEmpty()) localDataSource.saveCalifUnidades(it) }
    }

    override fun getCachedFinales() = localDataSource.getCalifFinales()
    override fun getFinalesLastUpdated() = "Local"
    override suspend fun fetchFinales(modEducativo: Int): Result<List<CalifFinalItem>> {
        return safeRemoteCall { remoteDataSource.getAllCalifFinalByAlumnos(modEducativo) }
            .onSuccess { if (it.isNotEmpty()) localDataSource.saveCalifFinales(it) }
    }
}
