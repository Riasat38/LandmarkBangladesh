package com.example.landmarkbangladesh.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.landmarkbangladesh.ui.components.AppTopBar
import com.example.landmarkbangladesh.ui.viewmodel.LandmarkUiState
import com.example.landmarkbangladesh.ui.viewmodel.LandmarkViewModel
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    viewModel: LandmarkViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Initialize OSMDroid configuration and refresh data
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Log.d("OverviewScreen", "Screen loaded, refreshing landmark data for map...")
        viewModel.loadLandmarks()
    }

    // Show snackbar when landmarks are loaded successfully
    LaunchedEffect(uiState) {
        if (uiState is LandmarkUiState.Success) {
            snackbarHostState.showSnackbar(
                message = "Map Successfully Loaded  ",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Landmarks Map Overview",
                actions = {
                    IconButton(
                        onClick = {
                            Log.d("OverviewScreen", "Refresh button clicked")
                            viewModel.loadLandmarks()
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val currentState = uiState) {
            is LandmarkUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading landmarks on map...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            is LandmarkUiState.Success -> {
                val landmarks = currentState.landmarks

                // Map view
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    factory = { context ->
                        Log.d("OverviewScreen", "Creating MapView with ${landmarks.size} landmarks")

                        MapView(context).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)

                            val mapController: IMapController = controller

                            // Center
                            val bangladeshCenter = GeoPoint(23.6850, 90.3563) // Dhaka center
                            mapController.setCenter(bangladeshCenter)
                            mapController.setZoom(7.0)

                            // markers for each landmark
                            landmarks.forEach { landmark ->
                                val marker = Marker(this).apply {
                                    position = GeoPoint(landmark.latitude, landmark.longitude)
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    title = landmark.title
                                    snippet = landmark.location
                                }

                                overlays.add(marker)
                                Log.d("OverviewScreen", "Added marker for ${landmark.title} at (${landmark.latitude}, ${landmark.longitude})")
                            }

                            // Refresh map
                            invalidate()
                        }
                    },
                    update = { mapView ->
                        // Update markers when landmarks change
                        Log.d("OverviewScreen", "Updating map with ${landmarks.size} landmarks")

                        // Clear existing markers
                        mapView.overlays.clear()

                        // Add updated markers
                        landmarks.forEach { landmark ->
                            val marker = Marker(mapView).apply {
                                position = GeoPoint(landmark.latitude, landmark.longitude)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = landmark.title
                                snippet = landmark.location
                            }

                            mapView.overlays.add(marker)
                        }

                        // Refresh
                        mapView.invalidate()
                    }
                )
            }

            is LandmarkUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error loading landmarks",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadLandmarks() }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}