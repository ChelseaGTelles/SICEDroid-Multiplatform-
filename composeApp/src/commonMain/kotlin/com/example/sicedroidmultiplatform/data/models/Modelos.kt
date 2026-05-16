package com.example.sicedroidmultiplatform.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LoginResult(
    val acceso: Boolean,
    val mensaje: String,
    val rawResponse: String? = null
)

@Serializable
data class AlumnoProfile(
    val matricula: String = "",
    val nombre: String = "",
    val carrera: String = "",
    val situacion: String = "",
    val fechaReins: String = "",
    val modEducativo: String = "",
    val adeudo: Boolean = false,
    val urlFoto: String = "",
    val adeudoDescripcion: String = "",
    val inscrito: Boolean = false,
    val estatus: String = "",
    val semActual: String = "",
    val cdtosAcumulados: String = "",
    val cdtosActuales: String = "",
    val especialidad: String = "",
    val lineamiento: String = "",
    val rawJson: String = ""
)

@Serializable
data class CargaItem(
    val Materia: String = "",
    val Grupo: String = "",
    val Docente: String = "",
    val CreditosMateria: Int = 0,
    val Lunes: String = "",
    val Martes: String = "",
    val Miercoles: String = "",
    val Jueves: String = "",
    val Viernes: String = "",
)

@Serializable
data class KardexResponse(
    val lstKardex: List<KardexItem> = emptyList()
)

@Serializable
data class KardexItem(
    @SerialName("ClvOfiMat") val clvOficial: String = "",
    @SerialName("Materia") val materia: String = "",
    @SerialName("S1") val periodo: String = "",
    @SerialName("Calif") val promedio: String = ""
)

@Serializable
data class CalifUnidadItem(
    @SerialName("Materia") val Materia: String = "",
    @SerialName("Grupo") val Grupo: String = "",
    @SerialName("C1") val C1: String? = null,
    @SerialName("C2") val C2: String? = null,
    @SerialName("C3") val C3: String? = null,
    @SerialName("C4") val C4: String? = null,
    @SerialName("C5") val C5: String? = null,
    @SerialName("C6") val C6: String? = null,
    @SerialName("C7") val C7: String? = null,
    @SerialName("C8") val C8: String? = null,
    @SerialName("C9") val C9: String? = null,
    @SerialName("C10") val C10: String? = null,
    @SerialName("C11") val C11: String? = null,
    @SerialName("C12") val C12: String? = null,
    @SerialName("C13") val C13: String? = null
) {
    fun getUnidadesMap(): Map<Int, String> {
        val rawMap = mapOf(
            1 to C1, 2 to C2, 3 to C3, 4 to C4, 5 to C5,
            6 to C6, 7 to C7, 8 to C8, 9 to C9, 10 to C10,
            11 to C11, 12 to C12, 13 to C13
        )
        return rawMap.entries
            .filter { it.value != null && it.value != "null" && it.value!!.trim().isNotEmpty() }
            .associate { it.key to it.value!! }
    }
}

@Serializable
data class CalifFinalItem(
    val materia: String = "",
    val calif: String = "",
    val acred: String = "",
    val grupo: String = "",
    val Observaciones: String = ""
)
