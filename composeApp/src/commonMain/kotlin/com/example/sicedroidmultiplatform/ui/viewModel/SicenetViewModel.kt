package com.example.sicedroidmultiplatform.ui.viewModel

import com.example.sicedroidmultiplatform.data.models.AlumnoProfile
import com.example.sicedroidmultiplatform.data.models.CalifFinalItem
import com.example.sicedroidmultiplatform.data.models.CalifUnidadItem
import com.example.sicedroidmultiplatform.data.models.CargaItem
import com.example.sicedroidmultiplatform.data.models.KardexItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SicenetUiState {

    object Idle : SicenetUiState()

    object Loading : SicenetUiState()
    object Success : SicenetUiState()

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
class SicenetViewModel {

    private val _uiState =
        MutableStateFlow<SicenetUiState>(
            SicenetUiState.Idle
        )

    val uiState = _uiState.asStateFlow()

    fun login(
        matricula: String,
        contrasenia: String,
        userType: String
    ) {

        if (
            matricula.isNotEmpty() &&
            contrasenia.isNotEmpty()
        ) {
            _uiState.value =
                SicenetUiState.Success
        } else {
            _uiState.value =
                SicenetUiState.Error(
                    "Campos vacíos"
                )
        }
    }

    fun getProfile() {

        _uiState.value =
            SicenetUiState.ProfileLoaded(
                com.example.sicedroidmultiplatform.data.models.AlumnoProfile(
                    matricula = "22100001",
                    nombre = "Andrea",
                    carrera = "Ingeniería en Sistemas",
                    especialidad = "Desarrollo Web",
                    semActual = "6",
                    cdtosAcumulados = "210",
                    cdtosActuales = "36",
                    fechaReins = "2026-05-13",
                    adeudo = false,
                    inscrito = true
                )
            )
    }

    fun getCarga() {}

    fun getKardex(lineamiento: Int) {}

    fun getCalifUnidades() {}

    fun getCalifFinales(modEducativo: Int) {}

    fun logout() {
        _uiState.value =
            SicenetUiState.Idle
    }
}