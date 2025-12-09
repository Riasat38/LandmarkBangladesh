package com.example.landmarkbangladesh.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.landmarkbangladesh.ui.components.LandmarkCard
import com.example.landmarkbangladesh.ui.viewmodel.LandmarkUiState
import com.example.landmarkbangladesh.ui.viewmodel.LandmarkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    viewModel: LandmarkViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Refresh data when screen is composed
    LaunchedEffect(Unit) {
        Log.d("RecordsScreen", "🔄 Screen loaded, refreshing landmark data...")
        viewModel.loadLandmarks()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    text = "Landmarks of Bangladesh",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        )

        when (val currentState = uiState) {
            is LandmarkUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading landmarks from API...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "https://labs.anontech.info/cse489/t3/api.php",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is LandmarkUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = " Found ${currentState.landmarks.size} landmarks",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    items(currentState.landmarks) { landmark ->
                        LandmarkCard(
                            landmark = landmark,
                            onClick = {
                                // TODO: Navigate to landmark details
                            },
                            onEdit = {
                                // TODO: Navigate to FormScreen with landmark data
                                Log.d("RecordsScreen", "Edit landmark: ${landmark.title}")
                            },
                            onDelete = {
                                // Show confirmation and delete
                                Log.d("RecordsScreen", "Delete landmark: ${landmark.title}")
                                viewModel.deleteLandmark(landmark.id)
                            }
                        )
                    }
                }
            }

            is LandmarkUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
