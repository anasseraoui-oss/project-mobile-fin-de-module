package com.elearning.app.presentation.phase5

import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.elearning.app.presentation.certificates.CertificatesScreen
import com.elearning.app.presentation.certificates.CertificatesUiState
import com.elearning.app.presentation.favorites.FavoritesScreen
import com.elearning.app.presentation.favorites.FavoritesUiState
import com.elearning.app.presentation.notifications.NotificationsScreen
import com.elearning.app.presentation.notifications.NotificationsUiState
import com.elearning.app.presentation.theme.ELearningTheme
import androidx.test.filters.SdkSuppress
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

@SdkSuppress(maxSdkVersion = 36)
class Phase5ScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun favorites_emptyState_isDisplayed() {
        assumeStableInstrumentationSdk()
        composeRule.setContent {
            ELearningTheme {
                FavoritesScreen(
                    onNavigateToDetail = {},
                    uiState = FavoritesUiState.Empty
                )
            }
        }

        composeRule.onNodeWithText("Aucun favori pour le moment.").assertIsDisplayed()
    }

    @Test
    fun certificates_emptyState_isDisplayed() {
        assumeStableInstrumentationSdk()
        composeRule.setContent {
            ELearningTheme {
                CertificatesScreen(uiState = CertificatesUiState.Empty)
            }
        }

        composeRule.onNodeWithText("Aucun certificat obtenu pour le moment.").assertIsDisplayed()
    }

    @Test
    fun notifications_emptyState_isDisplayed() {
        assumeStableInstrumentationSdk()
        composeRule.setContent {
            ELearningTheme {
                NotificationsScreen(
                    onNavigateBack = {},
                    onNotificationClick = {},
                    uiState = NotificationsUiState.Empty
                )
            }
        }

        composeRule.onNodeWithText("Aucune notification pour le moment.").assertIsDisplayed()
    }

    private fun assumeStableInstrumentationSdk() {
        assumeTrue(
            "Compose/Espresso instrumentation is not stable on Android preview SDK ${Build.VERSION.SDK_INT}",
            Build.VERSION.SDK_INT < 37
        )
    }
}
