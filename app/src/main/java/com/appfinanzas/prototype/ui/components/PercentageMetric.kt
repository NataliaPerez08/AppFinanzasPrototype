package com.appfinanzas.prototype.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.format.PercentageFormatter
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun PercentageMetric(
    label: String,
    percentage: Double,
    modifier: Modifier = Modifier,
    includeSign: Boolean = true,
) {
    val color = when {
        PercentageFormatter.isPositive(percentage) -> FinanzasColors.Accent
        PercentageFormatter.isNegative(percentage) -> FinanzasColors.Text
        else -> FinanzasColors.Text.copy(alpha = 0.65f)
    }
    Column(modifier.padding(vertical = 5.dp)) {
        FinanceLabel(label)
        Text(
            text = PercentageFormatter.format(percentage, includeSign),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}