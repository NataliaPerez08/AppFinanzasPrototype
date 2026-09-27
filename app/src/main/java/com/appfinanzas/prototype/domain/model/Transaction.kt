package com.appfinanzas.prototype.domain.model

import java.time.LocalDate

data class Transaction(
    val id: Long,
    val investmentId: Long,
    val type: TransactionType,
    val date: LocalDate,
    val quantity: Double,
    val price: Double,
    val commission: Double,
    val total: Double,
    val currency: Currency,
)