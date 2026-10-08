package com.appfinanzas.prototype.domain.model

import java.time.LocalDate

data class PricePoint(
    val id: Long = 0,
    val investmentId: Long,
    val date: LocalDate,
    val price: Double,
)
