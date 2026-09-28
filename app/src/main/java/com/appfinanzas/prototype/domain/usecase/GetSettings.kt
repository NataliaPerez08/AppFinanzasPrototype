package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow

class GetSettings(
    private val repository: InvestmentRepository,
) {
    fun observe(): Flow<AppSettings> = repository.observeSettings()
}