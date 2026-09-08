package com.controlprestamos.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing

enum class AppStatus(
    val label: String
) {
    CURRENT("Al día"),
    PENDING("Pendiente"),
    OVERDUE("Vencido"),
    INACTIVE("Inactivo"),
    COMPLETED("Completado"),
    ACTIVE("Activo")
}

private data class StatusStyle(
    val background: Color,
    val content: Color
)

@Composable
fun StatusChip(
    status: AppStatus,
    modifier: Modifier = Modifier
) {
    val style = when (status) {
        AppStatus.CURRENT -> StatusStyle(
            background = AppColors.SuccessSoft,
            content = AppColors.Success
        )

        AppStatus.PENDING -> StatusStyle(
            background = AppColors.WarningSoft,
            content = AppColors.Warning
        )

        AppStatus.OVERDUE -> StatusStyle(
            background = AppColors.ErrorSoft,
            content = AppColors.Error
        )

        AppStatus.INACTIVE -> StatusStyle(
            background = AppColors.InactiveSoft,
            content = AppColors.Gray600
        )

        AppStatus.COMPLETED -> StatusStyle(
            background = AppColors.SuccessSoft,
            content = AppColors.Success
        )

        AppStatus.ACTIVE -> StatusStyle(
            background = AppColors.InfoSoft,
            content = AppColors.Info
        )
    }

    Box(
        modifier = modifier
            .background(
                color = style.background,
                shape = RoundedCornerShape(AppRadius.chip)
            )
            .padding(
                horizontal = AppSpacing.sm,
                vertical = AppSpacing.xxs
            )
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = style.content
        )
    }
}

