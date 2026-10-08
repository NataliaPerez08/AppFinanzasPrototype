package com.appfinanzas.prototype.data

import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.PricePoint
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestInvestmentRepository(
    investments: List<Investment> = emptyList(),
    private val history: List<Float> = emptyList(),
    transactions: Map<Long, List<Transaction>> = emptyMap(),
    priceHistory: Map<Long, List<PricePoint>> = emptyMap(),
    private val institutions: List<Institution> = listOf(
        Institution(id = 1, name = "GBM", kind = "Casa de Bolsa"),
        Institution(id = 2, name = "CETES Directo", kind = "Renta fija"),
    ),
    private val error: Throwable? = null,
) : InvestmentRepository {

    private val investmentState = MutableStateFlow(investments)
    private val transactionState = MutableStateFlow(transactions.toMutableMap())
    private val priceHistoryState = MutableStateFlow(priceHistory.toMutableMap())
    private val institutionState = MutableStateFlow(institutions)
    private val settingsState = MutableStateFlow(AppSettings())
    private var nextInvestmentId = (investments.maxOfOrNull { it.id } ?: 0L) + 1
    private var nextTransactionId = (transactions.values.flatten().maxOfOrNull { it.id } ?: 0L) + 1
    private var nextPricePointId = (priceHistory.values.flatten().maxOfOrNull { it.id } ?: 0L) + 1
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

    override fun observePriceHistory(investmentId: Long): Flow<List<PricePoint>> =
        guarded(priceHistoryState.map { it[investmentId] ?: emptyList() })

    override fun observePriceHistory(): Flow<Map<Long, List<PricePoint>>> =
        guarded(priceHistoryState.map { it.toMap() })

    override fun observePortfolioHistory(): Flow<List<Float>> = flow {
        error?.let { throw it }
        emit(history)
    }

    override fun observeInstitutions(): Flow<List<Institution>> = guarded(institutionState)

    override fun observeSettings(): Flow<AppSettings> = guarded(settingsState)

    override suspend fun getInvestment(investmentId: Long): Investment? =
        investmentState.value.firstOrNull { it.id == investmentId }

    override suspend fun getAllInvestments(): List<Investment> = investmentState.value

    override suspend fun getTransaction(transactionId: Long): Transaction? =
        transactionState.value.values.flatten().firstOrNull { it.id == transactionId }

    override suspend fun getTransactions(investmentId: Long): List<Transaction> =
        transactionState.value[investmentId] ?: emptyList()

    override suspend fun getPriceHistory(investmentId: Long): List<PricePoint> =
        priceHistoryState.value[investmentId] ?: emptyList()

    override suspend fun saveInvestment(investment: Investment): Long {
        error?.let { throw it }
        val id = if (investment.id == 0L) nextInvestmentId++ else investment.id
        investmentState.update { it + investment.copy(id = id) }
        return id
    }

    override suspend fun saveInvestmentWithOpeningTransaction(
        investment: Investment,
        openingTransaction: Transaction,
    ): Long {
        error?.let { throw it }
        val id = nextInvestmentId++
        investmentState.update { it + investment.copy(id = id) }
        saveTransaction(openingTransaction.copy(investmentId = id))
        return id
    }

    override suspend fun updateInvestment(investment: Investment) {
        error?.let { throw it }
        investmentState.update { list -> list.map { if (it.id == investment.id) investment else it } }
    }

    override suspend fun deleteInvestment(investmentId: Long) {
        error?.let { throw it }
        investmentState.update { list -> list.filterNot { it.id == investmentId } }
        transactionState.update { state -> state.toMutableMap().also { it.remove(investmentId) } }
        priceHistoryState.update { state -> state.toMutableMap().also { it.remove(investmentId) } }
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

    override suspend fun savePricePoint(pricePoint: PricePoint): Long {
        error?.let { throw it }
        val id = if (pricePoint.id == 0L) nextPricePointId++ else pricePoint.id
        val saved = pricePoint.copy(id = id)
        priceHistoryState.update { state ->
            state.toMutableMap().also { map ->
                map[saved.investmentId] = (map[saved.investmentId] ?: emptyList()) + saved
            }
        }
        return id
    }

    override suspend fun saveTransactionWithInvestment(
        transaction: Transaction,
        investment: Investment,
    ): Long {
        error?.let { throw it }
        val id = saveTransaction(transaction)
        updateInvestment(investment)
        return id
    }

    override suspend fun updateTransactionWithInvestment(
        transaction: Transaction,
        investment: Investment,
    ) {
        error?.let { throw it }
        transactionState.update { state ->
            state.toMutableMap().also { map ->
                val list = map[transaction.investmentId] ?: emptyList()
                map[transaction.investmentId] = list.map { if (it.id == transaction.id) transaction else it }
            }
        }
        updateInvestment(investment)
    }

    override suspend fun deleteTransactionWithInvestment(
        transactionId: Long,
        investment: Investment,
    ) {
        error?.let { throw it }
        transactionState.update { state ->
            state.toMutableMap().also { map ->
                val list = map[investment.id] ?: emptyList()
                map[investment.id] = list.filterNot { it.id == transactionId }
            }
        }
        updateInvestment(investment)
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

    override suspend fun repairLedgerIfNeeded() = Unit
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
    cashBalance = 0.0,
    averageCost = currentPrice,
    realizedProfit = 0.0,
)

fun priceHistory(
    investmentId: Long = 1L,
    prices: List<Double>,
    startDate: LocalDate = LocalDate.of(2024, 1, 1),
    stepDays: Long = 30,
): List<PricePoint> = prices.mapIndexed { index, price ->
    PricePoint(
        id = (index + 1).toLong(),
        investmentId = investmentId,
        date = startDate.plusDays(index * stepDays),
        price = price,
    )
}
