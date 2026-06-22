package com.elearning.app.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.FormationCategory
import java.util.Locale
import com.elearning.app.presentation.components.EmptyState
import com.elearning.app.presentation.components.ErrorState
import com.elearning.app.presentation.components.FullScreenLoader
import com.elearning.app.presentation.theme.ELearningColors
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.FigmaSpacing
import com.elearning.app.presentation.theme.Radius
import com.elearning.app.presentation.theme.AnimDuration

@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCatalogue: (String?) -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is HomeUiState.Loading -> FullScreenLoader()
        is HomeUiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.loadHomeData() })
        is HomeUiState.Success -> {
            HomeContent(
                formations = state.recommendedFormations,
                categories = state.categories,
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToCatalogue = onNavigateToCatalogue,
                onNavigateToNotifications = onNavigateToNotifications,
                modifier = modifier
            )
        }
    }
}

@Composable
fun HomeContent(
    formations: List<Formation>,
    categories: List<FormationCategory>,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCatalogue: (String?) -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCategoriesSheet by remember { mutableStateOf(false) }

    if (showCategoriesSheet) {
        CategoriesBottomSheet(
            categories = categories,
            onDismiss = { showCategoriesSheet = false },
            onCategoryClick = { categoryId ->
                showCategoriesSheet = false
                onNavigateToCatalogue(categoryId)
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(FigmaSpacing.sectionGap)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = FigmaSpacing.pageHorizontal,
                        end = FigmaSpacing.pageHorizontal,
                        top = 44.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                HomeHeader(
                    userName = null,
                    notificationCount = 0,
                    onNotificationsClick = onNavigateToNotifications
                )
                SearchFilterBar()
                LearningHeroBanner(onExploreClick = {
                    formations.firstOrNull()?.let { onNavigateToDetail(it.id.toString()) }
                })
            }
        }

        item {
            HomeSectionHeader(
                title = "Categories populaires",
                action = "Voir tout",
                onActionClick = { showCategoriesSheet = true }
            )
            if (categories.isEmpty()) {
                HomeInlineEmptyState(
                    message = "Categories bientot disponibles.",
                    icon = Icons.Default.Category
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = FigmaSpacing.pageHorizontal),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categories) { category ->
                        CategoryShortcut(
                            category = category.toHomeCategory(),
                            onClick = { onNavigateToCatalogue(category.id) }
                        )
                    }
                }
            }
        }

        if (formations.isNotEmpty()) {
            item {
                HomeSectionHeader(
                    title = "Cours recommandes",
                    action = "Voir tout",
                    onActionClick = { onNavigateToCatalogue(null) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = FigmaSpacing.pageHorizontal),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(formations) { formation ->
                        RecommendedCourseCard(
                            formation = formation,
                            onClick = { onNavigateToDetail(formation.id.toString()) }
                        )
                    }
                }
            }
        } else {
            item {
                HomeSectionHeader(
                    title = "Cours recommandes",
                    action = "Voir tout",
                    onActionClick = { onNavigateToCatalogue(null) }
                )
                HomeInlineEmptyState(
                    message = "Aucune formation disponible pour le moment."
                )
            }
        }

        item {
            HomeSectionHeader(title = "Reprendre", action = "Voir tout")
            HomeInlineEmptyState(
                message = "Aucune seance en cours a reprendre."
            )
        }
    }
}

@Composable
fun HomeHeader(
    userName: String?,
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Bonjour",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = ELearningColors.TextPrimary
            )
            Text(
                text = userName ?: "Bienvenue",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = ELearningColors.TextPrimary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                Surface(
                    onClick = onNotificationsClick,
                    shape = CircleShape,
                    color = ELearningColors.CardSurface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = ELearningColors.TextSecondary)
                    }
                }
                if (notificationCount > 0) {
                    Badge(
                        containerColor = Color(0xFFEF4444),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(notificationCount.toString())
                    }
                }
            }
            Surface(shape = CircleShape, color = Color(0xFFE0E7FF), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = "Profil", tint = ELearningColors.BrandBlue)
                }
            }
        }
    }
}

@Composable
fun SearchFilterBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = Radius.search,
            color = ELearningColors.CardSurface,
            shadowElevation = 1.dp,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = ELearningColors.TextTertiary)
                Text(
                    text = "Rechercher un cours...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = ELearningColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Surface(
            shape = Radius.search,
            color = ELearningColors.CardSurface,
            shadowElevation = 1.dp,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Tune, contentDescription = "Filtres", tint = ELearningColors.TextSecondary)
            }
        }
    }
}

