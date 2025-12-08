package com.example.landmarkbangladesh.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.landmarkbangladesh.data.model.Landmark
import com.example.landmarkbangladesh.ui.viewmodel.CrudOperationState
import com.example.landmarkbangladesh.ui.viewmodel.LandmarkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLandmarkScreen(
    landmark: Landmark? = null, // null for create, non-null for edit
    onNavigateBack: () -> Unit,
    viewModel: LandmarkViewModel = viewModel()
) {
    var title by remember { mutableStateOf(landmark?.title ?: "") }
    var latitude by remember { mutableStateOf(landmark?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(landmark?.longitude?.toString() ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val crudOperationState by viewModel.crudOperationState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri = uri
        }
    )

    // Handle operation state changes
    LaunchedEffect(crudOperationState) {
        when (crudOperationState) {
            is CrudOperationState.Success -> {
                // Navigate back on success
                onNavigateBack()
                viewModel.clearCrudOperationState()
            }
            else -> { /* Handle other states if needed */ }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (landmark == null) "Add New Landmark" else "Edit Landmark",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Landmark Title") },
                modifier = Modifier.fillMaxWidth(),
                enabled = crudOperationState !is CrudOperationState.Loading
            )

            // Latitude Field
            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = crudOperationState !is CrudOperationState.Loading
            )

            // Longitude Field
            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = crudOperationState !is CrudOperationState.Loading
            )

            // Image Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (selectedImageUri != null) "Image Selected" else "No Image Selected",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        enabled = crudOperationState !is CrudOperationState.Loading
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Image")
                    }
                }
            }

            // Operation State Display
            when (val currentOperationState = crudOperationState) {
                is CrudOperationState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (landmark == null) "Creating landmark..." else "Updating landmark...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                is CrudOperationState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Error: ${currentOperationState.message}",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                else -> { /* Idle state - show nothing */ }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    val lat = latitude.toDoubleOrNull()
                    val lon = longitude.toDoubleOrNull()

                    if (title.isNotBlank() && lat != null && lon != null) {
                        if (landmark == null) {
                            // Create new landmark
                            viewModel.createLandmark(title, lat, lon, selectedImageUri, context)
                        } else {
                            // Update existing landmark
                            viewModel.updateLandmark(
                                landmark.id,
                                title,
                                lat,
                                lon,
                                selectedImageUri,
                                context
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = title.isNotBlank() &&
                         latitude.toDoubleOrNull() != null &&
                         longitude.toDoubleOrNull() != null &&
                         crudOperationState !is CrudOperationState.Loading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (landmark == null) "Create Landmark" else "Update Landmark",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Additional spacing at bottom
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
