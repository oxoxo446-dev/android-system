package com.example.adminapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.adminapp.ui.RecordFormScreen
import com.example.adminapp.ui.RecordListScreen
import com.example.adminapp.ui.theme.AdminAppTheme
import com.example.adminapp.viewmodel.RecordViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AdminAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AdminNavGraph(viewModel = viewModel)
                }
            }
        }
    }
}

/** Rutas de navegación del panel de administración */
private object Routes {
    const val LIST = "list"
    const val CREATE = "create"
    const val EDIT = "edit/{recordId}"
    fun edit(id: Long) = "edit/$id"
}

@Composable
private fun AdminNavGraph(viewModel: RecordViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LIST
    ) {
        composable(Routes.LIST) {
            RecordListScreen(
                viewModel = viewModel,
                onNavigateToCreate = { navController.navigate(Routes.CREATE) },
                onNavigateToEdit = { id -> navController.navigate(Routes.edit(id)) }
            )
        }

        composable(Routes.CREATE) {
            RecordFormScreen(
                viewModel = viewModel,
                recordId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(
                navArgument("recordId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong("recordId")
            RecordFormScreen(
                viewModel = viewModel,
                recordId = recordId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
