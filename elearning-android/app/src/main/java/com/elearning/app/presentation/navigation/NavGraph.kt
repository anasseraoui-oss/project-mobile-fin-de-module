package com.elearning.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elearning.app.presentation.auth.AuthEvent
import com.elearning.app.presentation.auth.AuthViewModel
import com.elearning.app.presentation.auth.screen.ForgotPasswordScreen
import com.elearning.app.presentation.auth.screen.LoginScreen
import com.elearning.app.presentation.auth.screen.RegisterScreen

// ─── Route Constants ──────────────────────────────────────────────────────────

object Routes {
    // Auth Graph
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    // App Graph (main content)
    const val HOME = "home"
    const val CATALOGUE = "catalogue"
    const val FORMATION_DETAIL = "formation/{formationId}"
    const val COURSE_PLAYER = "player/{seanceId}"
    const val QUIZ = "quiz/{quizId}"
    const val PROFILE = "profile"
    const val NOTIFICATIONS = "notifications"

    // Admin Graph
    const val ADMIN_DASHBOARD = "admin/dashboard"
    const val QR_GENERATOR = "admin/qr/{seanceId}"
    const val QR_SCANNER = "qr_scanner"

    // Helpers
    fun formationDetail(id: String) = "formation/$id"
    fun coursePlayer(seanceId: String) = "player/$seanceId"
    fun quiz(quizId: String) = "quiz/$quizId"
    fun qrGenerator(seanceId: String) = "admin/qr/$seanceId"
}

// ─── Root NavGraph ────────────────────────────────────────────────────────────

/**
 * Root navigation graph. Observes [AuthEvent]s from [AuthViewModel] to perform
 * top-level navigation (login ↔ home) without coupling individual screens to the ViewModel.
 */
@Composable
fun ELearningNavGraph(
    authViewModel: AuthViewModel,
    navController: NavHostController = rememberNavController()
) {
    val uiState by authViewModel.uiState.collectAsState()

    // Handle one-shot navigation events from AuthViewModel
    LaunchedEffect(authViewModel.events) {
        authViewModel.events.collect { event ->
            when (event) {
                AuthEvent.NavigateToHome -> {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
                AuthEvent.NavigateToLogin -> {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                is AuthEvent.ShowError -> { /* handled inline by screens */ }
                is AuthEvent.StartOAuthFlow -> {
                    // The screen handles the Custom Tab launch directly via Intent
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // ── Auth Graph ──────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── App Graph (placeholder — populated in Part 3) ───────────────────
        composable(Routes.HOME) {
            // AppScaffold will be added in Part 3
            AppScaffoldPlaceholder(
                authViewModel = authViewModel,
                navController = navController
            )
        }
    }
}
