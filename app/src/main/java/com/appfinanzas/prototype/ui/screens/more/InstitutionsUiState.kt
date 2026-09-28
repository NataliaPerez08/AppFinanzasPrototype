package com.appfinanzas.prototype.ui.screens.more

import com.appfinanzas.prototype.domain.model.Institution

data class InstitutionsUiState(
    val isLoading: Boolean = true,
    val institutions: List<Institution> = emptyList(),
    val nameText: String = "",
    val kindText: String = "",
    val showAddForm: Boolean = false,
    val isSubmitting: Boolean = false,
    val formError: String? = null,
    val error: String? = null,
)