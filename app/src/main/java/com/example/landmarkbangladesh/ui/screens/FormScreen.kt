package com.example.landmarkbangladesh.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.landmarkbangladesh.data.model.Landmark
import com.example.landmarkbangladesh.ui.viewmodel.CrudOperationState
import com.example.landmarkbangladesh.ui.viewmodel.LandmarkViewModel
import com.example.landmarkbangladesh.utils.ImageUtils
import com.example.landmarkbangladesh.utils.LocationCoordinates
import com.example.landmarkbangladesh.utils.LocationUtils
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    landmark: Landmark? = null, // null for create, non-null for edit
    onNavigateBack: () -> Unit,
    onSuccessNavigation: (() -> Unit)? = null,
    viewModel: LandmarkViewModel = viewModel()
) {
    var title by remember { mutableStateOf(landmark?.title ?: "") }
    var latitude by remember { mutableStateOf(landmark?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(landmark?.longitude?.toString() ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var resizedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLocationLoading by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isImageProcessing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val crudOperationState by viewModel.crudOperationState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }


    // Permission launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val allPermissionsGranted = permissions.values.all { it }
            if (allPermissionsGranted && landmark == null) {
                // Auto-detect GPS coordinates for new landmarks
                coroutineScope.launch {
                    detectCurrentLocation(context) { location, error ->
                        location?.let {
                            latitude = it.latitude.toString()
                            longitude = it.longitude.toString()
                            locationError = null
                        }
                        error?.let {
                            locationError = it
                        }
                        isLocationLoading = false
                    }
                }
            }
        }
    )



    // Image launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            Log.d("FormScreen", " Gallery result: URI = $uri")
            uri?.let {
                Log.d("FormScreen", " Image selected from gallery: $it")
                selectedImageUri = it
                processImage(context, it) { processedUri ->
                    resizedImageUri = processedUri
                    isImageProcessing = false
                    Log.d("FormScreen", " Gallery image processing completed")
                }
            } ?: run {
                Log.w("FormScreen", " No image selected from gallery")
                isImageProcessing = false
            }
        }
    )

    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            Log.d("FormScreen", " Camera result: success = $success")
            if (success && currentPhotoUri != null) {
                Log.d("FormScreen", " Photo captured successfully: ${currentPhotoUri}")
                selectedImageUri = currentPhotoUri
                processImage(context, currentPhotoUri!!) { processedUri ->
                    resizedImageUri = processedUri
                    isImageProcessing = false
                    Log.d("FormScreen", " Image processing completed")
                }
            } else {
                Log.w("FormScreen", " Camera capture failed or cancelled")
                isImageProcessing = false
            }
        }
    )

    // Auto-detect location when creating new landmark
    LaunchedEffect(landmark) {
        if (landmark == null) { // Only for new landmarks
            isLocationLoading = true
            locationPermissionLauncher.launch(LocationUtils.getRequiredPermissions())
        }
    }

    // Handle operation state changes
    LaunchedEffect(crudOperationState) {
        when (val currentState = crudOperationState) {
            is CrudOperationState.Success -> {
                Log.d("FormScreen", " Landmark operation successful!")

                // Show success snackbar
                val message = if (landmark == null) {
                    " Landmark created successfully and marked on map!"
                } else {
                    " Landmark updated successfully!"
                }

                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )

                // Wait a bit for snackbar to show, then navigate
                kotlinx.coroutines.delay(1000)

                // Clear the operation state
                viewModel.clearCrudOperationState()

                // Navigate to records screen if callback provided (for new landmarks)
                if (landmark == null && onSuccessNavigation != null) {
                    Log.d("FormScreen", "Navigating to records screen...")
                    onSuccessNavigation()
                } else {
                    // Navigate back for edit operations
                    onNavigateBack()
                }
            }
            else -> { /* Handle other states if needed */ }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

            // GPS Coordinates Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GPS Coordinates",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (landmark == null) { // Only show GPS button for new landmarks
                            FilledTonalButton(
                                onClick = {
                                    isLocationLoading = true
                                    locationError = null
                                    coroutineScope.launch {
                                        detectCurrentLocation(context) { location, error ->
                                            location?.let {
                                                latitude = it.latitude.toString()
                                                longitude = it.longitude.toString()
                                                locationError = null
                                            }
                                            error?.let {
                                                locationError = it
                                            }
                                            isLocationLoading = false
                                        }
                                    }
                                },
                                enabled = !isLocationLoading
                            ) {
                                if (isLocationLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                } else {
                                    Icon(Icons.Default.LocationOn, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Detect GPS")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Latitude Field
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = crudOperationState !is CrudOperationState.Loading
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Longitude Field
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = crudOperationState !is CrudOperationState.Loading
                    )

                    // Location status
                    if (isLocationLoading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Detecting GPS coordinates...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    locationError?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Location Error: $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Image Selection Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Landmark Image",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Image preview
                    if (selectedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Selected image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            if (isImageProcessing) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Resizing to 800×600...")
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Image selection buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Camera button
                        OutlinedButton(
                            onClick = {
                                Log.d("FormScreen", "📷 Camera button clicked")
                                isImageProcessing = true

                                try {
                                    // Create fresh photo file and URI for each capture
                                    val newPhotoFile = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
                                    val newPhotoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newPhotoFile)

                                    Log.d("FormScreen", "📁 Created photo file: ${newPhotoFile.absolutePath}")
                                    Log.d("FormScreen", "🔗 Created photo URI: $newPhotoUri")

                                    // Store the URI and launch camera
                                    currentPhotoUri = newPhotoUri
                                    cameraLauncher.launch(newPhotoUri)
                                } catch (e: Exception) {
                                    Log.e("FormScreen", "❌ Error launching camera: ${e.message}", e)
                                    isImageProcessing = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = crudOperationState !is CrudOperationState.Loading && !isImageProcessing
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Camera")
                        }

                        // Gallery button
                        OutlinedButton(
                            onClick = {
                                isImageProcessing = true
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f),
                            enabled = crudOperationState !is CrudOperationState.Loading && !isImageProcessing
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gallery")
                        }
                    }

                    if (resizedImageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Image resized to 800×600",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
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
                    val imageToSubmit = resizedImageUri ?: selectedImageUri

                    Log.d("FormScreen", "🚀 Submit button clicked")
                    Log.d("FormScreen", "📝 Title: $title")
                    Log.d("FormScreen", "📍 Coordinates: ($lat, $lon)")
                    Log.d("FormScreen", "🖼️ Selected image URI: $selectedImageUri")
                    Log.d("FormScreen", "📏 Resized image URI: $resizedImageUri")
                    Log.d("FormScreen", "📤 Image to submit: $imageToSubmit")

                    if (title.isNotBlank() && lat != null && lon != null) {
                        if (landmark == null) {
                            Log.d("FormScreen", "🆕 Creating new landmark...")
                            viewModel.createLandmark(title, lat, lon, imageToSubmit, context)
                        } else {
                            Log.d("FormScreen", "✏️ Updating existing landmark...")
                            viewModel.updateLandmark(
                                landmark.id,
                                title,
                                lat,
                                lon,
                                imageToSubmit,
                                context
                            )
                        }
                    } else {
                        Log.w("FormScreen", "⚠️ Form validation failed")
                        Log.w("FormScreen", "Title empty: ${title.isBlank()}")
                        Log.w("FormScreen", "Invalid coordinates: lat=$lat, lon=$lon")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = title.isNotBlank() &&
                         latitude.toDoubleOrNull() != null &&
                         longitude.toDoubleOrNull() != null &&
                         crudOperationState !is CrudOperationState.Loading &&
                         !isImageProcessing,
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
}

// Helper functions
private suspend fun detectCurrentLocation(
    context: Context,
    callback: (LocationCoordinates?, String?) -> Unit
) {
    try {
        val location = LocationUtils.getCurrentLocation(context)
        if (location != null) {
            callback(location, null)
        } else {
            callback(null, "Unable to detect GPS coordinates. Please enter manually.")
        }
    } catch (e: Exception) {
        Log.e("FormScreen", "Error detecting location", e)
        callback(null, "GPS detection failed: ${e.message}")
    }
}

private fun processImage(
    context: Context,
    imageUri: Uri,
    callback: (Uri?) -> Unit
) {
    Log.d("FormScreen", "🖼️ Starting image processing for URI: $imageUri")

    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            // Check if the original URI is accessible
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e("FormScreen", "❌ Cannot access image URI: $imageUri")
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    callback(null)
                }
                return@launch
            }
            inputStream.close()

            Log.d("FormScreen", "✅ Original image URI is accessible")

            // Resize the image
            val resizedUri = ImageUtils.resizeImage(context, imageUri)

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (resizedUri != null) {
                    Log.d("FormScreen", "✅ Image resized successfully to: $resizedUri")
                    callback(resizedUri)
                } else {
                    Log.w("FormScreen", "⚠️ Image resize failed, using original URI: $imageUri")
                    callback(imageUri) // Fallback to original if resize fails
                }
            }
        } catch (e: Exception) {
            Log.e("FormScreen", "❌ Error processing image: ${e.message}", e)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                // Fallback to original URI if processing fails
                callback(imageUri)
            }
        }
    }
}
