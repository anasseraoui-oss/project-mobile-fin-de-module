package com.elearning.app.presentation.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elearning.app.presentation.auth.AuthEvent
import com.elearning.app.presentation.auth.AuthViewModel
import com.elearning.app.presentation.theme.AnimDuration

@Composable
fun MainScreen(
    windowSizeClass: WindowSizeClass,
    authViewModel: AuthViewModel,
    pendingDeepLink: String? = null,
    onPendingDeepLinkConsumed: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Handle Deep Links
    LaunchedEffect(authViewModel.events) {
        authViewModel.events.collect { event ->
            if (event is AuthEvent.NavigateDeepLink) {
                navController.navigateResolvedDeepLink(event.route)
            }
        }
    }

    LaunchedEffect(pendingDeepLink) {
        if (!pendingDeepLink.isNullOrBlank()) {
            navController.navigateResolvedDeepLink(pendingDeepLink)
            onPendingDeepLinkConsumed()
        }
    }

    // Responsive rule: Compact -> BottomBar, Medium/Expanded (Tablet) -> NavigationRail
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val appContentMaxWidth = if (isCompact) null else 600.dp
    val isBottomOrRailVisible = currentRoute in bottomNavItems.map { it.route }

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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = if (isCompact) Alignment.TopStart else Alignment.TopCenter
            ) {
                ElearningNavGraph(
                    navController = navController,
                    onLogout = onLogout,
                    modifier = if (appContentMaxWidth == null) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .fillMaxHeight()
                            .widthIn(max = appContentMaxWidth)
                    }
                )
            }
        }
    }
}

private fun NavHostController.navigateResolvedDeepLink(deepLink: String) {
    val route = MainRoutes.resolveDeepLink(deepLink)
    android.util.Log.d("DeepLinkNav", "Received deep link=$deepLink, resolved=$route")
    route?.let { navigate(it) }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            val iconScale by animateFloatAsState(
                targetValue = if (selected) 1.1f else 1f,
                animationSpec = tween(AnimDuration.fast),
                label = "${item.route}BottomIconScale"
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = { navController.navigateBottom(item.route, currentRoute) }
            )
        }
    }
}

@Composable
private fun NavigationRailBar(navController: NavHostController, currentRoute: String?) {
    NavigationRail {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            val iconScale by animateFloatAsState(
                targetValue = if (selected) 1.1f else 1f,
                animationSpec = tween(AnimDuration.fast),
                label = "${item.route}RailIconScale"
            )
            NavigationRailItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = { navController.navigateBottom(item.route, currentRoute) }
            )
        }
    }
}

private fun NavHostController.navigateBottom(route: String, currentRoute: String?) {
    if (currentRoute == route) return
    navigate(route) {
        popUpTo(MainRoutes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(MainRoutes.HOME, "Accueil", Icons.Default.Home),
    BottomNavItem(MainRoutes.MY_TRAININGS, "Mes formations", Icons.Default.School),
    BottomNavItem(MainRoutes.FAVORITES, "Favoris", Icons.Default.Favorite),
    BottomNavItem(MainRoutes.CERTIFICATES, "Certificats", Icons.Default.EmojiEvents),
    BottomNavItem(MainRoutes.PROFILE, "Profil", Icons.Default.Person)
)
