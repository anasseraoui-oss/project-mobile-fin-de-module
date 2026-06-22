package com.elearning.app.presentation.instructor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.elearning.app.domain.model.InstructorDashboard
import com.elearning.app.domain.model.InstructorFormationSummary
import com.elearning.app.presentation.components.AvatarImage
import com.elearning.app.presentation.components.EmptyState
import com.elearning.app.presentation.components.ErrorState
import com.elearning.app.presentation.components.FullScreenLoader
import com.elearning.app.presentation.theme.ELearningColors
import com.elearning.app.presentation.theme.FigmaSpacing
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.Spacing

private val InstructorBlue = Color(0xFF3F51B5)

@Composable
fun InstructorDashboardRoute(
    onNavigateBack: () -> Unit,
    onCreateFormation: () -> Unit,
    onEditFormation: (String) -> Unit,
    onPreviewFormation: (String) -> Unit,
    viewModel: InstructorDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        InstructorDashboardUiState.Loading -> FullScreenLoader(Modifier.background(Color(0xFFF5F5F5)))
        is InstructorDashboardUiState.Error -> ErrorState(
            message = state.message,
            onRetry = { viewModel.refresh() },
            modifier = Modifier.background(Color(0xFFF5F5F5))
        )
        is InstructorDashboardUiState.Success -> InstructorDashboardScreen(
            dashboard = state.dashboard,
            formations = state.formations,
            selectedStatus = state.selectedStatus,
            onBack = onNavigateBack,
            onCreateFormation = onCreateFormation,
            onEditFormation = onEditFormation,
            onPreviewFormation = onPreviewFormation,
            onArchiveFormation = viewModel::archiveFormation,
            onDeleteFormation = viewModel::deleteFormation,
            onStatusSelected = viewModel::refresh
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorDashboardScreen(
    dashboard: InstructorDashboard,
    formations: List<InstructorFormationSummary>,
    selectedStatus: String?,
    onBack: () -> Unit,
    onCreateFormation: () -> Unit,
    onEditFormation: (String) -> Unit,
    onPreviewFormation: (String) -> Unit,
    onArchiveFormation: (String) -> Unit,
    onDeleteFormation: (String) -> Unit,
    onStatusSelected: (String?) -> Unit
) {
    var selectedDashboardTab by rememberSaveable { mutableStateOf(InstructorDashboardTab.Overview.name) }
    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = { Text("Espace formateur", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = FigmaSpacing.pageHorizontal),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item { InstructorHeader(dashboard) }
            item { InstructorStatsRow(dashboard) }
            item {
                InstructorDashboardTabs(
                    selected = InstructorDashboardTab.valueOf(selectedDashboardTab),
                    onSelected = { selectedDashboardTab = it.name }
                )
            }
            when (InstructorDashboardTab.valueOf(selectedDashboardTab)) {
                InstructorDashboardTab.Overview -> {
                    item {
                        Button(
                            onClick = onCreateFormation,
                            shape = Radius.lg,
                            colors = ButtonDefaults.buttonColors(containerColor = InstructorBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(Spacing.sm))
                            Text("Creer une formation", fontWeight = FontWeight.Bold)
                        }
                    }
                    item {
                        StatusTabs(selectedStatus = selectedStatus, onStatusSelected = onStatusSelected)
                    }
                    if (formations.isEmpty()) {
                        item {
                            EmptyState(
                                message = "Creez votre premiere formation",
                                icon = Icons.Default.School,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                            )
                        }
                    } else {
                        items(formations, key = { it.id }) { formation ->
                            InstructorFormationCard(
                                formation = formation,
                                onEdit = { onEditFormation(formation.id.toString()) },
                                onPreview = { onPreviewFormation(formation.id.toString()) },
                                onArchive = { onArchiveFormation(formation.id.toString()) },
                                onDelete = { onDeleteFormation(formation.id.toString()) }
                            )
                        }
                        item { Spacer(Modifier.height(Spacing.xxl)) }
                    }
                }
                InstructorDashboardTab.Stats -> item {
                    InstructorStatsTab(dashboard = dashboard)
                }
                InstructorDashboardTab.Learners -> item {
                    InstructorLearnersTab()
                }
                InstructorDashboardTab.Resources -> item {
                    InstructorResourcesTab()
                }
            }
        }
    }
}

@Composable
private fun InstructorDashboardTabs(
    selected: InstructorDashboardTab,
    onSelected: (InstructorDashboardTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        InstructorDashboardTab.entries.forEach { tab ->
            FilterChip(
                selected = selected == tab,
                onClick = { onSelected(tab) },
                label = { Text(tab.label) }
            )
        }
    }
}

@Composable
private fun InstructorStatsTab(dashboard: InstructorDashboard) {
    Card(shape = Radius.lg, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Statistiques detaillees", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Formations actives: ${dashboard.stats.activeFormations}", color = ELearningColors.TextSecondary)
            Text("Apprenants: ${dashboard.stats.totalLearners}", color = ELearningColors.TextSecondary)
            Text("Completion moyenne: ${dashboard.stats.averageCompletionPercent}%", color = ELearningColors.TextSecondary)
            EmptyState(
                message = "Les tendances, taux quiz et progression detaillee apparaitront ici apres branchement de l'endpoint stats.",
                icon = Icons.Default.ShowChart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }
    }
}

