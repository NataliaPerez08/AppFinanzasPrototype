package com.appfinanzas.prototype.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.appfinanzas.prototype.ui.theme.FinanzasColors

@Composable
fun PortfolioChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = FinanzasColors.Accent,
    strokeWidth: Float = 4f,
    contentDescription: String? = null,
) {
    Canvas(
        modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(vertical = 12.dp)
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
    ) {
        if (points.size < 2) return@Canvas
        val path = Path()
        points.forEachIndexed { index, v ->
            val x = index * size.width / (points.size - 1)
            val y = v.coerceIn(0f, 1f) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(strokeWidth))
    }
}