package com.elearning.app.presentation

import android.os.Bundle
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.elearning.app.domain.model.AuthState
import com.elearning.app.domain.repository.ProfileRepository
import com.elearning.app.presentation.auth.AuthViewModel
import com.elearning.app.presentation.navigation.ELearningNavGraph
import com.elearning.app.presentation.theme.ELearningTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    @Inject
    lateinit var profileRepository: ProfileRepository

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
        intent?.let {
            authViewModel.handleOAuthCallback(it)
            handleDeepLink(it)
        }

        setContent {
            ELearningTheme {
                ELearningNavGraph(authViewModel = authViewModel)
            }
        }
        disableSystemStylusHandwritingTutorial()
        registerFcmTokenWhenAuthenticated()
    }

    private fun registerFcmTokenWhenAuthenticated() {
        lifecycleScope.launch {
            authViewModel.uiState
                .map { it.authState }
                .distinctUntilChanged()
                .filter { it == AuthState.Authenticated }
                .first()

            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    Log.d("FCM", "Generated FCM token: $token")
                    lifecycleScope.launch {
                        Log.d("FCM", "updateFcmToken request body={fcmToken=$token}")
                        val result = profileRepository.updateFcmToken(token)
                        Log.d("FCM", "updateFcmToken response=$result")
                    }
                }
                .addOnFailureListener { error ->
                    Log.e("FCM", "Unable to generate FCM token", error)
                }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        // Forward deep links / OAuth2 redirects received while app is running
        authViewModel.handleOAuthCallback(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: android.content.Intent) {
        val deepLink = intent.getStringExtra("deep_link")
        Log.d("DeepLinkNav", "MainActivity intent deep_link=$deepLink")
        if (!deepLink.isNullOrBlank()) {
            authViewModel.onDeepLinkReceived(deepLink)
        }
    }

    private fun disableSystemStylusHandwritingTutorial() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        window.decorView.post {
            disableAutoHandwriting(window.decorView)
        }
    }

    private fun disableAutoHandwriting(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view.setAutoHandwritingEnabled(false)
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                disableAutoHandwriting(view.getChildAt(index))
            }
        }
    }
}
