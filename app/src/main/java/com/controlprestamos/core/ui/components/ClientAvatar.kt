package com.controlprestamos.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.theme.AppColors

@Composable
fun ClientAvatar(
    fullName: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(AppColors.AccentTeal.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = buildInitials(fullName),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.AccentTeal
        )
    }
}

private fun buildInitials(fullName: String): String {
    val parts = fullName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }

    if (parts.isEmpty()) return "?"

    return parts
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
}
