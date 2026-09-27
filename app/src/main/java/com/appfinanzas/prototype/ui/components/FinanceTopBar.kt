package com.appfinanzas.prototype.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun FinanceTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.headlineMedium,
        color = FinanzasColors.Text,
        modifier = modifier,
    )
    if (subtitle.isNotBlank()) {
        Text(
            text = subtitle.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = FinanzasColors.Text.copy(alpha = 0.65f),
        )
        Spacer(Modifier.height(12.dp))
    }
}