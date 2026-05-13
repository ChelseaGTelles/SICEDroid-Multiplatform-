package com.example.sicedroidmultiplatform.ui.viewModel

import com.example.sicedroidmultiplatform.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.sicedroidmultiplatform.data.repository.InterfaceRepository
import com.example.sicedroidmultiplatform.data.repository.SicenetRepository
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
    private val repository: InterfaceRepository = SicenetRepository()
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
                    _uiState.value = SicenetUiState.Error("Error de red: ${e.message}")
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
            _uiState.value = SicenetUiState.Loading
            try {
                val result = repository.getAlumno()
                result.onSuccess { profile ->
                    _uiState.value = SicenetUiState.ProfileLoaded(profile)
                }.onFailure { error ->
                    _uiState.value = SicenetUiState.Error(error.message ?: "Error al cargar perfil")
                }
            } catch (e: Exception) {
                _uiState.value = SicenetUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun getCarga() {
        scope.launch {
            _uiState.value = SicenetUiState.Loading
            try {
                val result = repository.getCargaAcademicaByAlumno()
                result.onSuccess { items ->
                    _uiState.value = SicenetUiState.CargaLoaded(items)
                }.onFailure { error ->
                    _uiState.value = SicenetUiState.Error(error.message ?: "Error al cargar carga académica")
                }
            } catch (e: Exception) {
                _uiState.value = SicenetUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun getKardex(lineamiento: Int) {
        scope.launch {
            _uiState.value = SicenetUiState.Loading
            try {
                val result = repository.getAllKardexConPromedioByAlumno(lineamiento)
                result.onSuccess { items ->
                    _uiState.value = SicenetUiState.KardexLoaded(items)
                }.onFailure { error ->
                    _uiState.value = SicenetUiState.Error(error.message ?: "Error al cargar kardex")
                }
            } catch (e: Exception) {
                _uiState.value = SicenetUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun getCalifUnidades() {
        scope.launch {
            _uiState.value = SicenetUiState.Loading
            try {
                val result = repository.getCalifUnidadesByAlumno()
                result.onSuccess { items ->
                    _uiState.value = SicenetUiState.UnidadesLoaded(items)
                }.onFailure { error ->
                    _uiState.value = SicenetUiState.Error(error.message ?: "Error al cargar calificaciones por unidad")
                }
            } catch (e: Exception) {
                _uiState.value = SicenetUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun getCalifFinales(modEducativo: Int) {
        scope.launch {
            _uiState.value = SicenetUiState.Loading
            try {
                val result = repository.getAllCalifFinalByAlumnos(modEducativo)
                result.onSuccess { items ->
                    _uiState.value = SicenetUiState.FinalesLoaded(items)
                }.onFailure { error ->
                    _uiState.value = SicenetUiState.Error(error.message ?: "Error al cargar calificaciones finales")
                }
            } catch (e: Exception) {
                _uiState.value = SicenetUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }
}
