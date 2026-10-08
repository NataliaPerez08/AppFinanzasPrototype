package com.appfinanzas.prototype.domain.projection

import kotlin.math.pow

data class AdjustedProjection(
    val nominal: Double,
    val estimatedTaxes: Double,
    val afterTax: Double,
    val inflationImpact: Double,
    val real: Double,
)

object ProjectionAdjustments {

    // ponytail: flat annual percentage; replace with bracket-aware tax when rates become authoritative
    fun afterTax(nominal: Double, isrPercent: Double, years: Double): Double {
        val base = 1.0 - isrPercent.finiteOrZero() / 100.0
        if (base < 0.0) return 0.0
        return (nominal.finiteOrZero() * base.pow(years)).finiteOrZero()
    }

    fun real(afterTax: Double, inflationPercent: Double, years: Double): Double {
        val base = 1.0 + inflationPercent.finiteOrZero() / 100.0
        if (base <= 0.0) return 0.0
        return (afterTax.finiteOrZero() / base.pow(years)).finiteOrZero()
    }

    fun adjust(
        nominal: Double,
        isrPercent: Double,
        inflationPercent: Double,
        years: Double,
    ): AdjustedProjection {
        val finiteNominal = nominal.finiteOrZero()
        val afterTax = afterTax(finiteNominal, isrPercent, years)
        val real = real(afterTax, inflationPercent, years)
        return AdjustedProjection(
            nominal = finiteNominal,
            estimatedTaxes = (finiteNominal - afterTax).finiteOrZero(),
            afterTax = afterTax,
            inflationImpact = (afterTax - real).finiteOrZero(),
            real = real,
        )
    }
}
