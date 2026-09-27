package com.appfinanzas.prototype.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun InstitutionRow(
    name: String,
    detail: String,
    status: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, FinanzasColors.Divider)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                color = FinanzasColors.Text,
            )
            FinanceLabel(detail)
        }
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = if (status == "ACTIVA") FinanzasColors.Accent else FinanzasColors.Text.copy(alpha = 0.65f),
        )
    }
}