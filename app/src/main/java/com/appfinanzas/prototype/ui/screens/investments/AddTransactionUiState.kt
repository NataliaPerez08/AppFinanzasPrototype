package com.appfinanzas.prototype.ui.screens.investments

import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.ui.format.DateFormatter
import java.time.LocalDate

data class AddTransactionUiState(
    val investmentHeader: InvestmentHeaderUi? = null,
    val type: TransactionType? = null,
    val dateText: String = DateFormatter.format(LocalDate.now()),
    val quantityText: String = "",
    val priceText: String = "",
    val commissionText: String = "",
    val currency: Currency = Currency.MXN,
    val total: Double = 0.0,
    val fieldErrors: Map<String, String> = emptyMap(),
    val formError: String? = null,
    val isSubmitting: Boolean = false,
)

data class InvestmentHeaderUi(
    val name: String,
    val subtitle: String,
)

internal fun Investment.toHeaderUi(): InvestmentHeaderUi =
    InvestmentHeaderUi(
        name = name,
        subtitle = "${institution.name} · ${type.category.label}",
    )