package com.appfinanzas.prototype.domain.repository

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

    suspend fun saveInvestment(investment: Investment): Long
    suspend fun updateInvestment(investment: Investment)
    suspend fun saveTransaction(transaction: Transaction): Long
}