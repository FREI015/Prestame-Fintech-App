package com.controlprestamos.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing

@Composable
fun QuickActionButton(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp),
        color = AppColors.PrimaryDark,
        contentColor = AppColors.Surface,
        shape = RoundedCornerShape(AppRadius.card)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = AppSpacing.xs,
                alignment = Alignment.CenterVertically
            )
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                tint = AppColors.Surface
            )

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Surface
            )
        }
    }
}
