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
fun FinanceLoadingState(
    modifier: Modifier = Modifier,
    text: String = "CARGANDO…",
) {
    FinancePanel(modifier) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = FinanzasColors.Text.copy(alpha = 0.65f),
        )
    }
}

@Composable
fun FinanceEmptyState(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FinancePanel(modifier) {
        SectionHeader(title)
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = FinanzasColors.Text,
        )
        Spacer(Modifier.height(12.dp))
        FinanceButton(text = actionLabel, onClick = onAction)
    }
}

@Composable
fun FinanceErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FinancePanel(modifier) {
        SectionHeader("ERROR")
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = FinanzasColors.Text,
        )
        Spacer(Modifier.height(12.dp))
        FinanceButton(text = "REINTENTAR", onClick = onRetry)
    }
}

@Composable
fun FinanceErrorText(
    message: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelMedium,
        color = FinanzasColors.Accent,
        modifier = modifier,
    )
}
