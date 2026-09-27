package com.appfinanzas.prototype.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.ui.format.MoneyFormatter
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun MoneyMetric(
    label: String,
    amount: Double,
    modifier: Modifier = Modifier,
    currency: Currency = Currency.MXN,
    accent: Boolean = false,
) {
    Column(modifier.padding(vertical = 5.dp)) {
        FinanceLabel(label)
        Text(
            text = MoneyFormatter.format(amount, currency),
            style = MaterialTheme.typography.titleMedium,
            color = if (accent) FinanzasColors.Accent else FinanzasColors.Text,
        )
    }
}