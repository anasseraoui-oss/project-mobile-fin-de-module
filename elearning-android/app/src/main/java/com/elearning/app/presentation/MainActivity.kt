package com.elearning.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.elearning.app.presentation.auth.AuthViewModel
import com.elearning.app.presentation.navigation.ELearningNavGraph
import com.elearning.app.presentation.theme.ELearningTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity — single-activity host for the Compose navigation graph.
 *
 * Handles:
 * - Splash screen (keeps visible until auth state is determined).
 * - OAuth2 redirect intents forwarded to [AuthViewModel].
 * - Edge-to-edge rendering.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Keep splash screen until the auth state is no longer Loading
        val splashScreen = installSplashScreen()
        // splashScreen.setKeepOnScreenCondition {
        //     authViewModel.uiState.value.authState ==
        //             com.elearning.app.domain.model.AuthState.Loading
        // }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Forward any OAuth2 redirect intent received at launch
        intent?.let { authViewModel.handleOAuthCallback(it) }

        setContent {
            ELearningTheme {
                ELearningNavGraph(authViewModel = authViewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        // Forward deep links / OAuth2 redirects received while app is running
        authViewModel.handleOAuthCallback(intent)
    }
}
