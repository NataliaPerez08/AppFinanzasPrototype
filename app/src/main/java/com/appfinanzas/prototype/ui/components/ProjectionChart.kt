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

data class ProjectionSeries(
    val points: List<Float>,
    val color: Color,
)

@Composable
fun ProjectionChart(
    series: List<ProjectionSeries>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 3f,
    contentDescription: String? = null,
) {
    Canvas(
        modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(vertical = 12.dp)
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
    ) {
        series.forEach { s ->
            if (s.points.size < 2) return@forEach
            val path = Path()
            s.points.forEachIndexed { index, v ->
                val x = index * size.width / (s.points.size - 1)
                val y = v.coerceIn(0f, 1f) * size.height
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, s.color, style = Stroke(strokeWidth))
        }
    }
}