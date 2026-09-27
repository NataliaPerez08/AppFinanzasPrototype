package com.appfinanzas.prototype.ui.format

import java.util.Locale

object PercentageFormatter {
    fun format(percentage: Double, includeSign: Boolean = true): String {
        val sign = when {
            percentage > 0 && includeSign -> "+"
            percentage < 0 -> "-"
            else -> ""
        }
        val value = String.format(Locale.US, "%.1f", kotlin.math.abs(percentage))
        return "$sign$value%"
    }

    fun isPositive(percentage: Double): Boolean = percentage > 0

    fun isNegative(percentage: Double): Boolean = percentage < 0

    fun isZero(percentage: Double): Boolean = percentage == 0.0
}