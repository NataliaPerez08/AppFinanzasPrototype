package com.appfinanzas.prototype.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = FinanzasColors.Accent,
    onPrimary = FinanzasColors.OnAccent,
    background = FinanzasColors.Background,
    onBackground = FinanzasColors.Text,
    surface = FinanzasColors.Surface,
    onSurface = FinanzasColors.Text,
    onSurfaceVariant = FinanzasColors.Text,
    outline = FinanzasColors.Divider,
)

@Composable
fun FinanzasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = FinanzasTypography,
        content = content,
    )
}