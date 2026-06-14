package com.controlprestamos.core.design

import androidx.compose.runtime.Composable
import com.controlprestamos.core.ui.theme.ControlPrestamosTheme as OfficialControlPrestamosTheme

@Composable
fun ControlPrestamosTheme(
    content: @Composable () -> Unit
) {
    OfficialControlPrestamosTheme(content = content)
}
