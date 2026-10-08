package com.appfinanzas.prototype.flow

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.usecase.AddTransactionResult
import com.appfinanzas.prototype.domain.usecase.AddTransactionUseCase
import com.appfinanzas.prototype.domain.usecase.DeleteInvestmentResult
import com.appfinanzas.prototype.domain.usecase.DeleteInvestmentUseCase
import com.appfinanzas.prototype.domain.usecase.GetDashboardSummary
import com.appfinanzas.prototype.domain.usecase.DeleteTransactionResult
import com.appfinanzas.prototype.domain.usecase.DeleteTransactionUseCase
import com.appfinanzas.prototype.domain.usecase.UpdateTransactionResult
import com.appfinanzas.prototype.domain.usecase.UpdateTransactionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class InvestmentFlowIntegrationTest {

    private lateinit var harness: FlowTestHarness

    @Before
    fun setUp() {
        harness = FlowTestHarness()
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun createInvestment_seedsDepositAndUpdatesDashboard() = runTest {
        val institutionId = harness.seedInstitution()
        val id = harness.seedInvestment(institutionId)

        val investment = harness.repository.getInvestment(id)!!
        assertEquals(0.0, investment.quantity, 0.001)
        assertEquals(10_000.0, investment.cashBalance, 0.001)
        assertEquals(10_000.0, investment.investedCapital, 0.001)

        val transactions = harness.repository.getTransactions(id)
        assertEquals(1, transactions.size)
        assertEquals(TransactionType.DEPOSITO, transactions.single().type)
        assertEquals(10_000.0, transactions.single().total, 0.001)

        val summary = GetDashboardSummary(harness.repository).observe().first()
        assertEquals(10_000.0, summary.portfolioValue, 0.001)
        assertEquals(10_000.0, summary.investedCapital, 0.001)
        assertEquals(1, summary.investmentCount)
    }

    @Test
    fun buySellAndWithdraw_updateLedgerAndDashboard() = runTest {
        val institutionId = harness.seedInstitution()
        val id = harness.seedInvestment(institutionId)
        val useCase = AddTransactionUseCase(harness.repository)

        assertTrue(useCase.execute(id, FlowTestHarness.transactionForm(TransactionType.COMPRA, "2", "100")) is AddTransactionResult.Success)
        var investment = harness.repository.getInvestment(id)!!
        assertEquals(2.0, investment.quantity, 0.001)
        assertEquals(9_800.0, investment.cashBalance, 0.001)

        val afterBuy = GetDashboardSummary(harness.repository).observe().first()
        assertEquals(10_000.0, afterBuy.portfolioValue, 0.001)

        assertTrue(useCase.execute(id, FlowTestHarness.transactionForm(TransactionType.VENTA, "1", "120")) is AddTransactionResult.Success)
        investment = harness.repository.getInvestment(id)!!
        assertEquals(1.0, investment.quantity, 0.001)
        assertEquals(9_920.0, investment.cashBalance, 0.001)
        assertEquals(20.0, investment.realizedProfit, 0.001)

        val error = useCase.execute(id, FlowTestHarness.transactionForm(TransactionType.RETIRO, "999999"))
        assertTrue(error is AddTransactionResult.BusinessError)
        assertEquals(1.0, harness.repository.getInvestment(id)!!.quantity, 0.001)
    }

    @Test
    fun deleteInvestment_removesCascadeAndEmptiesDashboard() = runTest {
        val institutionId = harness.seedInstitution()
        val id = harness.seedInvestment(institutionId)
        AddTransactionUseCase(harness.repository)
            .execute(id, FlowTestHarness.transactionForm(TransactionType.COMPRA, "2", "100"))

        assertTrue(harness.repository.getTransactions(id).isNotEmpty())

        val result = DeleteInvestmentUseCase(harness.repository).execute(id)
        assertTrue(result is DeleteInvestmentResult.Success)

        assertTrue(harness.repository.observeInvestments().first().isEmpty())
        assertTrue(harness.repository.getTransactions(id).isEmpty())

        val summary = GetDashboardSummary(harness.repository).observe().first()
        assertEquals(0.0, summary.portfolioValue, 0.001)
        assertEquals(0, summary.investmentCount)
    }

    @Test
    fun deleteInvestmentWithoutTransactions_removesInvestmentAndEmptiesDashboard() = runTest {
        val institutionId = harness.seedInstitution()
        val institution = harness.repository.observeInstitutions().first().single()
        val id = harness.repository.saveInvestment(
            Investment(
                id = 0,
                name = "Apple",
                description = "Apple",
                symbol = "AAPL",
                type = InvestmentType.ACCION,
                institution = institution.copy(id = institutionId),
                currency = Currency.MXN,
                currentPrice = 0.0,
                priceChange = 0.0,
                dailyChangePercentage = 0.0,
                quantity = 0.0,
                investedCapital = 0.0,
                currentValue = 0.0,
                dailyValueChange = 0.0,
                returnPercentage = 0.0,
                history = listOf(.5f, .5f),
            ),
        )

        assertTrue(harness.repository.getTransactions(id).isEmpty())

        val result = DeleteInvestmentUseCase(harness.repository).execute(id)

        assertTrue(result is DeleteInvestmentResult.Success)
        assertTrue(harness.repository.getInvestment(id) == null)
        assertTrue(harness.repository.getTransactions(id).isEmpty())
        assertEquals(0, GetDashboardSummary(harness.repository).observe().first().investmentCount)
    }

    @Test
    fun roomChangesPropagateThroughFlows() = runTest {
        val institutionId = harness.seedInstitution()
        val id = harness.seedInvestment(institutionId)

        assertEquals(0.0, harness.repository.observeInvestment(id).first()!!.quantity, 0.001)

        AddTransactionUseCase(harness.repository)
            .execute(id, FlowTestHarness.transactionForm(TransactionType.COMPRA, "3", "50"))

        val updated = harness.repository.observeInvestment(id).first()!!
        assertEquals(3.0, updated.quantity, 0.001)
    }

    @Test
    fun editingTransaction_emitsUpdatedInvestmentThroughFlow() = runTest {
        val institutionId = harness.seedInstitution()
        val id = harness.seedInvestment(institutionId)
        assertTrue(
            AddTransactionUseCase(harness.repository)
                .execute(id, FlowTestHarness.transactionForm(TransactionType.COMPRA, "2", "100"))
                is AddTransactionResult.Success,
        )
        val transaction = harness.repository.getTransactions(id).single { it.type == TransactionType.COMPRA }
        val emissions = Channel<Investment>(Channel.UNLIMITED)
        backgroundScope.launch {
            harness.repository.observeInvestment(id).filterNotNull().collect { emissions.send(it) }
        }
        val before = emissions.receive()
        assertEquals(2.0, before.quantity, 0.001)

        val result = UpdateTransactionUseCase(harness.repository).execute(
            investmentId = id,
            transactionId = transaction.id,
            form = FlowTestHarness.transactionForm(TransactionType.COMPRA, "5", "100"),
        )

        assertTrue(result is UpdateTransactionResult.Success)
        val after = emissions.receive()
        assertEquals(5.0, after.quantity, 0.001)
    }

    @Test
    fun deletingTransaction_emitsRecomputedInvestmentThroughFlow() = runTest {
        val institutionId = harness.seedInstitution()
        val id = harness.seedInvestment(institutionId)
        assertTrue(
            AddTransactionUseCase(harness.repository)
                .execute(id, FlowTestHarness.transactionForm(TransactionType.COMPRA, "2", "100"))
                is AddTransactionResult.Success,
        )
        val transaction = harness.repository.getTransactions(id).single { it.type == TransactionType.COMPRA }
        val emissions = Channel<Investment>(Channel.UNLIMITED)
        backgroundScope.launch {
            harness.repository.observeInvestment(id).filterNotNull().collect { emissions.send(it) }
        }
        val before = emissions.receive()
        assertEquals(2.0, before.quantity, 0.001)

        val result = DeleteTransactionUseCase(harness.repository).execute(id, transaction.id)

        assertTrue(result is DeleteTransactionResult.Success)
        val after = emissions.receive()
        assertEquals(0.0, after.quantity, 0.001)
        assertEquals(10_000.0, after.cashBalance, 0.001)
    }

    @Test
    fun dashboardAndDetailMetricsMatch() = runTest {
        val institutionId = harness.seedInstitution()
        val first = harness.seedInvestment(institutionId, initialValue = "1000")
        harness.seedInvestment(institutionId, initialValue = "500", name = "Cetes", symbol = "CETES")
        AddTransactionUseCase(harness.repository)
            .execute(first, FlowTestHarness.transactionForm(TransactionType.COMPRA, "3", "100"))

        val investments = harness.repository.observeInvestments().first()
        val summary = GetDashboardSummary(harness.repository).summarize(investments, emptyList())
        val details = investments.map { harness.repository.getInvestment(it.id)!! }

        assertEquals(details.sumOf { it.currentValue }, summary.portfolioValue, 0.001)
        assertEquals(details.sumOf { it.investedCapital }, summary.investedCapital, 0.001)
        assertEquals(details.sumOf { it.currentValue - it.investedCapital }, summary.profit, 0.001)
    }

    @Test
    fun partialSale_reducesPositionTo60() = runTest {
        val institutionId = harness.seedInstitution()
        val id = harness.seedInvestment(institutionId)
        val useCase = AddTransactionUseCase(harness.repository)

        assertTrue(useCase.execute(id, FlowTestHarness.transactionForm(TransactionType.COMPRA, "100", "100")) is AddTransactionResult.Success)
        assertTrue(useCase.execute(id, FlowTestHarness.transactionForm(TransactionType.VENTA, "40", "120")) is AddTransactionResult.Success)

        val investment = harness.repository.getInvestment(id)!!
        assertEquals(60.0, investment.quantity, 0.001)
        assertEquals(4_800.0, investment.cashBalance, 0.001)
        assertEquals(800.0, investment.realizedProfit, 0.001)
    }

    @Test
    fun fullSale_closesPositionWithoutNegativeQuantity() = runTest {
        val institutionId = harness.seedInstitution()
        val id = harness.seedInvestment(institutionId)
        val useCase = AddTransactionUseCase(harness.repository)

        assertTrue(useCase.execute(id, FlowTestHarness.transactionForm(TransactionType.COMPRA, "100", "100")) is AddTransactionResult.Success)
        assertTrue(useCase.execute(id, FlowTestHarness.transactionForm(TransactionType.VENTA, "100", "100")) is AddTransactionResult.Success)

        val investment = harness.repository.getInvestment(id)!!
        assertEquals(0.0, investment.quantity, 0.001)
        assertEquals(10_000.0, investment.cashBalance, 0.001)
        assertEquals(0.0, investment.realizedProfit, 0.001)
        assertFalse(investment.quantity < 0.0)
    }

    @Test
    fun invalidSale_isRejectedWithoutChangingRoom() = runTest {
        val institutionId = harness.seedInstitution()
        val id = harness.seedInvestment(institutionId)
        val useCase = AddTransactionUseCase(harness.repository)

        assertTrue(useCase.execute(id, FlowTestHarness.transactionForm(TransactionType.COMPRA, "100", "100")) is AddTransactionResult.Success)
        val transactionsBefore = harness.repository.getTransactions(id)

        val result = useCase.execute(id, FlowTestHarness.transactionForm(TransactionType.VENTA, "150", "120"))
        assertTrue(result is AddTransactionResult.BusinessError)

        val investment = harness.repository.getInvestment(id)!!
        assertEquals(100.0, investment.quantity, 0.001)
        assertEquals(transactionsBefore.size, harness.repository.getTransactions(id).size)
    }

    @Test
    fun totalWithdrawal_emptiesCash() = runTest {
        val institutionId = harness.seedInstitution()
        val id = harness.seedInvestment(institutionId)
        val useCase = AddTransactionUseCase(harness.repository)

        assertTrue(useCase.execute(id, FlowTestHarness.transactionForm(TransactionType.RETIRO, "10000")) is AddTransactionResult.Success)

        val investment = harness.repository.getInvestment(id)!!
        assertEquals(0.0, investment.cashBalance, 0.001)
        assertEquals(0.0, investment.investedCapital, 0.001)
    }
}
