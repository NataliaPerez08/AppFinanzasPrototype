package com.appfinanzas.prototype.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.theme.FinanzasColors

data class FinanceOption(
    val value: String,
    val label: String,
)

@Composable
fun FinanceOptionSelector(
    label: String,
    options: List<FinanceOption>,
    selectedValue: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
    ) {
        FinanceLabel(label)
        Column(
            Modifier
                .fillMaxWidth()
                .border(1.dp, FinanzasColors.Divider)
                .background(FinanzasColors.Surface),
        ) {
            options.forEachIndexed { index, option ->
                val selected = option.value == selectedValue
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (selected) FinanzasColors.Accent.copy(alpha = 0.10f) else FinanzasColors.Surface)
                        .clickable { onSelect(option.value) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = option.label.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) FinanzasColors.Accent else FinanzasColors.Text,
                        modifier = Modifier.weight(1f),
                    )
                    if (selected) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.titleSmall,
                            color = FinanzasColors.Accent,
                        )
                    }
                }
                if (index < options.lastIndex) {
                    HorizontalDivider(color = FinanzasColors.Divider, thickness = 1.dp)
                }
            }
        }
    }
}