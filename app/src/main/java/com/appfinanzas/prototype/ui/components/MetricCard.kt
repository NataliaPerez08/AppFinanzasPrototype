package com.appfinanzas.prototype.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    Column(modifier.padding(vertical = 5.dp)) {
        FinanceLabel(label)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = if (accent) FinanzasColors.Accent else FinanzasColors.Text,
        )
    }
}