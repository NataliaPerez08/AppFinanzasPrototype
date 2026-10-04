package com.appfinanzas.prototype.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.format.PercentageFormatter
import com.appfinanzas.prototype.ui.theme.FinanzasColors

data class AllocationItem(
    val label: String,
    val percentage: Float,
)

@Composable
fun AllocationBar(
    items: List<AllocationItem>,
    modifier: Modifier = Modifier,
) {
    val visibleItems = items.filter { it.percentage.isFinite() && it.percentage > 0f }
    val total = visibleItems.sumOf { it.percentage.toDouble() }.toFloat()
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(18.dp)) {
            visibleItems.forEachIndexed { index, item ->
                val weight = item.percentage / total
                Box(
                    Modifier
                        .weight(weight)
                        .height(18.dp)
                        .background(
                            when (index) {
                                0 -> FinanzasColors.Accent
                                1 -> FinanzasColors.Text.copy(alpha = 0.80f)
                                else -> FinanzasColors.Text.copy(alpha = 0.55f)
                            },
                        ),
                )
            }
        }
        visibleItems.forEach { item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                FinanceLabel(item.label)
                Spacer(Modifier.weight(1f))
                Text(
                    text = PercentageFormatter.format(item.percentage.toDouble(), includeSign = false),
                    style = MaterialTheme.typography.labelMedium,
                    color = FinanzasColors.Text,
                )
            }
        }
    }
}
