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
import com.elearning.app.presentation.instructor.FormationEditorRoute
import com.elearning.app.presentation.instructor.InstructorDashboardRoute
import com.elearning.app.presentation.instructor.SeanceEditorRoute
import com.elearning.app.presentation.notifications.NotificationsScreen
import com.elearning.app.presentation.player.SeancePlayerScreen
import com.elearning.app.presentation.profile.ProfileScreen
import com.elearning.app.presentation.quiz.QuizScreen
import com.elearning.app.presentation.quiz.history.QuizHistoryScreen
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
                onNavigateToCatalogue = { categoryId -> navController.navigate(MainRoutes.catalogue(categoryId)) },
                onNavigateToNotifications = { navController.navigate(MainRoutes.NOTIFICATIONS) }
            )
        }
        composable(MainRoutes.MY_TRAININGS) {
            MyTrainingsScreen(
                onNavigateToPlayer = { id -> navController.navigate(MainRoutes.player(id)) },
                onNavigateToCatalogue = { navController.navigate(MainRoutes.catalogue()) }
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
        composable(
            route = MainRoutes.CATALOGUE_WITH_FILTER,
            arguments = listOf(navArgument("categoryId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
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
                onLogout = onLogout,
                onNavigateToInstructor = { navController.navigate(MainRoutes.INSTRUCTOR_DASHBOARD) },
                onNavigateToQuizHistory = { navController.navigate(MainRoutes.QUIZ_HISTORY) }
            )
        }
        composable(MainRoutes.QUIZ_HISTORY) {
            QuizHistoryScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(MainRoutes.INSTRUCTOR_DASHBOARD) {
            InstructorDashboardRoute(
                onNavigateBack = { navController.popBackStack() },
                onCreateFormation = { navController.navigate(MainRoutes.INSTRUCTOR_FORMATION_NEW) },
                onEditFormation = { id -> navController.navigate(MainRoutes.instructorFormationEdit(id)) },
                onPreviewFormation = { id -> navController.navigate(MainRoutes.formationDetail(id)) }
            )
        }
        composable(MainRoutes.INSTRUCTOR_FORMATION_NEW) {
            FormationEditorRoute(
                onNavigateBack = { navController.popBackStack() },
                onEditSeanceContent = { id -> navController.navigate(MainRoutes.instructorSeanceEdit(id)) }
            )
        }
        composable(
            route = MainRoutes.INSTRUCTOR_FORMATION_EDIT,
            arguments = listOf(navArgument("formationId") { type = NavType.StringType })
        ) {
            FormationEditorRoute(
                onNavigateBack = { navController.popBackStack() },
                onEditSeanceContent = { id -> navController.navigate(MainRoutes.instructorSeanceEdit(id)) }
            )
        }
        composable(
            route = MainRoutes.INSTRUCTOR_SEANCE_EDIT,
            arguments = listOf(navArgument("seanceId") { type = NavType.StringType })
        ) {
            SeanceEditorRoute(onNavigateBack = { navController.popBackStack() })
        }
        composable(MainRoutes.QUIZ) {
            QuizScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(MainRoutes.NOTIFICATIONS) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNotificationClick = { deepLink ->
                    when (val route = MainRoutes.resolveDeepLink(deepLink)) {
                        null -> Unit
                        else -> navController.navigate(route)
                    }
                }
            )
        }
    }
}
