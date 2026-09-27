package com.appfinanzas.prototype.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
internal fun FinanceLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = FinanzasColors.Text.copy(alpha = 0.65f),
        modifier = modifier,
    )
}