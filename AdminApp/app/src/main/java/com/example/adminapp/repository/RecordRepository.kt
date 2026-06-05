package com.example.adminapp.repository

import com.example.adminapp.database.dao.RecordDao
import com.example.adminapp.database.entity.Record
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que abstrae el acceso a Room para el ViewModel.
 * No genera datos de prueba: todas las operaciones tocan la DB real.
 */
class RecordRepository(private val dao: RecordDao) {

    /** Flow de todos los registros reales, ordenados por fecha de creación descendente. */
    val allRecords: Flow<List<Record>> = dao.getAllRecords()

    /** Lee un registro por ID desde la base de datos real. */
    suspend fun getById(id: Long): Record? = dao.getRecordById(id)

    /**
     * Crea un nuevo registro real.
     * El campo [Record.id] debe ser 0 para que Room asigne el ID automáticamente.
     * @return ID asignado por Room.
     */
    suspend fun create(title: String, description: String): Long {
        val record = Record(
            id = 0,
            title = title.trim(),
            description = description.trim()
        )
        return dao.insert(record)
    }

    /** Actualiza un registro existente en la base de datos real. */
    suspend fun update(record: Record) = dao.update(record)

    /** Elimina el registro con el ID dado de la base de datos real. */
    suspend fun delete(id: Long) = dao.deleteById(id)
}
