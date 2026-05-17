package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.database.SicenetDatabase
import com.example.sicedroidmultiplatform.data.models.*

class LocalRepository(
    private val database: SicenetDatabase
) {

    private val queries = database.sicenetDatabaseQueries


    fun saveProfile(profile: AlumnoProfile) {
        queries.insertProfile(
            matricula = profile.matricula,
            nombre = profile.nombre,
            carrera = profile.carrera,
            situacion = profile.situacion,
            fechaReins = profile.fechaReins,
            modEducativo = profile.modEducativo,
            adeudo = if (profile.adeudo) 1L else 0L,
            urlFoto = profile.urlFoto,
            adeudoDescripcion = profile.adeudoDescripcion,
            inscrito = if (profile.inscrito) 1L else 0L,
            estatus = profile.estatus,
            semActual = profile.semActual,
            cdtosAcumulados = profile.cdtosAcumulados,
            cdtosActuales = profile.cdtosActuales,
            especialidad = profile.especialidad,
            lineamiento = profile.lineamiento,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun getProfile(): AlumnoProfile? {
        return queries.selectProfile()
            .executeAsOneOrNull()
            ?.let {
                AlumnoProfile(
                    matricula = it.matricula,
                    nombre = it.nombre,
                    carrera = it.carrera,
                    situacion = it.situacion,
                    fechaReins = it.fechaReins,
                    modEducativo = it.modEducativo,
                    adeudo = it.adeudo == 1L,
                    urlFoto = it.urlFoto,
                    adeudoDescripcion = it.adeudoDescripcion,
                    inscrito = it.inscrito == 1L,
                    estatus = it.estatus,
                    semActual = it.semActual,
                    cdtosAcumulados = it.cdtosAcumulados,
                    cdtosActuales = it.cdtosActuales,
                    especialidad = it.especialidad,
                    lineamiento = it.lineamiento
                )
            }
    }

    fun getProfileLastUpdated(): String {
        return "Local"
    }


    fun saveSession(
        matricula: String,
        contrasenia: String,
        tipoUsuario: String
    ) {
        queries.insertSession(
            matricula,
            contrasenia,
            tipoUsuario
        )
    }

    fun getSession() =
        queries.getSession().executeAsOneOrNull()


    fun saveCarga(items: List<CargaItem>) {

        queries.clearCarga()

        items.forEach {
            queries.insertCarga(
                materia = it.Materia,
                grupo = it.Grupo,
                docente = it.Docente,
                creditosMateria = it.CreditosMateria.toLong(),
                lunes = it.Lunes,
                martes = it.Martes,
                miercoles = it.Miercoles,
                jueves = it.Jueves,
                viernes = it.Viernes
            )
        }
    }

    fun getCarga(): List<CargaItem> {

        return queries.selectAllCarga()
            .executeAsList()
            .map {

                CargaItem(
                    Materia = it.materia,
                    Grupo = it.grupo,
                    Docente = it.docente,
                    CreditosMateria = it.creditosMateria.toInt(),
                    Lunes = it.lunes,
                    Martes = it.martes,
                    Miercoles = it.miercoles,
                    Jueves = it.jueves,
                    Viernes = it.viernes
                )
            }
    }

    fun getCargaLastUpdated(): String {
        return "Local"
    }


    fun saveKardex(items: List<KardexItem>) {

        queries.clearKardex()

        items.forEach {
            queries.insertKardex(
                clvOficial = it.clvOficial,
                materia = it.materia,
                periodo = it.periodo,
                promedio = it.promedio
            )
        }
    }

    fun getKardex(): List<KardexItem> {

        return queries.selectAllKardex()
            .executeAsList()
            .map {

                KardexItem(
                    clvOficial = it.clvOficial,
                    materia = it.materia,
                    periodo = it.periodo,
                    promedio = it.promedio
                )
            }
    }

    fun getKardexLastUpdated(): String {
        return "Local"
    }
   fun saveCalifUnidades(items: List<CalifUnidadItem>) {

        queries.clearCalifUnidades()

        items.forEach {
            queries.insertCalifUnidad(
                materia = it.Materia,
                grupo = it.Grupo,
                c1 = it.C1,
                c2 = it.C2,
                c3 = it.C3,
                c4 = it.C4,
                c5 = it.C5,
                c6 = it.C6,
                c7 = it.C7,
                c8 = it.C8,
                c9 = it.C9,
                c10 = it.C10,
                c11 = it.C11,
                c12 = it.C12,
                c13 = it.C13
            )
        }
    }

    fun getCalifUnidades(): List<CalifUnidadItem> {

        return queries.selectAllCalifUnidades()
            .executeAsList()
            .map {

                CalifUnidadItem(
                    Materia = it.materia,
                    Grupo = it.grupo,
                    C1 = it.c1,
                    C2 = it.c2,
                    C3 = it.c3,
                    C4 = it.c4,
                    C5 = it.c5,
                    C6 = it.c6,
                    C7 = it.c7,
                    C8 = it.c8,
                    C9 = it.c9,
                    C10 = it.c10,
                    C11 = it.c11,
                    C12 = it.c12,
                    C13 = it.c13
                )
            }
    }

    fun getCalifUnidadesLastUpdated(): String {
        return "Local"
    }

    fun saveCalifFinales(items: List<CalifFinalItem>) {

        queries.clearCalifFinales()

        items.forEach {
            queries.insertCalifFinal(
                materia = it.materia,
                calif = it.calif,
                acred = it.acred,
                grupo = it.grupo,
                observaciones = it.Observaciones
            )
        }
    }

    fun getCalifFinales(): List<CalifFinalItem> {

        return queries.selectAllCalifFinales()
            .executeAsList()
            .map {

                CalifFinalItem(
                    materia = it.materia,
                    calif = it.calif,
                    acred = it.acred,
                    grupo = it.grupo,
                    Observaciones = it.observaciones
                )
            }
    }

    fun getCalifFinalesLastUpdated(): String {
        return "Local"
    }
    fun clearAll() {

        queries.clearProfile()
        queries.clearSession()
        queries.clearCarga()
        queries.clearKardex()
        queries.clearCalifUnidades()
        queries.clearCalifFinales()
    }
}