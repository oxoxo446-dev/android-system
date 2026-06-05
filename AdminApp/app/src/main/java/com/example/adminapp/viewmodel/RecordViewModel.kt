package com.example.adminapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminapp.database.AppDatabase
import com.example.adminapp.database.entity.Record
import com.example.adminapp.repository.RecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estados posibles de las operaciones de escritura CRUD.
 * No hay estados simulados — cada caso refleja un resultado real.
 */
sealed class WriteState {
    object Idle : WriteState()
    object Loading : WriteState()
    object Success : WriteState()
    data class Failure(val reason: String) : WriteState()
}

/**
 * ViewModel para el panel de administración CRUD.
 * Expone la lista real de registros y el estado de las operaciones.
 */
class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).recordDao()
    private val repository = RecordRepository(dao)

    /** Lista de registros reales desde Room, vacía si no hay datos. */
    val records: StateFlow<List<Record>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _writeState = MutableStateFlow<WriteState>(WriteState.Idle)
    val writeState: StateFlow<WriteState> = _writeState.asStateFlow()

    /** Registro actual que se está editando. Null = formulario de creación. */
    private val _editingRecord = MutableStateFlow<Record?>(null)
    val editingRecord: StateFlow<Record?> = _editingRecord.asStateFlow()

    // ----------------------------------------------------------------
    // CRUD Operations
    // ----------------------------------------------------------------

    /** Crea un nuevo registro real en Room. */
    fun createRecord(title: String, description: String) {
        if (title.isBlank()) {
            _writeState.value = WriteState.Failure("El título no puede estar vacío")
            return
        }
        viewModelScope.launch {
            _writeState.value = WriteState.Loading
            try {
                repository.create(title, description)
                _writeState.value = WriteState.Success
            } catch (e: Exception) {
                _writeState.value = WriteState.Failure(
                    "Error al crear el registro: ${e.message}"
                )
            }
        }
    }

    /** Carga un registro real desde Room para editarlo. */
    fun loadRecordForEdit(id: Long) {
        viewModelScope.launch {
            _editingRecord.value = repository.getById(id)
        }
    }

    /** Limpia el registro en edición (modo creación). */
    fun clearEditingRecord() {
        _editingRecord.value = null
    }

    /** Actualiza un registro existente real en Room. */
    fun updateRecord(id: Long, title: String, description: String) {
        if (title.isBlank()) {
            _writeState.value = WriteState.Failure("El título no puede estar vacío")
            return
        }
        val current = _editingRecord.value ?: return
        viewModelScope.launch {
            _writeState.value = WriteState.Loading
            try {
                repository.update(
                    current.copy(title = title.trim(), description = description.trim())
                )
                _editingRecord.value = null
                _writeState.value = WriteState.Success
            } catch (e: Exception) {
                _writeState.value = WriteState.Failure(
                    "Error al actualizar: ${e.message}"
                )
            }
        }
    }

    /** Elimina un registro real de Room por su ID. */
    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            _writeState.value = WriteState.Loading
            try {
                repository.delete(id)
                _writeState.value = WriteState.Success
            } catch (e: Exception) {
                _writeState.value = WriteState.Failure(
                    "Error al eliminar: ${e.message}"
                )
            }
        }
    }

    /** Restablece el estado de escritura a Idle (tras mostrar éxito/error). */
    fun resetWriteState() {
        _writeState.value = WriteState.Idle
    }
}
