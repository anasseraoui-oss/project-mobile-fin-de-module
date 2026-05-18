package com.elearning.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.elearning.app.presentation.auth.AuthViewModel

/**
 * Temporary placeholder for the main app scaffold.
 * Will be replaced by the full AppScaffold in Part 3 (Design System & Navigation).
 */
@Composable
fun AppScaffoldPlaceholder(
    authViewModel: AuthViewModel,
    navController: NavHostController
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { authViewModel.logout() }) {
            Text(
                text = "Déconnexion (Home Placeholder)",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
