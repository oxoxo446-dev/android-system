package com.example.clientapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.clientapp.model.NetworkState
import com.example.clientapp.repository.NetworkRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel que expone el estado real de red a la UI mediante StateFlow.
 * No genera datos simulados: todo viene del sistema Android a través del repositorio.
 */
class NetworkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NetworkRepository(application.applicationContext)

    /**
     * StateFlow observable desde Compose.
     * Emite Loading mientras no hay datos del sistema,
     * luego el estado real conforme cambia la red.
     */
    val networkState: StateFlow<NetworkState> = repository.networkState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NetworkState.Loading
        )
}
