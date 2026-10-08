package com.appfinanzas.prototype.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun PinDots(
    filled: Int,
    modifier: Modifier = Modifier,
    total: Int = 4,
) {
    Row(
        modifier = modifier.semantics {
            contentDescription = "$filled de $total dígitos introducidos"
        },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (index < filled) FinanzasColors.Accent else FinanzasColors.Surface)
                    .border(1.dp, FinanzasColors.Divider, CircleShape),
            )
        }
    }
}

@Composable
fun PinKeypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    onBiometric: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
    )
    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { digit ->
                    PinKey(
                        label = digit,
                        modifier = Modifier.weight(1f),
                        enabled = enabled,
                        onClick = { onDigit(digit.first()) },
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (onBiometric != null) {
                PinKey(
                    label = "BIO",
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    onClick = onBiometric,
                )
            } else {
                Box(Modifier.weight(1f))
            }
            PinKey(
                label = "0",
                modifier = Modifier.weight(1f),
                enabled = enabled,
                onClick = { onDigit('0') },
            )
            PinKey(
                label = "←",
                modifier = Modifier.weight(1f),
                enabled = enabled,
                onClick = onBackspace,
            )
        }
    }
}

@Composable
private fun PinKey(
    label: String,
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .aspectRatio(1.6f)
            .border(1.dp, FinanzasColors.Divider)
            .background(FinanzasColors.Surface)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (enabled) FinanzasColors.Text else FinanzasColors.Text.copy(alpha = 0.4f),
        )
    }
}
