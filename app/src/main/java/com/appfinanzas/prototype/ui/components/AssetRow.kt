package com.appfinanzas.prototype.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun AssetRow(
    name: String,
    subtitle: String,
    value: String,
    change: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, FinanzasColors.Divider)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                color = FinanzasColors.Text,
            )
            FinanceLabel(subtitle)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = FinanzasColors.Text,
            )
            Text(
                text = change,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = FinanzasColors.Accent,
            )
        }
    }
}