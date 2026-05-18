package com.elearning.app.presentation.prof

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elearning.app.presentation.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFormationScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateFormationViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val price by viewModel.price.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer une Formation") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState) {
                is CreateFormationState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(80.dp))
                        Spacer(Modifier.height(Spacing.lg))
                        Text("Formation créée avec succès !", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(Spacing.xl))
                        Button(onClick = onNavigateBack) { Text("Retour au tableau de bord") }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(Spacing.lg)
                    ) {
                        if (uiState is CreateFormationState.Error) {
                            Text(
                                text = (uiState as CreateFormationState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = Spacing.md)
                            )
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = viewModel::updateTitle,
                            label = { Text("Titre de la formation") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(Spacing.md))

                        OutlinedTextField(
                            value = description,
                            onValueChange = viewModel::updateDescription,
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            maxLines = 5
                        )
                        Spacer(Modifier.height(Spacing.md))

                        OutlinedTextField(
                            value = price,
                            onValueChange = viewModel::updatePrice,
                            label = { Text("Prix (0 pour gratuit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(Spacing.xl))

                        Button(
                            onClick = viewModel::submitFormation,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = uiState !is CreateFormationState.Loading
                        ) {
                            if (uiState is CreateFormationState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Publier la formation")
                            }
                        }
                    }
                }
            }
        }
    }
}
