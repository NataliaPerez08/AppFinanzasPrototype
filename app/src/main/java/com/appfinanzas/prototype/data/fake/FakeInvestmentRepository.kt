package com.appfinanzas.prototype.data.fake

import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.repository.InvestmentRepository
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object FakeInvestmentRepository : InvestmentRepository {

    private val gbm = Institution(id = 1, name = "GBM", kind = "Casa de Bolsa")
    private val cetesDirecto = Institution(id = 2, name = "CETES Directo", kind = "Renta fija")
    private val nu = Institution(id = 3, name = "NU", kind = "SOFIPO")

    private val investments = listOf(
        Investment(
            id = 1,
            name = "VOO",
            description = "Vanguard S&P 500 ETF",
            symbol = "VOO",
            type = InvestmentType.ETF_FONDO,
            institution = gbm,
            currency = Currency.USD,
            currentPrice = 432.18,
            priceChange = 1.24,
            dailyChangePercentage = 0.29,
            quantity = 34.0,
            investedCapital = 160_000.0,
            currentValue = 185_430.0,
            dailyValueChange = 185_430.0 * 0.29 / 100.0,
            returnPercentage = 12.4,
            history = listOf(.80f, .76f, .70f, .62f, .58f, .50f, .44f, .40f, .34f, .30f, .26f, .22f, .20f, .16f, .14f, .10f, .06f),
        ),
        Investment(
            id = 2,
            name = "FUNO11",
            description = "Fibra Uno",
            symbol = "FUNO11",
            type = InvestmentType.FIBRA,
            institution = gbm,
            currency = Currency.MXN,
            currentPrice = 19.95,
            priceChange = 0.35,
            dailyChangePercentage = 1.79,
            quantity = 4_125.0,
            investedCapital = 79_000.0,
            currentValue = 82_300.0,
            dailyValueChange = 82_300.0 * 1.79 / 100.0,
            returnPercentage = 4.2,
            history = listOf(.60f, .58f, .55f, .52f, .50f, .48f, .45f, .42f, .40f, .38f, .36f, .34f, .32f),
        ),
        Investment(
            id = 3,
            name = "AAPL",
            description = "Apple Inc.",
            symbol = "AAPL",
            type = InvestmentType.ACCION,
            institution = gbm,
            currency = Currency.USD,
            currentPrice = 228.50,
            priceChange = 2.10,
            dailyChangePercentage = 0.93,
            quantity = 120.0,
            investedCapital = 52_000.0,
            currentValue = 71_540.0,
            dailyValueChange = 71_540.0 * 0.93 / 100.0,
            returnPercentage = 28.1,
            history = listOf(.90f, .85f, .78f, .72f, .66f, .60f, .55f, .50f, .45f, .40f, .36f, .32f, .28f),
        ),
        Investment(
            id = 4,
            name = "CETES 28D",
            description = "CETES 28 días",
            symbol = "CETES28",
            type = InvestmentType.CETES,
            institution = cetesDirecto,
            currency = Currency.MXN,
            currentPrice = 100.00,
            priceChange = 0.0,
            dailyChangePercentage = 0.0,
            quantity = 1_500.0,
            investedCapital = 150_000.0,
            currentValue = 150_000.0,
            dailyValueChange = 0.0,
            returnPercentage = 7.1,
            history = listOf(.50f, .50f, .50f, .50f, .50f, .50f, .50f, .50f, .50f, .50f),
        ),
        Investment(
            id = 5,
            name = "NU",
            description = "Nu México",
            symbol = "NU",
            type = InvestmentType.SOFIPO,
            institution = nu,
            currency = Currency.MXN,
            currentPrice = 20.00,
            priceChange = 0.0,
            dailyChangePercentage = 0.0,
            quantity = 10_000.0,
            investedCapital = 180_000.0,
            currentValue = 200_000.0,
            dailyValueChange = 0.0,
            returnPercentage = 11.0,
            history = listOf(.70f, .66f, .62f, .58f, .54f, .50f, .46f, .42f, .38f, .34f, .30f),
        ),
    )

    private val transactions = mapOf(
        1L to listOf(
            Transaction(
                id = 1,
                investmentId = 1,
                type = TransactionType.COMPRA,
                date = LocalDate.of(2023, 1, 10),
                quantity = 20.0,
                price = 300.0,
                commission = 2.0,
                total = 6_002.0,
                currency = Currency.USD,
            ),
            Transaction(
                id = 2,
                investmentId = 1,
                type = TransactionType.COMPRA,
                date = LocalDate.of(2024, 6, 5),
                quantity = 14.0,
                price = 350.0,
                commission = 2.0,
                total = 4_902.0,
                currency = Currency.USD,
            ),
            Transaction(
                id = 3,
                investmentId = 1,
                type = TransactionType.DIVIDENDO,
                date = LocalDate.of(2025, 3, 15),
                quantity = 0.0,
                price = 0.0,
                commission = 0.0,
                total = 450.0,
                currency = Currency.USD,
            ),
        ),
        2L to listOf(
            Transaction(
                id = 4,
                investmentId = 2,
                type = TransactionType.COMPRA,
                date = LocalDate.of(2023, 4, 20),
                quantity = 4_125.0,
                price = 19.0,
                commission = 5.0,
                total = 78_405.0,
                currency = Currency.MXN,
            ),
        ),
        4L to listOf(
            Transaction(
                id = 5,
                investmentId = 4,
                type = TransactionType.DEPOSITO,
                date = LocalDate.of(2025, 1, 15),
                quantity = 0.0,
                price = 0.0,
                commission = 0.0,
                total = 150_000.0,
                currency = Currency.MXN,
            ),
        ),
        5L to listOf(
            Transaction(
                id = 6,
                investmentId = 5,
                type = TransactionType.DEPOSITO,
                date = LocalDate.of(2024, 9, 1),
                quantity = 0.0,
                price = 0.0,
                commission = 0.0,
                total = 180_000.0,
                currency = Currency.MXN,
            ),
            Transaction(
                id = 7,
                investmentId = 5,
                type = TransactionType.INTERES,
                date = LocalDate.of(2025, 6, 30),
                quantity = 0.0,
                price = 0.0,
                commission = 0.0,
                total = 20_000.0,
                currency = Currency.MXN,
            ),
        ),
    )

    private val portfolioHistory = listOf(
        .80f, .72f, .68f, .58f, .61f, .49f, .45f, .38f, .42f, .31f, .25f, .32f, .18f, .23f, .12f, .19f, .08f,
    )

    override fun observeInvestments(): Flow<List<Investment>> = flow {
        delay(300)
        emit(investments)
    }

    override fun observeInvestment(investmentId: Long): Flow<Investment?> = flow {
        delay(300)
        emit(investments.firstOrNull { it.id == investmentId })
    }

    override fun observeTransactions(investmentId: Long): Flow<List<Transaction>> = flow {
        delay(300)
        emit(transactions[investmentId] ?: emptyList())
    }

    override fun observePortfolioHistory(): Flow<List<Float>> = flow {
        delay(300)
        emit(portfolioHistory)
    }

    override fun observeInstitutions(): Flow<List<Institution>> = flow {
        emit(listOf(gbm, cetesDirecto, nu))
    }

    override suspend fun saveInvestment(investment: Investment): Long =
        throw UnsupportedOperationException("FakeInvestmentRepository es de solo lectura")

    override suspend fun updateInvestment(investment: Investment) {
        throw UnsupportedOperationException("FakeInvestmentRepository es de solo lectura")
    }

    override suspend fun saveTransaction(transaction: Transaction): Long =
        throw UnsupportedOperationException("FakeInvestmentRepository es de solo lectura")
}