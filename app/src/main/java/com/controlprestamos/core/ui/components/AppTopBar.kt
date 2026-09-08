package com.controlprestamos.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing

@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    showMenu: Boolean = false,
    showNotifications: Boolean = false,
    showBack: Boolean = false,
    showMore: Boolean = false,
    onMenu: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onBack: () -> Unit = {},
    onMore: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    val hasSubtitle = !subtitle.isNullOrBlank()

    val contentHeight = if (hasSubtitle) {
        72.dp
    } else {
        64.dp
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.PrimaryDark,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(contentHeight)
                .padding(
                    horizontal = AppSpacing.md,
                    vertical = AppSpacing.xs
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    showBack -> {
                        TopBarAction(
                            text = "←",
                            contentDescription = "Volver",
                            onClick = onBack
                        )
                    }

                    showMore -> {
                        TopBarAction(
                            text = "⋯",
                            contentDescription = "Más opciones",
                            onClick = onMore
                        )
                    }

                    showMenu -> {
                        TopBarAction(
                            text = "☰",
                            contentDescription = "Menú",
                            onClick = onMenu
                        )
                    }
                }

                if (showBack || showMore || showMenu) {
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (hasSubtitle) {
                        Text(
                            text = subtitle.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                if (showNotifications) {
                    NotificationAction(
                        onClick = onNotifications
                    )
                }

                actions()
            }
        }
    }
}

@Composable
private fun TopBarAction(
    text: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .semantics {
                this.contentDescription = contentDescription
            }
            .clickable(
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun NotificationAction(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .semantics {
                this.contentDescription = "Notificaciones"
            }
            .clickable(
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🔔",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(AppColors.Warning)
        )
    }
}

