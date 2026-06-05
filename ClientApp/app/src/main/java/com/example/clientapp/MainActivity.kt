package com.example.clientapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.clientapp.ui.NetworkScreen
import com.example.clientapp.ui.theme.ClientAppTheme
import com.example.clientapp.viewmodel.NetworkViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NetworkViewModel by viewModels()

    /**
     * Solicita permiso de ubicación en tiempo de ejecución.
     * Android 10+ requiere ACCESS_FINE_LOCATION para leer el SSID real.
     * Si el usuario lo deniega, la app muestra el mensaje de restricción del sistema.
     */
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Sin importar el resultado: la UI ya maneja el estado de restricción
        // mediante el mensaje real del sistema ("No se puede verificar el hotspot…")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLocationPermissionIfNeeded()

        setContent {
            ClientAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NetworkScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun requestLocationPermissionIfNeeded() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}
