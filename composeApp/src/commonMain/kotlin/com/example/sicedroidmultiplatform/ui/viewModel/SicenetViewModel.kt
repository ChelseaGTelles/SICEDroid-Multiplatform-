package com.example.sicedroidmultiplatform.ui.viewModel

import com.example.sicedroidmultiplatform.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.sicedroidmultiplatform.data.repository.InterfaceRepository
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
    private val repository: InterfaceRepository
) {
    private val _uiState = MutableStateFlow<SicenetUiState>(SicenetUiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun checkExistingSession(): Boolean {
        val session = repository.getSession()
        return if (session != null) {
            _uiState.value = SicenetUiState.Success(LoginResult(acceso = true, mensaje = "Sesión recuperada"))
            true
        } else {
            false
        }
    }

    fun login(matricula: String, contrasenia: String, tipoUsuario: String) {
        if (matricula.isBlank() || contrasenia.isBlank()) {
            _uiState.value = SicenetUiState.Error("Matrícula y contraseña son requeridas")
            return
        }

        scope.launch {
            _uiState.value = SicenetUiState.Loading
            repository.accesoLogin(AccesoLoginRequest(matricula, contrasenia, tipoUsuario))
                .onSuccess { login ->
                    if (login.acceso) {
                        _uiState.value = SicenetUiState.Success(login)
                    } else {
                        _uiState.value = SicenetUiState.Error(login.mensaje ?: "Credenciales incorrectas")
                    }
                }.onFailure { e ->
                    _uiState.value = SicenetUiState.Error(e.message ?: "Error desconocido")
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
            // Caché primero
            val cached = repository.getCachedProfile()
            if (cached != null) {
                _uiState.value = SicenetUiState.ProfileLoaded(
                    profile = cached,
                    lastUpdated = repository.getProfileLastUpdated(),
                    fromCache = true
                )
            } else {
                _uiState.value = SicenetUiState.Loading
            }

            // Red después
            repository.fetchProfile()
                .onSuccess { profile ->
                    _uiState.value = SicenetUiState.ProfileLoaded(
                        profile = profile,
                        lastUpdated = "Ahora",
                        fromCache = false
                    )
                }.onFailure { error ->
                    if (repository.getCachedProfile() == null) {
                        _uiState.value = SicenetUiState.Error(error.message ?: "Error de red")
                    }
                }
        }
    }

    fun getCarga() {
        scope.launch {
            val cached = repository.getCachedCarga()
            if (cached.isNotEmpty()) {
                _uiState.value = SicenetUiState.CargaLoaded(
                    items = cached,
                    lastUpdated = repository.getCargaLastUpdated(),
                    fromCache = true
                )
            } else {
                _uiState.value = SicenetUiState.Loading
            }

            repository.fetchCarga()
                .onSuccess { items ->
                    _uiState.value = SicenetUiState.CargaLoaded(
                        items = items,
                        lastUpdated = "Ahora",
                        fromCache = false
                    )
                }.onFailure { error ->
                    if (repository.getCachedCarga().isEmpty()) {
                        _uiState.value = SicenetUiState.Error(error.message ?: "Error de red")
                    }
                }
        }
    }

    fun getKardex(lineamiento: Int) {
        scope.launch {
            val cached = repository.getCachedKardex()
            if (cached.isNotEmpty()) {
                _uiState.value = SicenetUiState.KardexLoaded(
                    items = cached,
                    lastUpdated = repository.getKardexLastUpdated(),
                    fromCache = true
                )
            } else {
                _uiState.value = SicenetUiState.Loading
            }

            repository.fetchKardex(lineamiento)
                .onSuccess { items ->
                    _uiState.value = SicenetUiState.KardexLoaded(
                        items = items,
                        lastUpdated = "Ahora",
                        fromCache = false
                    )
                }.onFailure { error ->
                    if (repository.getCachedKardex().isEmpty()) {
                        _uiState.value = SicenetUiState.Error(error.message ?: "Error de red")
                    }
                }
        }
    }

    fun getCalifUnidades() {
        scope.launch {
            val cached = repository.getCachedUnidades()
            if (cached.isNotEmpty()) {
                _uiState.value = SicenetUiState.UnidadesLoaded(
                    items = cached,
                    lastUpdated = repository.getUnidadesLastUpdated(),
                    fromCache = true
                )
            } else {
                _uiState.value = SicenetUiState.Loading
            }

            repository.fetchUnidades()
                .onSuccess { items ->
                    _uiState.value = SicenetUiState.UnidadesLoaded(
                        items = items,
                        lastUpdated = "Ahora",
                        fromCache = false
                    )
                }.onFailure { error ->
                    if (repository.getCachedUnidades().isEmpty()) {
                        _uiState.value = SicenetUiState.Error(error.message ?: "Error de red")
                    }
                }
        }
    }

    fun getCalifFinales(modEducativo: Int) {
        scope.launch {
            val cached = repository.getCachedFinales()
            if (cached.isNotEmpty()) {
                _uiState.value = SicenetUiState.FinalesLoaded(
                    items = cached,
                    lastUpdated = repository.getFinalesLastUpdated(),
                    fromCache = true
                )
            } else {
                _uiState.value = SicenetUiState.Loading
            }

            repository.fetchFinales(modEducativo)
                .onSuccess { items ->
                    _uiState.value = SicenetUiState.FinalesLoaded(
                        items = items,
                        lastUpdated = "Ahora",
                        fromCache = false
                    )
                }.onFailure { error ->
                    if (repository.getCachedFinales().isEmpty()) {
                        _uiState.value = SicenetUiState.Error(error.message ?: "Error de red")
                    }
                }
        }
    }
}
