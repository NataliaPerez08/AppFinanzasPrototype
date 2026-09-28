package com.appfinanzas.prototype.data

import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestInvestmentRepository(
    investments: List<Investment> = emptyList(),
    private val history: List<Float> = emptyList(),
    transactions: Map<Long, List<Transaction>> = emptyMap(),
    private val institutions: List<Institution> = listOf(
        Institution(id = 1, name = "GBM", kind = "Casa de Bolsa"),
        Institution(id = 2, name = "CETES Directo", kind = "Renta fija"),
    ),
    private val error: Throwable? = null,
) : InvestmentRepository {

    private val investmentState = MutableStateFlow(investments)
    private val transactionState = MutableStateFlow(transactions.toMutableMap())
    private val institutionState = MutableStateFlow(institutions)
    private val settingsState = MutableStateFlow(AppSettings())
    private var nextInvestmentId = (investments.maxOfOrNull { it.id } ?: 0L) + 1
    private var nextTransactionId = (transactions.values.flatten().maxOfOrNull { it.id } ?: 0L) + 1
    private var nextInstitutionId = (institutions.maxOfOrNull { it.id } ?: 0L) + 1

    private fun <T> guarded(inner: Flow<T>): Flow<T> = flow {
        error?.let { throw it }
        emitAll(inner)
    }

    override fun observeInvestments(): Flow<List<Investment>> = guarded(investmentState)

    override fun observeInvestment(investmentId: Long): Flow<Investment?> =
        guarded(investmentState.map { list -> list.firstOrNull { it.id == investmentId } })

    override fun observeTransactions(investmentId: Long): Flow<List<Transaction>> =
        guarded(transactionState.map { it[investmentId] ?: emptyList() })

    override fun observePortfolioHistory(): Flow<List<Float>> = flow {
        error?.let { throw it }
        emit(history)
    }

    override fun observeInstitutions(): Flow<List<Institution>> = guarded(institutionState)

    override fun observeSettings(): Flow<AppSettings> = guarded(settingsState)

    override suspend fun saveInvestment(investment: Investment): Long {
        error?.let { throw it }
        val id = if (investment.id == 0L) nextInvestmentId++ else investment.id
        investmentState.update { it + investment.copy(id = id) }
        return id
    }

    override suspend fun updateInvestment(investment: Investment) {
        error?.let { throw it }
        investmentState.update { list -> list.map { if (it.id == investment.id) investment else it } }
    }

    override suspend fun saveTransaction(transaction: Transaction): Long {
        error?.let { throw it }
        val id = if (transaction.id == 0L) nextTransactionId++ else transaction.id
        val saved = transaction.copy(id = id)
        transactionState.update { state ->
            state.toMutableMap().also { map ->
                map[saved.investmentId] = (map[saved.investmentId] ?: emptyList()) + saved
            }
        }
        return id
    }

    override suspend fun saveSettings(settings: AppSettings) {
        error?.let { throw it }
        settingsState.value = settings
    }

    override suspend fun addInstitution(institution: Institution): Long {
        error?.let { throw it }
        val id = if (institution.id == 0L) nextInstitutionId++ else institution.id
        val saved = institution.copy(id = id)
        institutionState.update { it + saved }
        return id
    }
}

fun sampleInvestment(
    id: Long = 1L,
    name: String = "VOO",
    type: InvestmentType = InvestmentType.ETF_FONDO,
    currency: Currency = Currency.MXN,
    currentValue: Double = 1_000.0,
    investedCapital: Double = 800.0,
    dailyValueChange: Double = 10.0,
    returnPercentage: Double = 25.0,
    quantity: Double = 10.0,
    currentPrice: Double = 100.0,
    institution: Institution = Institution(id = 1, name = "GBM", kind = "Casa de Bolsa"),
): Investment = Investment(
    id = id,
    name = name,
    description = "Descripción $name",
    symbol = name,
    type = type,
    institution = institution,
    currency = currency,
    currentPrice = currentPrice,
    priceChange = 1.0,
    dailyChangePercentage = 1.0,
    quantity = quantity,
    investedCapital = investedCapital,
    currentValue = currentValue,
    dailyValueChange = dailyValueChange,
    returnPercentage = returnPercentage,
    history = listOf(.5f, .5f, .5f),
)