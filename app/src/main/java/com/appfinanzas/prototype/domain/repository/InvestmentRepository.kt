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

    suspend fun getInvestment(investmentId: Long): Investment?
    suspend fun getAllInvestments(): List<Investment>
    suspend fun getTransaction(transactionId: Long): Transaction?
    suspend fun getTransactions(investmentId: Long): List<Transaction>

    suspend fun saveInvestment(investment: Investment): Long
    suspend fun saveInvestmentWithOpeningTransaction(investment: Investment, openingTransaction: Transaction): Long
    suspend fun updateInvestment(investment: Investment)
    suspend fun deleteInvestment(investmentId: Long)

    suspend fun saveTransaction(transaction: Transaction): Long
    suspend fun saveTransactionWithInvestment(transaction: Transaction, investment: Investment): Long
    suspend fun updateTransactionWithInvestment(transaction: Transaction, investment: Investment)
    suspend fun deleteTransactionWithInvestment(transactionId: Long, investment: Investment)

    suspend fun saveSettings(settings: AppSettings)
    suspend fun addInstitution(institution: Institution): Long
    suspend fun repairLedgerIfNeeded()
}
