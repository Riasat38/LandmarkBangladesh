package com.example.landmarkbangladesh.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OverviewScreen() {
    val context = LocalContext.current

    // Bangladesh center coordinates
    val bangladeshCenter = remember { GeoPoint(23.6850, 90.3563) }

    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = "LandmarkBangladesh"
    }

    // State to track if we need to center the map
    val shouldCenterMap = remember { androidx.compose.runtime.mutableStateOf(true) }

    // Trigger centering when screen is opened
    LaunchedEffect(key1 = bangladeshCenter) {
        shouldCenterMap.value = true
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // OpenStreetMap View
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    // Configure map settings
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    // Set minimum and maximum zoom levels
                    minZoomLevel = 5.0
                    maxZoomLevel = 18.0

                    // Set initial position to Bangladesh center
                    controller.setZoom(7.0)
                    controller.setCenter(bangladeshCenter)

                    // Add marker for Bangladesh center
                    val marker = Marker(this).apply {
                        position = bangladeshCenter
                        title = "Bangladesh"
                        snippet = "The heart of Bangladesh - Dhaka Region"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    overlays.add(marker)

                    // Force center the map when created
                    post {
                        controller.animateTo(bangladeshCenter, 7.0, 1000L)
                        shouldCenterMap.value = false
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                // Center on Bangladesh when screen is opened or updated
                if (shouldCenterMap.value) {
                    mapView.controller.animateTo(bangladeshCenter, 7.0, 800L)
                    shouldCenterMap.value = false
                }
                mapView.invalidate()
            }
        )

        // Title overlay
        Text(
            text = "🇧🇩 Bangladesh Landmarks",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(
                    Color.Black.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

    }
}
