package com.elearning.app.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elearning.app.presentation.components.AvatarImage
import com.elearning.app.presentation.theme.Spacing

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarImage(name = "Apprenant Demo", imageUrl = null, size = 100.dp)
        Spacer(Modifier.height(Spacing.md))
        Text("Apprenant Demo", style = MaterialTheme.typography.headlineMedium)
        Text("apprenant@elearning.com", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(Modifier.height(Spacing.xl))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(Spacing.lg)) {
                Text("Mon Organisation", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.xs))
                Text("SupNumérique - Campus Paris", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    "ℹ️ La gestion détaillée des utilisateurs et la création d'organisations n'est pas disponible sur l'application mobile apprenant. Elle est réservée au Dashboard Web Admin.", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(Spacing.sm))
            Text("Se déconnecter")
        }
    }
}