@Composable
private fun InstructorLearnersTab() {
    EmptyState(
        message = "Aucun apprenant a afficher pour le moment",
        icon = Icons.Default.People,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    )
}

@Composable
private fun InstructorResourcesTab() {
    EmptyState(
        message = "Aucune ressource formateur disponible",
        icon = Icons.Default.AttachFile,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    )
}

private enum class InstructorDashboardTab(val label: String) {
    Overview("Formations"),
    Stats("Stats"),
    Learners("Apprenants"),
    Resources("Ressources")
}

@Composable
private fun InstructorHeader(dashboard: InstructorDashboard) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        AvatarImage(
            name = dashboard.instructor.fullName,
            imageUrl = dashboard.instructor.avatarUrl,
            size = 64.dp
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dashboard.instructor.fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = ELearningColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = dashboard.instructor.organisationName ?: "Organisation non rattachee",
                style = MaterialTheme.typography.bodyMedium,
                color = ELearningColors.TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(shape = Radius.full, color = Color(0xFFE8EAF6)) {
            Text(
                text = dashboard.instructor.levelLabel,
                color = InstructorBlue,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun InstructorStatsRow(dashboard: InstructorDashboard) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        StatCard(Icons.Default.School, "Actives", dashboard.stats.activeFormations.toString())
        StatCard(Icons.Default.Group, "Apprenants", dashboard.stats.totalLearners.toString())
        StatCard(Icons.Default.ShowChart, "Completion", "${dashboard.stats.averageCompletionPercent}%")
        StatCard(
            Icons.Default.TrendingUp,
            "Revenus",
            "${dashboard.stats.monthlyRevenue.toInt()} ${dashboard.stats.monthlyRevenueCurrency}"
        )
    }
}

@Composable
private fun StatCard(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier.width(150.dp),
        shape = Radius.lg,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Surface(shape = CircleShape, color = Color(0xFFE8EAF6), modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = InstructorBlue, modifier = Modifier.size(18.dp))
                }
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = ELearningColors.TextTertiary)
        }
    }
}

@Composable
private fun StatusTabs(selectedStatus: String?, onStatusSelected: (String?) -> Unit) {
    val items = listOf(null to "Toutes", "PUBLIEE" to "Publiees", "BROUILLON" to "Brouillons", "ARCHIVEE" to "Archivees")
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        items.forEach { (status, label) ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun InstructorFormationCard(
    formation: InstructorFormationSummary,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer la formation") },
            text = { Text("Cette action supprimera la formation et son programme. Continuer ?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("Supprimer", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = Radius.panel,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(132.dp)) {
                AsyncImage(
                    model = formation.coverImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(Spacing.sm),
                    shape = Radius.full,
                    color = statusColor(formation.status)
                ) {
                    Text(
                        text = statusLabel(formation.status),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Modifier") }, onClick = {
                        menuExpanded = false
                        onEdit()
                    })
                    DropdownMenuItem(text = { Text("Apercu") }, onClick = {
                        menuExpanded = false
                        onPreview()
                    })
                    DropdownMenuItem(text = { Text("Archiver") }, onClick = {
                        menuExpanded = false
                        onArchive()
                    })
                    DropdownMenuItem(text = { Text("Supprimer") }, onClick = {
                        menuExpanded = false
                        showDeleteConfirm = true
                    })
                }
            }
            Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = formation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = ELearningColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ELearningColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formation.coursesCount} cours - ${formation.seancesCount} seances - ${formation.enrolledCount} apprenants",
                    style = MaterialTheme.typography.labelMedium,
                    color = ELearningColors.TextTertiary
                )
            }
        }
    }
}

private fun statusColor(status: String): Color = when (status) {
    "PUBLIEE" -> Color(0xFF4CAF50)
    "BROUILLON" -> Color(0xFFFF9800)
    else -> Color(0xFF9E9E9E)
}

private fun statusLabel(status: String): String = when (status) {
    "PUBLIEE" -> "Publiee"
    "BROUILLON" -> "Brouillon"
    "ARCHIVEE" -> "Archivee"
    else -> status
}
