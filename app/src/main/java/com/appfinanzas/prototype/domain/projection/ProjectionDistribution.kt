package com.appfinanzas.prototype.domain.projection

import kotlin.math.floor

data class ProjectionSeries(
    val months: List<ProjectionDistribution>,
    val hasProbability: Boolean,
) {
    val final: ProjectionDistribution get() = months.last()
}

data class ProjectionDistribution(
    val p10: Double,
    val p25: Double,
    val p50: Double,
    val p75: Double,
    val p90: Double,
    val mean: Double,
) {
    companion object {

        fun of(values: List<Double>): ProjectionDistribution {
            if (values.isEmpty()) return ProjectionDistribution(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            val sorted = values.toDoubleArray().apply { sort() }
            return ProjectionDistribution(
                p10 = percentile(sorted, 0.10),
                p25 = percentile(sorted, 0.25),
                p50 = percentile(sorted, 0.50),
                p75 = percentile(sorted, 0.75),
                p90 = percentile(sorted, 0.90),
                mean = sorted.average(),
            )
        }

        fun percentile(sorted: DoubleArray, p: Double): Double {
            if (sorted.isEmpty()) return 0.0
            if (sorted.size == 1) return sorted[0]
            val rank = p.coerceIn(0.0, 1.0) * (sorted.size - 1)
            val low = floor(rank).toInt()
            val high = (low + 1).coerceAtMost(sorted.size - 1)
            val fraction = rank - low
            return sorted[low] + fraction * (sorted[high] - sorted[low])
        }
    }
}
