package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.repository.InvestmentRepository

class SaveSettings(
    private val repository: InvestmentRepository,
) {
    suspend fun execute(settings: AppSettings) {
        repository.saveSettings(settings)
    }
}