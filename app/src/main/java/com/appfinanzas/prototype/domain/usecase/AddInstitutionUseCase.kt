package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.repository.InvestmentRepository

sealed class AddInstitutionResult {
    data class Success(val institutionId: Long) : AddInstitutionResult()
    data class Error(val message: String) : AddInstitutionResult()
}

class AddInstitutionUseCase(
    private val repository: InvestmentRepository,
) {
    suspend fun execute(name: String, kind: String): AddInstitutionResult {
        val trimmedName = name.trim()
        val trimmedKind = kind.trim()
        if (trimmedName.isBlank()) return AddInstitutionResult.Error("El nombre es obligatorio")
        if (trimmedKind.isBlank()) return AddInstitutionResult.Error("El tipo es obligatorio")
        val id = repository.addInstitution(
            Institution(id = 0, name = trimmedName, kind = trimmedKind),
        )
        return AddInstitutionResult.Success(id)
    }
}