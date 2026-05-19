package com.elearning.app.presentation.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.elearning.app.presentation.catalogue.CatalogueScreen
import com.elearning.app.presentation.formation.FormationDetailScreen
import com.elearning.app.presentation.player.SeancePlayerScreen
import com.elearning.app.presentation.profile.ProfileScreen
import com.elearning.app.presentation.quiz.QuizScreen
import com.elearning.app.presentation.scanner.ScannerScreen

@Composable
fun ElearningNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = "catalogue", modifier = modifier) {
        composable("catalogue") {
            CatalogueScreen(
                onNavigateToDetail = { id -> navController.navigate("formation_detail/$id") }
            )
        }
        composable("formation_detail/{formationId}") {
            FormationDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSeance = { id -> navController.navigate("player/$id") }
            )
        }
        composable("player/{seanceId}") {
            SeancePlayerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("scanner") {
            ScannerScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("profile") {
            ProfileScreen(
                onLogout = onLogout
            )
        }
        composable("quiz/{quizId}") {
            QuizScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
