package com.elearning.app.presentation.formation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.elearning.app.presentation.components.*
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormationDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSeance: (String) -> Unit,
    viewModel: FormationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEnrolling by viewModel.isEnrolling.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Aperçu", "Cours", "Avis")

    Scaffold(
        bottomBar = {
            if (uiState is FormationDetailState.Success) {
                val state = uiState as FormationDetailState.Success
                if (!state.formation.isEnrolled) {
                    Surface(
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.lg)
                                .navigationBarsPadding(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Prix total",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (state.formation.price == 0.0) "Gratuit" else "${state.formation.price} ${state.formation.currency}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Button(
                                onClick = viewModel::enroll,
                                enabled = !isEnrolling,
                                shape = Radius.md,
                                modifier = Modifier.height(48.dp)
                            ) {
                                if (isEnrolling) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("S'inscrire maintenant")
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when (uiState) {
            is FormationDetailState.Loading -> FullScreenLoader()
            is FormationDetailState.Error -> ErrorState(
                message = (uiState as FormationDetailState.Error).message,
                onRetry = viewModel::loadFormationDetails
            )
            is FormationDetailState.Success -> {
                val state = uiState as FormationDetailState.Success
                val formation = state.formation
                val courses = state.courses

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = paddingValues.calculateBottomPadding())
                ) {
                    // Header Image with Scrim
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            AsyncImage(
                                model = formation.thumbnailUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                            startY = 200f
                                        )
                                    )
                            )
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .statusBarsPadding()
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                            }
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(Spacing.lg)
                            ) {
                                LevelBadge(level = formation.level)
                                Spacer(Modifier.height(Spacing.xs))
                                Text(
                                    text = formation.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Tabs
                    item {
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = MaterialTheme.colorScheme.surface,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { Text(title, fontWeight = FontWeight.SemiBold) }
                                )
                            }
                        }
                    }

                    // Tab Content
                    item {
                        when (selectedTabIndex) {
                            0 -> { // Aperçu
                                Column(modifier = Modifier.padding(Spacing.lg)) {
                                    Text("À propos de cette formation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(Spacing.sm))
                                    Text(formation.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            1 -> { // Cours
                                Column(modifier = Modifier.padding(Spacing.lg)) {
                                    courses.forEachIndexed { index, course ->
                                        CourseCard(
                                            courseTitle = course.title,
                                            courseIndex = index,
                                            seances = course.seances,
                                            initiallyExpanded = index == 0, // First expanded by default
                                            onSeanceClick = { onNavigateToSeance(it.id.toString()) }
                                        )
                                        Spacer(Modifier.height(Spacing.md))
                                    }
                                }
                            }
                            2 -> { // Avis
                                EmptyState(message = "Les avis seront bientôt disponibles.")
                            }
                        }
                    }
                }
            }
        }
    }
}
