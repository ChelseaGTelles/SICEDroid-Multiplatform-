package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.data.local.SicenetDatabase
import com.example.sicedroidmultiplatform.data.local.entities.*
import com.example.sicedroidmultiplatform.data.models.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class LocalRepository(private val database: SicenetDatabase) {

    private val json = Json { ignoreUnknownKeys = true }

    private fun getCurrentTime(): Long = Clock.System.now().toEpochMilliseconds()

    private fun formatTimestamp(timestamp: Long?): String? {
        if (timestamp == null) return null
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} ${dateTime.hour}:${dateTime.minute}"
    }

    suspend fun saveProfile(profile: AlumnoProfile) {
        database.profileDao().insertProfile(ProfileEntity(
            matricula = profile.matricula,
            nombre = profile.nombre,
            carrera = profile.carrera,
            situacion = profile.situacion,
            fechaReins = profile.fechaReins,
            modEducativo = profile.modEducativo,
            adeudo = profile.adeudo,
            urlFoto = profile.urlFoto,
            adeudoDescripcion = profile.adeudoDescripcion,
            inscrito = profile.inscrito,
            estatus = profile.estatus,
            semActual = profile.semActual,
            cdtosAcumulados = profile.cdtosAcumulados,
            cdtosActuales = profile.cdtosActuales,
            especialidad = profile.especialidad,
            lineamiento = profile.lineamiento,
            lastUpdated = getCurrentTime()
        ))
    }

    suspend fun getProfile(): AlumnoProfile? {
        return database.profileDao().getProfile()?.let {
            AlumnoProfile(
                matricula = it.matricula,
                nombre = it.nombre,
                carrera = it.carrera,
                situacion = it.situacion,
                fechaReins = it.fechaReins,
                modEducativo = it.modEducativo,
                adeudo = it.adeudo,
                urlFoto = it.urlFoto,
                adeudoDescripcion = it.adeudoDescripcion,
                inscrito = it.inscrito,
                estatus = it.estatus,
                semActual = it.semActual,
                cdtosAcumulados = it.cdtosAcumulados,
                cdtosActuales = it.cdtosActuales,
                especialidad = it.especialidad,
                lineamiento = it.lineamiento
            )
        }
    }

    suspend fun getProfileLastUpdated(): String? = formatTimestamp(database.profileDao().getProfile()?.lastUpdated)

    suspend fun saveCarga(items: List<CargaItem>) {
        val currentTime = getCurrentTime()
        database.cargaDao().clearCarga()
        database.cargaDao().insertAll(items.map {
            CargaEntity(
                materia = it.Materia,
                grupo = it.Grupo,
                docente = it.Docente,
                creditosMateria = it.CreditosMateria,
                lunes = it.Lunes,
                martes = it.Martes,
                miercoles = it.Miercoles,
                jueves = it.Jueves,
                viernes = it.Viernes,
                lastUpdated = currentTime
            )
        })
    }

    suspend fun getCarga(): List<CargaItem> {
        return database.cargaDao().getAllCarga().map {
            CargaItem(
                Materia = it.materia,
                Grupo = it.grupo,
                Docente = it.docente,
                CreditosMateria = it.creditosMateria,
                Lunes = it.lunes,
                Martes = it.martes,
                Miercoles = it.miercoles,
                Jueves = it.jueves,
                Viernes = it.viernes
            )
        }
    }

    suspend fun getCargaLastUpdated(): String? = formatTimestamp(database.cargaDao().getLastUpdated())

    suspend fun saveKardex(items: List<KardexItem>) {
        val currentTime = getCurrentTime()
        database.kardexDao().clearKardex()
        database.kardexDao().insertAll(items.map {
            KardexEntity(
                clvOficial = it.clvOficial,
                materia = it.materia,
                periodo = it.periodo,
                promedio = it.promedio,
                lastUpdated = currentTime
            )
        })
    }

    suspend fun getKardex(): List<KardexItem> {
        return database.kardexDao().getAllKardex().map {
            KardexItem(
                clvOficial = it.clvOficial,
                materia = it.materia,
                periodo = it.periodo,
                promedio = it.promedio
            )
        }
    }

    suspend fun getKardexLastUpdated(): String? = formatTimestamp(database.kardexDao().getLastUpdated())

    suspend fun saveCalifUnidades(items: List<CalifUnidadItem>) {
        val currentTime = getCurrentTime()
        database.califUnidadesDao().clearCalifUnidades()
        database.califUnidadesDao().insertAll(items.map {
            CalifUnidadesEntity(
                materia = it.Materia,
                grupo = it.Grupo,
                unidades = json.encodeToString(it.getUnidadesMap()),
                lastUpdated = currentTime
            )
        })
    }

    suspend fun getCalifUnidades(): List<CalifUnidadItem> {
        return database.califUnidadesDao().getAllCalifUnidades().map { entity ->
            val unitsMap = try {
                json.decodeFromString<Map<Int, String>>(entity.unidades)
            } catch (e: Exception) {
                emptyMap()
            }
            CalifUnidadItem(
                Materia = entity.materia,
                Grupo = entity.grupo,
                C1 = unitsMap[1], C2 = unitsMap[2], C3 = unitsMap[3], C4 = unitsMap[4],
                C5 = unitsMap[5], C6 = unitsMap[6], C7 = unitsMap[7], C8 = unitsMap[8],
                C9 = unitsMap[9], C10 = unitsMap[10], C11 = unitsMap[11], C12 = unitsMap[12],
                C13 = unitsMap[13]
            )
        }
    }

    suspend fun getCalifUnidadesLastUpdated(): String? = formatTimestamp(database.califUnidadesDao().getLastUpdated())

    suspend fun saveCalifFinales(items: List<CalifFinalItem>) {
        val currentTime = getCurrentTime()
        database.califFinalesDao().clearCalifFinales()
        database.califFinalesDao().insertAll(items.map {
            CalifFinalesEntity(
                materia = it.materia,
                calif = it.calif,
                acred = it.acred,
                grupo = it.grupo,
                observaciones = it.Observaciones,
                lastUpdated = currentTime
            )
        })
    }

    suspend fun getCalifFinales(): List<CalifFinalItem> {
        return database.califFinalesDao().getAllCalifFinales().map {
            CalifFinalItem(
                materia = it.materia,
                calif = it.calif,
                acred = it.acred,
                grupo = it.grupo,
                Observaciones = it.observaciones
            )
        }
    }

    suspend fun getCalifFinalesLastUpdated(): String? = formatTimestamp(database.califFinalesDao().getLastUpdated())

    suspend fun saveSession(matricula: String, contrasenia: String, tipo: String) {
        database.sessionDao().clearSession()
        database.sessionDao().insertSession(SessionEntity(matricula = matricula, contrasenia = contrasenia, tipoUsuario = tipo))
    }

    suspend fun getSession(): SessionEntity? = database.sessionDao().getSession()

    suspend fun clearAll() {
        database.profileDao().clearProfile()
        database.cargaDao().clearCarga()
        database.kardexDao().clearKardex()
        database.califUnidadesDao().clearCalifUnidades()
        database.califFinalesDao().clearCalifFinales()
        database.sessionDao().clearSession()
    }
}
