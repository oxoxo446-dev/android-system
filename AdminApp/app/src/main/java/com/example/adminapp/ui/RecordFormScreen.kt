package com.example.adminapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.adminapp.viewmodel.RecordViewModel
import com.example.adminapp.viewmodel.WriteState

/**
 * Pantalla de formulario para crear o editar un registro real en Room.
 * No precarga datos de ejemplo — el formulario inicia vacío en modo creación
 * y con los datos reales del registro en modo edición.
 *
 * @param recordId Si es null → modo creación. Si tiene valor → modo edición.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordFormScreen(
    viewModel: RecordViewModel,
    recordId: Long?,
    onNavigateBack: () -> Unit
) {
    val isEditMode = recordId != null
    val editingRecord by viewModel.editingRecord.collectAsState()
    val writeState by viewModel.writeState.collectAsState()

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var fieldsLoaded by remember { mutableStateOf(!isEditMode) }

    // En modo edición: rellenar campos con datos reales del registro
    LaunchedEffect(editingRecord) {
        if (isEditMode && editingRecord != null && !fieldsLoaded) {
            title = editingRecord!!.title
            description = editingRecord!!.description
            fieldsLoaded = true
        }
    }

    // Navegar atrás cuando la operación real se completa con éxito
    LaunchedEffect(writeState) {
        if (writeState is WriteState.Success) {
            viewModel.resetWriteState()
            viewModel.clearEditingRecord()
            onNavigateBack()
        }
    }

    val isLoading = writeState is WriteState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Editar Registro" else "Nuevo Registro")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearEditingRecord()
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (isEditMode && !fieldsLoaded) {
                // Cargando datos reales del registro desde Room
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cargando datos del registro…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = null
                    },
                    label = { Text("Título *") },
                    placeholder = { Text("Nombre o identificador del registro") },
                    isError = titleError != null,
                    supportingText = {
                        if (titleError != null) {
                            Text(
                                text = titleError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Descripción opcional del registro") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (writeState is WriteState.Failure) {
                    Text(
                        text = (writeState as WriteState.Failure).reason,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            titleError = "El título no puede estar vacío"
                            return@Button
                        }
                        if (isEditMode && editingRecord != null) {
                            viewModel.updateRecord(editingRecord!!.id, title, description)
                        } else {
                            viewModel.createRecord(title, description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (isEditMode) "Guardar Cambios" else "Crear Registro")
                    }
                }
            }
        }
    }
}
