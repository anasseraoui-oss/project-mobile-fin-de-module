package com.elearning.app.presentation.catalogue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.elearning.app.domain.model.FormationLevel
import com.elearning.app.presentation.components.*
import com.elearning.app.presentation.theme.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogueScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: CatalogueViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val formations = viewModel.formationsFlow.collectAsLazyPagingItems()
    val gridState = rememberLazyGridState()

    val isRefreshing = formations.loadState.refresh is LoadState.Loading
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catalogue") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { formations.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    placeholder = { Text("Rechercher une formation...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )

                // Filters
                val levels = FormationLevel.values().toList()
                val chips = levels.map { it.name to (it == selectedLevel) }
                ChipFiltreRow(
                    chips = chips,
                    onChipClick = { index -> viewModel.updateLevelFilter(levels[index]) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Spacing.lg, end = Spacing.lg, bottom = Spacing.md)
                )

                // Paging State Handling
                when (val refreshState = formations.loadState.refresh) {
                    is LoadState.Loading -> {
                        if (!isRefreshing) FullScreenLoader()
                    }
                    is LoadState.Error -> {
                        ErrorState(
                            message = refreshState.error.localizedMessage ?: "Erreur réseau",
                            onRetry = { formations.retry() }
                        )
                    }
                    is LoadState.NotLoading -> {
                        if (formations.itemCount == 0) {
                            EmptyState(message = "Aucune formation trouvée pour cette recherche.")
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 300.dp),
                                state = gridState,
                                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(formations.itemCount) { index ->
                                    formations[index]?.let { formation ->
                                        FormationCard(
                                            formation = formation,
                                            onClick = { onNavigateToDetail(formation.id.toString()) }
                                        )
                                    }
                                }

                                // Handling pagination append loading
                                if (formations.loadState.append is LoadState.Loading) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
