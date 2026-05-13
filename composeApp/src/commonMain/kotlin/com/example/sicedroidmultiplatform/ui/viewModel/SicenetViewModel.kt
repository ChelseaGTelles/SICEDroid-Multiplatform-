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

    data class Success(
        val loginResult: LoginResult
    ) : SicenetUiState()

    data class Error(
        val message: String
    ) : SicenetUiState()

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

    private val _uiState =
        MutableStateFlow<SicenetUiState>(
            SicenetUiState.Idle
        )

    val uiState = _uiState.asStateFlow()
    private val scope = CoroutineScope(
        Dispatchers.Main + SupervisorJob()
    )

    fun login(
        matricula: String,
        contrasenia: String,
        tipoUsuario: String
    ) {

        scope.launch {

            _uiState.value = SicenetUiState.Loading

            try {

                val result = repository.accesoLogin(
                    AccesoLoginRequest(
                        matricula,
                        contrasenia,
                        tipoUsuario
                    )
                )

                result.onSuccess { login ->

                    if (login.acceso) {

                        _uiState.value =
                            SicenetUiState.Success(login)

                    } else {

                        _uiState.value =
                            SicenetUiState.Error(
                                login.mensaje
                            )
                    }
                }

                result.onFailure {

                    _uiState.value =
                        SicenetUiState.Error(
                            it.message ?: "Error desconocido"
                        )
                }

            } catch (e: Exception) {

                _uiState.value =
                    SicenetUiState.Error(
                        e.message ?: "Error"
                    )
            }
        }
    }

    fun logout() {
        _uiState.value = SicenetUiState.Idle
    }

    fun getProfile() {

        val profile = AlumnoProfile(
            matricula = "22100000",
            nombre = "Alumno Demo",
            carrera = "Ingeniería",
            especialidad = "Sistemas",
            semActual = "6",
            cdtosAcumulados = "180",
            cdtosActuales = "32",
            fechaReins = "10/05/2026",
            adeudo = false,
            inscrito = true
        )

        _uiState.value =
            SicenetUiState.ProfileLoaded(profile)
    }

    fun getCarga() {

        val items = listOf(
            CargaItem(
                Materia = "Programación",
                Grupo = "A",
                Docente = "Profesor Demo",
                CreditosMateria = 5
            )
        )

        _uiState.value =
            SicenetUiState.CargaLoaded(items)
    }

    fun getKardex(lineamiento: Int) {

        val items = listOf(
            KardexItem(
                clvOficial = "SCC-101",
                materia = "Estructuras",
                periodo = "3",
                promedio = "95"
            )
        )

        _uiState.value =
            SicenetUiState.KardexLoaded(items)
    }

    fun getCalifUnidades() {

        val items = listOf(
            CalifUnidadItem(
                Materia = "Matemáticas",
                Grupo = "A",
                unidades = mapOf(
                    1 to "90",
                    2 to "85",
                    3 to "100"
                )
            )
        )

        _uiState.value =
            SicenetUiState.UnidadesLoaded(items)
    }

    fun getCalifFinales(modEducativo: Int) {

        val items = listOf(
            CalifFinalItem(
                materia = "Programación Móvil",
                calif = "95",
                acred = "Sí",
                grupo = "A"
            )
        )

        _uiState.value =
            SicenetUiState.FinalesLoaded(items)
    }
}