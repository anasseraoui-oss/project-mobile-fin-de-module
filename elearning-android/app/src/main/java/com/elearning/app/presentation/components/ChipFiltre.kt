package com.elearning.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elearning.app.presentation.theme.Spacing

/**
 * ChipFiltre — FilterChip with optional leading icon and animated color transition.
 *
 * Used in CatalogueScreen for level / language / organisation filters.
 *
 * Edge cases:
 *  - No icon → renders without leading icon slot
 *  - Long label → single line, ellipsis truncation handled by FilterChip internally
 *  - Disabled state → reduced alpha via [enabled] parameter
 */
@Composable
fun ChipFiltre(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "chip_color"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "chip_label_color"
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = labelColor
            )
        },
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = labelColor
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLabelColor = MaterialTheme.colorScheme.primary,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            borderWidth = 1.dp,
            selectedBorderWidth = 1.5.dp
        ),
        modifier = modifier.semantics {
            contentDescription = "$label ${if (selected) "sélectionné" else "non sélectionné"}"
        }
    )
}

/**
 * ChipFiltreRow — Horizontal scrollable row of filter chips.
 *
 * Usage: pass a list of (label, isSelected) pairs and a unified callback.
 */
@Composable
fun ChipFiltreRow(
    chips: List<Pair<String, Boolean>>,
    onChipClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcons: List<ImageVector?> = emptyList()
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        chips.forEachIndexed { index, (label, isSelected) ->
            ChipFiltre(
                label = label,
                selected = isSelected,
                onClick = { onChipClick(index) },
                leadingIcon = leadingIcons.getOrNull(index)
            )
        }
    }
}
