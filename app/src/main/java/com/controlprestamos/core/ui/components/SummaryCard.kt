package com.controlprestamos.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null
) {
    AppCard(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconRes != null) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = AppColors.AccentTeal.copy(alpha = 0.10f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = AppColors.AccentTeal
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Gray600
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Gray600
                )
            }
        }
    }
}
