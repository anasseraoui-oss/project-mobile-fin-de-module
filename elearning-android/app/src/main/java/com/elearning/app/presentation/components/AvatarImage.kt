package com.elearning.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.elearning.app.domain.model.UserRole
import com.elearning.app.presentation.theme.ELearningTheme
import com.elearning.app.presentation.theme.Spacing

/**
 * AvatarImage — Circular user avatar using Coil AsyncImage.
 *
 * Fallback strategy (in order):
 *  1. Load [imageUrl] via Coil.
 *  2. On loading → shimmer placeholder.
 *  3. On error or null URL → initials badge (first letter of [name]).
 *
 * Optional [role] badge is overlaid at bottom-right.
 *
 * Edge cases:
 *  - Empty name → shows generic person icon instead of initials.
 *  - Very long name → only first char used.
 *  - imageUrl = null → goes directly to initials fallback.
 */
@Composable
fun AvatarImage(
    name: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    role: UserRole? = null,
    showBorder: Boolean = false,
    borderColor: Color = MaterialTheme.colorScheme.primary
) {
    val initials = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
    val avatarBgColor = MaterialTheme.colorScheme.primaryContainer
    val avatarTextColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .then(
                    if (showBorder) Modifier.border(2.dp, borderColor, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Avatar de $name",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) },
                    error = {
                        InitialsBadge(
                            initials = initials,
                            size = size,
                            bgColor = avatarBgColor,
                            textColor = avatarTextColor
                        )
                    }
                )
            } else {
                InitialsBadge(
                    initials = initials,
                    size = size,
                    bgColor = avatarBgColor,
                    textColor = avatarTextColor
                )
            }
        }

        // Role badge overlay (bottom-right)
        role?.let { userRole ->
            val badgeSize = (size.value * 0.38f).dp
            Surface(
                shape = CircleShape,
                color = userRole.badgeColor(),
                modifier = Modifier
                    .size(badgeSize)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = userRole.badgeIcon(),
                        contentDescription = userRole.name,
                        tint = Color.White,
                        modifier = Modifier.size((badgeSize.value * 0.6f).dp)
                    )
                }
            }
        }
    }
}

// ─── Initials Badge ───────────────────────────────────────────────────────────

@Composable
private fun InitialsBadge(
    initials: String,
    size: Dp,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (initials.isNotBlank()) {
            Text(
                text = initials,
                fontSize = (size.value * 0.38f).sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

// ─── Role extensions ──────────────────────────────────────────────────────────

@Composable
private fun UserRole.badgeColor(): Color = when (this) {
    UserRole.ADMIN      -> MaterialTheme.colorScheme.error
    UserRole.FORMATEUR  -> MaterialTheme.colorScheme.tertiary
    UserRole.APPRENANT  -> MaterialTheme.colorScheme.secondary
}

private fun UserRole.badgeIcon(): ImageVector = when (this) {
    UserRole.ADMIN     -> Icons.Default.AdminPanelSettings
    UserRole.FORMATEUR -> Icons.Default.School
    UserRole.APPRENANT -> Icons.Default.Person
}
