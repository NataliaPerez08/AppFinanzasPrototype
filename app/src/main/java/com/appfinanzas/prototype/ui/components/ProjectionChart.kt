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

data class ProjectionChartBand(
    val low: List<Float>,
    val mid: List<Float>,
    val high: List<Float>,
)

@Composable
fun ProjectionChart(
    band: ProjectionChartBand,
    modifier: Modifier = Modifier,
    color: Color = FinanzasColors.Accent,
    contentDescription: String? = null,
) {
    Canvas(
        modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(vertical = 12.dp)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            ),
    ) {
        val size = band.low.size
        if (size < 2 || band.high.size != size || band.mid.size != size) return@Canvas

        val min = (band.low + band.high).minOrNull() ?: 0f
        val max = (band.low + band.high).maxOrNull() ?: 1f
        val range = if (max > min) max - min else 1f

        fun x(index: Int) = index * this.size.width / (size - 1).toFloat()
        fun y(value: Float) = (1f - (value - min) / range) * this.size.height

        val fill = Path().apply {
            band.high.forEachIndexed { index, value ->
                if (index == 0) moveTo(x(index), y(value)) else lineTo(x(index), y(value))
            }
            for (index in band.low.indices.reversed()) {
                lineTo(x(index), y(band.low[index]))
            }
            close()
        }
        drawPath(fill, color.copy(alpha = 0.18f))

        val median = Path().apply {
            band.mid.forEachIndexed { index, value ->
                if (index == 0) moveTo(x(index), y(value)) else lineTo(x(index), y(value))
            }
        }
        drawPath(median, color, style = Stroke(4f))
    }
}
