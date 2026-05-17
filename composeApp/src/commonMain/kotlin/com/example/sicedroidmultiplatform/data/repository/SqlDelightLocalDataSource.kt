package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.database.SicenetDatabase
import com.example.sicedroidmultiplatform.data.models.*
import com.example.sicedroidmultiplatform.database.SessionEntity

class SqlDelightLocalDataSource(
    database: SicenetDatabase
) : LocalDataSource {
    private val queries = database.sicenetDatabaseQueries

    override fun saveProfile(profile: AlumnoProfile) {
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

    override fun getProfile(): AlumnoProfile? {
        return queries.selectProfile().executeAsOneOrNull()?.let {
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

    override fun saveSession(matricula: String, contrasenia: String, tipoUsuario: String) {
        queries.insertSession(matricula, contrasenia, tipoUsuario)
    }

    override fun getSession(): SessionEntity? = queries.getSession().executeAsOneOrNull()

    override fun saveCarga(items: List<CargaItem>) {
        queries.clearCarga()
        items.forEach {
            queries.insertCarga(
                it.Materia, it.Grupo, it.Docente, it.CreditosMateria.toLong(),
                it.Lunes, it.Martes, it.Miercoles, it.Jueves, it.Viernes
            )
        }
    }

    override fun getCarga(): List<CargaItem> {
        return queries.selectAllCarga().executeAsList().map {
            CargaItem(it.materia, it.grupo, it.docente, it.creditosMateria.toInt(),
                it.lunes, it.martes, it.miercoles, it.jueves, it.viernes)
        }
    }

    override fun saveKardex(items: List<KardexItem>) {
        queries.clearKardex()
        items.forEach { queries.insertKardex(it.clvOficial, it.materia, it.periodo, it.promedio) }
    }

    override fun getKardex(): List<KardexItem> {
        return queries.selectAllKardex().executeAsList().map { KardexItem(it.clvOficial, it.materia, it.periodo, it.promedio) }
    }

    override fun saveCalifUnidades(items: List<CalifUnidadItem>) {
        queries.clearCalifUnidades()
        items.forEach { queries.insertCalifUnidad(it.Materia, it.Grupo, it.C1, it.C2, it.C3, it.C4, it.C5, it.C6, it.C7, it.C8, it.C9, it.C10, it.C11, it.C12, it.C13) }
    }

    override fun getCalifUnidades(): List<CalifUnidadItem> {
        return queries.selectAllCalifUnidades().executeAsList().map {
            CalifUnidadItem(it.materia, it.grupo, it.c1, it.c2, it.c3, it.c4, it.c5, it.c6, it.c7, it.c8, it.c9, it.c10, it.c11, it.c12, it.c13)
        }
    }

    override fun saveCalifFinales(items: List<CalifFinalItem>) {
        queries.clearCalifFinales()
        items.forEach { queries.insertCalifFinal(it.materia, it.calif, it.acred, it.grupo, it.Observaciones) }
    }

    override fun getCalifFinales(): List<CalifFinalItem> {
        return queries.selectAllCalifFinales().executeAsList().map { CalifFinalItem(it.materia, it.calif, it.acred, it.grupo, it.observaciones) }
    }

    override fun clearAll() {
        queries.clearProfile()
        queries.clearSession()
        queries.clearCarga()
        queries.clearKardex()
        queries.clearCalifUnidades()
        queries.clearCalifFinales()
    }
}
