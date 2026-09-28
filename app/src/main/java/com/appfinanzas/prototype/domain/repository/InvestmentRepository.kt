package com.appfinanzas.prototype.domain.repository

import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface InvestmentRepository {
    fun observeInvestments(): Flow<List<Investment>>
    fun observeInvestment(investmentId: Long): Flow<Investment?>
    fun observeTransactions(investmentId: Long): Flow<List<Transaction>>
    fun observePortfolioHistory(): Flow<List<Float>>
    fun observeInstitutions(): Flow<List<Institution>>
    fun observeSettings(): Flow<AppSettings>

    suspend fun saveInvestment(investment: Investment): Long
    suspend fun updateInvestment(investment: Investment)
    suspend fun saveTransaction(transaction: Transaction): Long
    suspend fun saveSettings(settings: AppSettings)
    suspend fun addInstitution(institution: Institution): Long
}