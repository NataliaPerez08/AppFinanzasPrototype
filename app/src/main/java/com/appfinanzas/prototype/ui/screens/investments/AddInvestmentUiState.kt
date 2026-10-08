package com.appfinanzas.prototype.ui.screens.investments

import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.ProjectionStrategy
import com.appfinanzas.prototype.ui.format.DateFormatter
import java.time.LocalDate

data class AddInvestmentUiState(
    val isEditing: Boolean = false,
    val type: InvestmentType? = null,
    val institutionId: Long? = null,
    val institutions: List<Institution> = emptyList(),
    val name: String = "",
    val symbol: String = "",
    val currency: Currency? = null,
    val initialValueText: String = "",
    val dateText: String = DateFormatter.format(LocalDate.now()),
    val projectionStrategy: ProjectionStrategy? = null,
    val projectionReturnText: String = "",
    val projectionVolatilityText: String = "",
    val recommendedStrategyLabel: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val formError: String? = null,
    val isSubmitting: Boolean = false,
)