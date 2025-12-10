package com.example.landmarkbangladesh.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.landmarkbangladesh.ui.components.Navbar
import com.example.landmarkbangladesh.ui.viewmodel.LandmarkViewModel


@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val sharedViewModel: LandmarkViewModel = viewModel()

    Scaffold(
        bottomBar = { Navbar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "overview",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("overview") {
                OverviewScreen(viewModel = sharedViewModel)
            }
            composable("records") {
                RecordsScreen(
                    viewModel = sharedViewModel,
                    onNavigateToEdit = { landmarkId ->
                        navController.navigate("edit_entry/$landmarkId")
                    }
                )
            }
            composable("new_entry") {
                NewEntryScreen(
                    viewModel = sharedViewModel,
                    onNavigateToRecords = {
                        navController.navigate("records") {
                            // Clear the back stack to prevent returning to form
                            popUpTo("new_entry") { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = "edit_entry/{landmarkId}",
                arguments = listOf(navArgument("landmarkId") { type = NavType.StringType })
            ) { backStackEntry ->
                val landmarkId = backStackEntry.arguments?.getString("landmarkId")
                EditEntryScreen(
                    viewModel = sharedViewModel,
                    landmarkId = landmarkId ?: "",
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToRecords = {
                        navController.navigate("records") {
                            popUpTo("records") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}



@Composable
fun NewEntryScreen(
    viewModel: LandmarkViewModel,
    onNavigateToRecords: () -> Unit = {}
) {
    FormScreen(
        viewModel = viewModel,
        landmark = null, // null means create new
        onNavigateBack = {
            Log.d("NewEntryScreen", "Navigation back requested")
        },
        onSuccessNavigation = onNavigateToRecords
    )
}

@Composable
fun EditEntryScreen(
    viewModel: LandmarkViewModel,
    landmarkId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {}
) {
    // Find the landmark from the viewModel's current state
    val uiState by viewModel.uiState.collectAsState()
    val landmark = (uiState as? com.example.landmarkbangladesh.ui.viewmodel.LandmarkUiState.Success)?.landmarks?.find {
        it.id.toString() == landmarkId
    }

    FormScreen(
        viewModel = viewModel,
        landmark = landmark,
        onNavigateBack = onNavigateBack,
        onSuccessNavigation = onNavigateToRecords
    )
}

