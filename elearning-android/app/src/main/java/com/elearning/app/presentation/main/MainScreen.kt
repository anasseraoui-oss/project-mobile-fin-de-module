package com.elearning.app.presentation.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Responsive rule: Compact -> BottomBar, Medium/Expanded (Tablet) -> NavigationRail
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val isBottomOrRailVisible = currentRoute in listOf("catalogue", "scanner", "profile")

    Scaffold(
        bottomBar = {
            if (isCompact && isBottomOrRailVisible) {
                BottomNavigationBar(navController, currentRoute)
            }
        }
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Navigation Rail for Tablets
            if (!isCompact && isBottomOrRailVisible) {
                NavigationRailBar(navController, currentRoute)
            }
            
            ElearningNavGraph(
                navController = navController,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Catalogue") },
            label = { Text("Catalogue") },
            selected = currentRoute == "catalogue",
            onClick = { if(currentRoute != "catalogue") navController.navigate("catalogue") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner") },
            label = { Text("Scanner") },
            selected = currentRoute == "scanner",
            onClick = { if(currentRoute != "scanner") navController.navigate("scanner") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            label = { Text("Profil") },
            selected = currentRoute == "profile",
            onClick = { if(currentRoute != "profile") navController.navigate("profile") }
        )
    }
}

@Composable
private fun NavigationRailBar(navController: NavHostController, currentRoute: String?) {
    NavigationRail {
        NavigationRailItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Catalogue") },
            label = { Text("Catalogue") },
            selected = currentRoute == "catalogue",
            onClick = { if(currentRoute != "catalogue") navController.navigate("catalogue") }
        )
        NavigationRailItem(
            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner") },
            label = { Text("Scanner") },
            selected = currentRoute == "scanner",
            onClick = { if(currentRoute != "scanner") navController.navigate("scanner") }
        )
        NavigationRailItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            label = { Text("Profil") },
            selected = currentRoute == "profile",
            onClick = { if(currentRoute != "profile") navController.navigate("profile") }
        )
    }
}