@Composable
fun LearningHeroBanner(onExploreClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(176.dp)
            .clip(Radius.xl)
            .background(Brush.linearGradient(listOf(ELearningColors.BrandBlue, ELearningColors.BrandBlueDark)))
            .padding(FigmaSpacing.heroPadding)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.68f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Continuez d'apprendre",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.82f)
            )
            Text(
                text = "Investissez en vous, construisez votre avenir",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = MaterialTheme.typography.titleLarge.lineHeight
            )
            Button(
                onClick = onExploreClick,
                shape = Radius.md,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = ELearningColors.BrandBlue
                )
            ) {
                Text("Explorer", fontWeight = FontWeight.Bold)
            }
        }
        Icon(
            Icons.Default.School,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.18f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(112.dp)
        )
    }
}

@Composable
fun CategoryShortcut(
    category: HomeCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(78.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = Radius.xl,
            color = category.background,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(30.dp))
            }
        }
        Text(
            text = category.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = ELearningColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RecommendedCourseCard(
    formation: Formation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(240.dp)
            .clickable(onClick = onClick),
        shape = Radius.xl,
        colors = CardDefaults.cardColors(containerColor = ELearningColors.CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(Radius.lg)
                    .background(Color(0xFFE5E7EB))
            ) {
                AsyncImage(
                    model = formation.thumbnailUrl,
                    contentDescription = formation.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    shape = Radius.sm,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "BESTSELLER",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ELearningColors.TextPrimary
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.22f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favori", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = formation.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ELearningColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formation.organisation,
                style = MaterialTheme.typography.labelMedium,
                color = ELearningColors.TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = String.format(Locale.getDefault(), "%.1f", formation.rating),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF97316)
                )
                Text(
                    text = " (${formation.enrollmentCount})",
                    style = MaterialTheme.typography.labelMedium,
                    color = ELearningColors.TextTertiary
                )
            }
        }
    }
}

@Composable
fun ResumeLearningCard(
    title: String,
    subtitle: String,
    progress: Float,
    thumbnailUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(AnimDuration.xSlow),
        label = "resumeLearningProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(2.dp, Radius.xl),
        shape = Radius.xl,
        colors = CardDefaults.cardColors(containerColor = ELearningColors.CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 100.dp, height = 75.dp)
                    .clip(Radius.lg)
                    .background(Color(0xFFE5E7EB))
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = ELearningColors.TextTertiary)
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = ELearningColors.BrandBlue,
                    trackColor = Color(0xFFE5E7EB),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )
            }
            Surface(shape = CircleShape, color = Color(0xFFEFF6FF), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Continuer", tint = ELearningColors.BrandBlue)
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    action: String,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FigmaSpacing.pageHorizontal),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ELearningColors.TextPrimary)
        Text(
            text = action,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = ELearningColors.BrandBlue,
            modifier = Modifier.clickable(enabled = onActionClick != null) {
                onActionClick?.invoke()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoriesBottomSheet(
    categories: List<FormationCategory>,
    onDismiss: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ELearningColors.CardSurface,
        shape = Radius.dialog
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FigmaSpacing.pageHorizontal, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Toutes les categories",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = ELearningColors.TextPrimary
            )
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                categories.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        row.forEach { category ->
                            CategoryGridItem(
                                category = category.toHomeCategory(),
                                count = category.formationsCount,
                                onClick = { onCategoryClick(category.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CategoryGridItem(
    category: HomeCategory,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(112.dp),
        shape = Radius.xl,
        color = category.background
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = ELearningColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$count cours",
                style = MaterialTheme.typography.labelSmall,
                color = ELearningColors.TextTertiary
            )
        }
    }
}

@Composable
private fun HomeInlineEmptyState(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Info,
    modifier: Modifier = Modifier
) {
    EmptyState(
        message = message,
        icon = icon,
        modifier = modifier
            .padding(horizontal = FigmaSpacing.pageHorizontal)
            .fillMaxWidth()
            .height(136.dp)
    )
}

data class HomeCategory(
    val id: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val background: Color,
    val color: Color
)

private fun FormationCategory.toHomeCategory(): HomeCategory {
    val (icon, background, color) = when (id) {
        "backend" -> Triple(Icons.Default.Storage, Color(0xFFEFF6FF), ELearningColors.BrandBlue)
        "devops" -> Triple(Icons.Default.DeveloperBoard, Color(0xFFFFF7ED), Color(0xFFF97316))
        "mobile" -> Triple(Icons.Default.PhoneAndroid, Color(0xFFECFDF5), Color(0xFF16A34A))
        "frontend" -> Triple(Icons.Default.Dashboard, Color(0xFFF5F3FF), Color(0xFF7C3AED))
        "cloud" -> Triple(Icons.Default.Cloud, Color(0xFFE0F2FE), Color(0xFF0284C7))
        else -> Triple(Icons.Default.Category, Color(0xFFF3F4F6), ELearningColors.TextSecondary)
    }
    return HomeCategory(
        id = id,
        title = title,
        icon = icon,
        background = background,
        color = color
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ELearningTheme {
        HomeContent(
            formations = emptyList(),
            categories = emptyList(),
            onNavigateToDetail = {},
            onNavigateToCatalogue = {},
            onNavigateToNotifications = {}
        )
    }
}
