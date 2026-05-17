package com.example.sicedroidmultiplatform.ui.viewModel

import com.example.sicedroidmultiplatform.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.sicedroidmultiplatform.data.repository.InterfaceRepository
import com.example.sicedroidmultiplatform.data.repository.LocalRepository
import com.example.sicedroidmultiplatform.data.AccesoLoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

sealed class SicenetUiState {
    object Idle : SicenetUiState()
    object Loading : SicenetUiState()
    data class Success(val loginResult: LoginResult) : SicenetUiState()
    data class Error(val message: String) : SicenetUiState()
    data class ProfileLoaded(
        val profile: AlumnoProfile,
        val lastUpdated: String? = null,
        val fromCache: Boolean = false
    ) : SicenetUiState()
    data class CargaLoaded(
        val items: List<CargaItem>,
        val lastUpdated: String? = null,
        val fromCache: Boolean = false
    ) : SicenetUiState()
    data class KardexLoaded(
        val items: List<KardexItem>,
        val lastUpdated: String? = null,
        val fromCache: Boolean = false
    ) : SicenetUiState()
    data class UnidadesLoaded(
        val items: List<CalifUnidadItem>,
        val lastUpdated: String? = null,
        val fromCache: Boolean = false
    ) : SicenetUiState()
    data class FinalesLoaded(
        val items: List<CalifFinalItem>,
        val lastUpdated: String? = null,
        val fromCache: Boolean = false
    ) : SicenetUiState()
}

class SicenetViewModel(
    private val repository: InterfaceRepository,
    private val localRepository: LocalRepository
) {
    private val _uiState = MutableStateFlow<SicenetUiState>(SicenetUiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun login(matricula: String, contrasenia: String, tipoUsuario: String) {
        if (matricula.isBlank() || contrasenia.isBlank()) {
            _uiState.value = SicenetUiState.Error("Matrícula y contraseña son requeridas")
            return
        }

        scope.launch {
            _uiState.value = SicenetUiState.Loading
            try {
                val result = repository.accesoLogin(
                    AccesoLoginRequest(matricula, contrasenia, tipoUsuario)
                )
                
                result.onSuccess { login ->
                    if (login.acceso) {
                        _uiState.value = SicenetUiState.Success(login)
                    } else {
                        _uiState.value = SicenetUiState.Error(login.mensaje ?: "Credenciales incorrectas")
                    }
                }.onFailure { e ->
                    _uiState.value = SicenetUiState.Error("Error: ${e.message}")
                }
            } catch (e: Exception) {
                _uiState.value = SicenetUiState.Error("Excepción: ${e.message}")
            }
        }
    }

    fun logout() {
        scope.launch {
            repository.logout()
            _uiState.value = SicenetUiState.Idle
        }
    }

    fun getProfile() {
        scope.launch {
            // Intentar cargar desde caché inmediatamente
            val cached = localRepository.getProfile()
            if (cached != null) {
                _uiState.value = SicenetUiState.ProfileLoaded(
                    profile = cached,
                    lastUpdated = localRepository.getProfileLastUpdated(),
                    fromCache = true
                )
            } else {
                _uiState.value = SicenetUiState.Loading
            }

            // Petición a red en segundo plano
            val result = repository.getAlumno()

            result.onSuccess { profile ->
                localRepository.saveProfile(profile)
                _uiState.value = SicenetUiState.ProfileLoaded(
                    profile = profile,
                    lastUpdated = localRepository.getProfileLastUpdated(),
                    fromCache = false
                )
            }.onFailure { error ->
                // Si ya tenemos datos de caché, no mostramos error, solo dejamos lo que hay.
                // Si no hay nada, entonces sí mostramos error.
                if (localRepository.getProfile() == null) {
                    _uiState.value = SicenetUiState.Error("Sin conexión: ${error.message}")
                }
            }
        }
    }

    fun getCarga() {
        scope.launch {
            val cached = localRepository.getCarga()
            if (cached.isNotEmpty()) {
                _uiState.value = SicenetUiState.CargaLoaded(
                    items = cached,
                    lastUpdated = localRepository.getCargaLastUpdated(),
                    fromCache = true
                )
            } else {
                _uiState.value = SicenetUiState.Loading
            }

            val result = repository.getCargaAcademicaByAlumno()
            
            result.onSuccess { items ->
                _uiState.value = SicenetUiState.CargaLoaded(
                    items = items,
                    lastUpdated = localRepository.getCargaLastUpdated(),
                    fromCache = false
                )
            }.onFailure { error ->
                if (localRepository.getCarga().isEmpty()) {
                    _uiState.value = SicenetUiState.Error("Sin conexión: ${error.message}")
                }
            }
        }
    }

    fun getKardex(lineamiento: Int) {
        scope.launch {
            val cached = localRepository.getKardex()
            if (cached.isNotEmpty()) {
                _uiState.value = SicenetUiState.KardexLoaded(
                    items = cached,
                    lastUpdated = localRepository.getKardexLastUpdated(),
                    fromCache = true
                )
            } else {
                _uiState.value = SicenetUiState.Loading
            }

            val result = repository.getAllKardexConPromedioByAlumno(lineamiento)
            
            result.onSuccess { items ->
                _uiState.value = SicenetUiState.KardexLoaded(
                    items = items,
                    lastUpdated = localRepository.getKardexLastUpdated(),
                    fromCache = false
                )
            }.onFailure { error ->
                if (localRepository.getKardex().isEmpty()) {
                    _uiState.value = SicenetUiState.Error("Sin conexión: ${error.message}")
                }
            }
        }
    }

    fun getCalifUnidades() {
        scope.launch {
            val cached = localRepository.getCalifUnidades()
            if (cached.isNotEmpty()) {
                _uiState.value = SicenetUiState.UnidadesLoaded(
                    items = cached,
                    lastUpdated = localRepository.getCalifUnidadesLastUpdated(),
                    fromCache = true
                )
            } else {
                _uiState.value = SicenetUiState.Loading
            }

            val result = repository.getCalifUnidadesByAlumno()
            
            result.onSuccess { items ->
                _uiState.value = SicenetUiState.UnidadesLoaded(
                    items = items,
                    lastUpdated = localRepository.getCalifUnidadesLastUpdated(),
                    fromCache = false
                )
            }.onFailure { error ->
                if (localRepository.getCalifUnidades().isEmpty()) {
                    _uiState.value = SicenetUiState.Error("Sin conexión: ${error.message}")
                }
            }
        }
    }

    fun getCalifFinales(modEducativo: Int) {
        scope.launch {
            val cached = localRepository.getCalifFinales()
            if (cached.isNotEmpty()) {
                _uiState.value = SicenetUiState.FinalesLoaded(
                    items = cached,
                    lastUpdated = localRepository.getCalifFinalesLastUpdated(),
                    fromCache = true
                )
            } else {
                _uiState.value = SicenetUiState.Loading
            }

            val result = repository.getAllCalifFinalByAlumnos(modEducativo)
            
            result.onSuccess { items ->
                _uiState.value = SicenetUiState.FinalesLoaded(
                    items = items,
                    lastUpdated = localRepository.getCalifFinalesLastUpdated(),
                    fromCache = false
                )
            }.onFailure { error ->
                if (localRepository.getCalifFinales().isEmpty()) {
                    _uiState.value = SicenetUiState.Error("Sin conexión: ${error.message}")
                }
            }
        }
    }
}
