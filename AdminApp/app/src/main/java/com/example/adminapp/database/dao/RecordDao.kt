package com.example.adminapp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.adminapp.database.entity.Record
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD reales sobre la tabla "records".
 * Todos los métodos de lectura devuelven Flow para observar cambios en tiempo real.
 */
@Dao
interface RecordDao {

    /** Emite la lista completa cada vez que cambia la tabla. Orden: más reciente primero. */
    @Query("SELECT * FROM records ORDER BY created_at DESC")
    fun getAllRecords(): Flow<List<Record>>

    /** Lee un registro por su ID real. Null si no existe. */
    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): Record?

    /** Inserta un nuevo registro. Devuelve el rowId asignado por Room. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(record: Record): Long

    /** Actualiza un registro existente. Requiere que el ID ya exista en la tabla. */
    @Update
    suspend fun update(record: Record)

    /** Elimina el registro con el ID dado. */
    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Cuenta total de registros reales en la tabla. */
    @Query("SELECT COUNT(*) FROM records")
    suspend fun count(): Int
}
