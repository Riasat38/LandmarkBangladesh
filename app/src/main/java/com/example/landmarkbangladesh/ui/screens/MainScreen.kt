package com.example.landmarkbangladesh.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.landmarkbangladesh.ui.components.Navbar


@Composable
fun MainScreen() {
    val navController = rememberNavController()

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
                OverviewScreen()
            }
            composable("records") {
                RecordsScreen()
            }
            composable("new_entry") {
                NewEntryScreen(
                    onNavigateToRecords = {
                        navController.navigate("records") {
                            // Clear the back stack to prevent returning to form
                            popUpTo("new_entry") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}



@Composable
fun NewEntryScreen(
    onNavigateToRecords: () -> Unit = {}
) {
    FormScreen(
        landmark = null, // null means create new
        onNavigateBack = {
            Log.d("NewEntryScreen", "Navigation back requested")
        },
        onSuccessNavigation = onNavigateToRecords
    )
}
