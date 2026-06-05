package com.example.clientapp.model

/**
 * Estado real de red derivado únicamente de Android APIs.
 * No contiene valores inventados ni simulados.
 */
sealed class NetworkState {

    /** Se está determinando el estado — no mostrar datos aún */
    object Loading : NetworkState()

    /** Conectado a Wi-Fi con datos reales del sistema */
    data class ConnectedWifi(
        /** SSID real. Null si el sistema lo restringe (Android 10+/sin permiso de ubicación) */
        val ssid: String?,
        /** Indica si hay conectividad real a Internet (NetworkCapabilities.NET_CAPABILITY_INTERNET + VALIDATED) */
        val hasInternet: Boolean,
        /** Mensaje informativo cuando el sistema restringe el acceso al SSID */
        val ssidRestrictionMessage: String?
    ) : NetworkState()

    /** Conectado a red móvil (celular) */
    data class ConnectedMobile(
        val hasInternet: Boolean
    ) : NetworkState()

    /** Sin conexión de red detectada */
    object Disconnected : NetworkState()

    /** El sistema lanzó una excepción real al consultar la red */
    data class Error(val cause: String) : NetworkState()
}
