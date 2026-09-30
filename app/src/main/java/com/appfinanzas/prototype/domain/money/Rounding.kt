package com.appfinanzas.prototype.domain.money

import java.math.BigDecimal
import java.math.RoundingMode

object Rounding {

    private const val MONEY_SCALE = 2
    private const val PERCENT_SCALE = 4

    fun money(value: Double): Double {
        if (value.isNaN() || value.isInfinite()) return 0.0
        return BigDecimal(value).setScale(MONEY_SCALE, RoundingMode.HALF_UP).toDouble()
    }

    fun percentage(value: Double): Double {
        if (value.isNaN() || value.isInfinite()) return 0.0
        return BigDecimal(value).setScale(PERCENT_SCALE, RoundingMode.HALF_UP).toDouble()
    }
}
