package com.example.clientapp.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import com.example.clientapp.model.NetworkState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Monitorea la red real del dispositivo usando ConnectivityManager y WifiManager.
 * No simula estados — todo viene de las APIs del sistema Android.
 */
class NetworkMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Suppress("DEPRECATION")
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    /**
     * Flow que emite el estado real de red cada vez que cambia.
     * Usa NetworkCallback para recibir eventos reales del sistema operativo.
     */
    val networkState: Flow<NetworkState> = callbackFlow {

        // Emitir estado actual al suscribirse
        trySend(getCurrentState())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getCurrentState())
            }

            override fun onLost(network: Network) {
                trySend(NetworkState.Disconnected)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(buildStateFromCapabilities(networkCapabilities))
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            trySend(NetworkState.Error("Error al registrar NetworkCallback: ${e.message}"))
        }

        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (_: Exception) {
                // Ya desregistrado — ignorar
            }
        }
    }.distinctUntilChanged()

    /**
     * Lee el estado de red en este momento exacto desde el sistema.
     */
    private fun getCurrentState(): NetworkState {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
                ?: return NetworkState.Disconnected

            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                ?: return NetworkState.Disconnected

            buildStateFromCapabilities(capabilities)
        } catch (e: SecurityException) {
            NetworkState.Error("Permiso denegado por el sistema: ${e.message}")
        } catch (e: Exception) {
            NetworkState.Error("Error del sistema al leer la red: ${e.message}")
        }
    }

    /**
     * Construye el estado a partir de las capacidades reales reportadas por Android.
     */
    private fun buildStateFromCapabilities(capabilities: NetworkCapabilities): NetworkState {
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                val (ssid, restrictionMsg) = readWifiSsid()
                NetworkState.ConnectedWifi(
                    ssid = ssid,
                    hasInternet = hasInternet,
                    ssidRestrictionMessage = restrictionMsg
                )
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                NetworkState.ConnectedMobile(hasInternet = hasInternet)
            }
            else -> NetworkState.Disconnected
        }
    }

    /**
     * Intenta leer el SSID real usando WifiManager.
     * En Android 10+ se requiere permiso ACCESS_FINE_LOCATION.
     * Si el sistema lo restringe → devuelve null con un mensaje real.
     *
     * @return Pair(ssid o null, mensajeDeRestricción o null)
     */
    @Suppress("DEPRECATION")
    private fun readWifiSsid(): Pair<String?, String?> {
        return try {
            val wifiInfo = wifiManager.connectionInfo
            val rawSsid = wifiInfo?.ssid

            when {
                rawSsid == null || rawSsid == "<unknown ssid>" -> {
                    // Android restringe el SSID sin permiso de ubicación exacta
                    null to "No se puede verificar el hotspot por restricciones del sistema. " +
                            "Concede el permiso de Ubicación para ver el SSID."
                }
                else -> {
                    // Quitar comillas que WifiManager agrega al SSID
                    rawSsid.removeSurrounding("\"") to null
                }
            }
        } catch (e: SecurityException) {
            null to "Acceso al SSID bloqueado por el sistema: ${e.message}"
        } catch (e: Exception) {
            null to "Error al leer información Wi-Fi: ${e.message}"
        }
    }
}
