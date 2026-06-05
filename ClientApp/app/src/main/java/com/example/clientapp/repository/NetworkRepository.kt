package com.example.clientapp.repository

import android.content.Context
import com.example.clientapp.model.NetworkState
import com.example.clientapp.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que expone el estado real de red al ViewModel.
 * Actúa como capa intermedia entre NetworkMonitor y la UI.
 */
class NetworkRepository(context: Context) {

    private val networkMonitor = NetworkMonitor(context)

    /**
     * Flow de estado de red real. El ViewModel lo recopila y lo expone como StateFlow.
     */
    val networkState: Flow<NetworkState> = networkMonitor.networkState
}
