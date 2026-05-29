package com.elearning.app.presentation.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.elearning.app.presentation.catalogue.CatalogueScreen
import com.elearning.app.presentation.certificates.CertificatesScreen
import com.elearning.app.presentation.favorites.FavoritesScreen
import com.elearning.app.presentation.formation.FormationDetailScreen
import com.elearning.app.presentation.home.HomeScreen
import com.elearning.app.presentation.notifications.NotificationsScreen
import com.elearning.app.presentation.player.SeancePlayerScreen
import com.elearning.app.presentation.profile.ProfileScreen
import com.elearning.app.presentation.quiz.QuizScreen
import com.elearning.app.presentation.scanner.ScannerScreen
import com.elearning.app.presentation.trainings.MyTrainingsScreen

@Composable
fun ElearningNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = MainRoutes.HOME, modifier = modifier) {
        composable(MainRoutes.HOME) {
            HomeScreen(
                onNavigateToDetail = { id -> navController.navigate(MainRoutes.formationDetail(id)) },
                onNavigateToNotifications = { navController.navigate(MainRoutes.NOTIFICATIONS) }
            )
        }
        composable(MainRoutes.MY_TRAININGS) {
            MyTrainingsScreen(
                onNavigateToPlayer = { id -> navController.navigate(MainRoutes.player(id)) },
                onNavigateToCatalogue = { navController.navigate(MainRoutes.CATALOGUE) }
            )
        }
        composable(MainRoutes.FAVORITES) {
            FavoritesScreen(
                onNavigateToDetail = { id -> navController.navigate(MainRoutes.formationDetail(id)) }
            )
        }
        composable(MainRoutes.CERTIFICATES) {
            CertificatesScreen()
        }
        composable(MainRoutes.CATALOGUE) {
            CatalogueScreen(
                onNavigateToDetail = { id -> navController.navigate(MainRoutes.formationDetail(id)) }
            )
        }
        composable(
            route = MainRoutes.FORMATION_DETAIL,
            arguments = listOf(navArgument("formationId") { type = NavType.StringType })
        ) {
            FormationDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSeance = { id -> navController.navigate(MainRoutes.player(id)) }
            )
        }
        composable(MainRoutes.PLAYER) {
            SeancePlayerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(MainRoutes.SCANNER) {
            ScannerScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(MainRoutes.PROFILE) {
            ProfileScreen(
                onLogout = onLogout
            )
        }
        composable(MainRoutes.QUIZ) {
            QuizScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(MainRoutes.NOTIFICATIONS) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNotificationClick = { deepLink ->
                    when {
                        deepLink == MainRoutes.CERTIFICATES -> navController.navigate(MainRoutes.CERTIFICATES)
                        MainRoutes.isPlayerRoute(deepLink) -> navController.navigate(deepLink!!)
                        MainRoutes.isQuizRoute(deepLink) -> navController.navigate(deepLink!!)
                    }
                }
            )
        }
    }
}
