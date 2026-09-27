package com.appfinanzas.prototype.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun PrimaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    Column(modifier) {
        FinanceLabel(label)
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium,
            color = if (accent) FinanzasColors.Accent else FinanzasColors.Text,
        )
    }
}