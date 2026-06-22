package com.elearning.app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elearning.app.domain.model.UserProfile
import com.elearning.app.domain.model.UserRole
import com.elearning.app.presentation.components.AvatarImage
import com.elearning.app.presentation.components.ErrorState
import com.elearning.app.presentation.components.FullScreenLoader
import com.elearning.app.presentation.theme.ELearningColors
import com.elearning.app.presentation.theme.FigmaSpacing
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.Spacing

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToInstructor: () -> Unit = {},
    onNavigateToQuizHistory: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        ProfileUiState.Loading -> FullScreenLoader(Modifier.background(ELearningColors.AppBackground))
        is ProfileUiState.Error -> ErrorState(
            message = state.message,
            onRetry = viewModel::loadProfile,
            modifier = Modifier.background(ELearningColors.AppBackground)
        )
        is ProfileUiState.Success -> ProfileContent(
            profile = state.profile,
            onLogout = onLogout,
            onNavigateToInstructor = onNavigateToInstructor,
            onNavigateToQuizHistory = onNavigateToQuizHistory
        )
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile,
    onLogout: () -> Unit,
    onNavigateToInstructor: () -> Unit,
    onNavigateToQuizHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ELearningColors.AppBackground)
            .padding(horizontal = FigmaSpacing.pageHorizontal)
            .padding(top = 48.dp, bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarImage(name = profile.fullName, imageUrl = profile.avatarUrl, size = 104.dp)
        Spacer(Modifier.height(Spacing.md))
        Text(
            text = profile.fullName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = ELearningColors.TextPrimary
        )
        Text(
            text = profile.email,
            style = MaterialTheme.typography.bodyMedium,
            color = ELearningColors.TextTertiary
        )

        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatCard(
                label = "Heures",
                value = "${profile.hoursSpent}h",
                icon = Icons.Default.Schedule,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                label = "Cours",
                value = profile.completedCourses.toString(),
                icon = Icons.Default.MenuBook,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                label = "Certificats",
                value = profile.certificatesCount.toString(),
                icon = Icons.Default.EmojiEvents,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        if (profile.role == UserRole.FORMATEUR || profile.role == UserRole.ADMIN_ORG) {
            InstructorEntryCard(onClick = onNavigateToInstructor)
            Spacer(Modifier.height(20.dp))
        }

        ProfileActionCard(
            title = "Historique des quiz",
            subtitle = "Consultez vos tentatives, scores et certificats",
            icon = Icons.Default.History,
            onClick = onNavigateToQuizHistory
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = Radius.xl,
            colors = CardDefaults.cardColors(containerColor = ELearningColors.CardSurface)
        ) {
            Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mon organisation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = profile.organisationName ?: "Aucune organisation rattachee",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ELearningColors.TextSecondary
                )
                HorizontalDivider(color = ELearningColors.BorderSubtle)
                Text(
                    text = "${profile.enrolledFormations} formations en cours - ${profile.completedFormations} terminees",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ELearningColors.TextTertiary
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = Radius.md,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(Spacing.sm))
            Text("Se deconnecter", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = Radius.xl,
        colors = CardDefaults.cardColors(containerColor = ELearningColors.CardSurface)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Surface(shape = Radius.md, color = Color(0xFFFFF7ED), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Color(0xFFEA580C))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ELearningColors.TextTertiary)
            }
            Surface(shape = CircleShape, color = Color(0xFFEA580C), modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Ouvrir", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun InstructorEntryCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = Radius.xl,
        colors = CardDefaults.cardColors(containerColor = ELearningColors.CardSurface)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Surface(shape = Radius.md, color = Color(0xFFE8EAF6), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF3F51B5))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Espace formateur", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Gerez vos formations et vos eleves",
                    style = MaterialTheme.typography.bodySmall,
                    color = ELearningColors.TextTertiary
                )
            }
            Surface(shape = CircleShape, color = Color(0xFF3F51B5), modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Ouvrir", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(112.dp),
        shape = Radius.xl,
        colors = CardDefaults.cardColors(containerColor = ELearningColors.CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = Color(0xFFEFF6FF), modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = ELearningColors.BrandBlue, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = ELearningColors.TextTertiary)
        }
    }
}
