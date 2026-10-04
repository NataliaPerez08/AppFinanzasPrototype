package com.appfinanzas.prototype.ui.screens.more

import com.appfinanzas.prototype.domain.model.Currency

data class SettingsUiState(
    val isLoading: Boolean = true,
    val baseCurrency: Currency = Currency.MXN,
    val usdToMxnRateText: String = "",
    val inflationText: String = "",
    val isrText: String = "",
    val expectedReturnText: String = "",
    val isSubmitting: Boolean = false,
    val saved: Boolean = false,
    val formError: String? = null,
    val error: String? = null,
)
