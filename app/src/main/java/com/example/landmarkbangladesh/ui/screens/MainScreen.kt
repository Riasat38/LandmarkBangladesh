package com.example.landmarkbangladesh.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
                NewEntryScreen()
            }
        }
    }
}

@Composable
fun OverviewScreen() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "Overview Screen",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun RecordsScreen() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "Records Screen",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun NewEntryScreen() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "New Entry Screen",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
    }
}
