package com.appfinanzas.prototype.ui.screens.investments

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.usecase.AddTransactionUseCase
import com.appfinanzas.prototype.domain.usecase.DeleteTransactionUseCase
import com.appfinanzas.prototype.domain.usecase.UpdateTransactionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(repo: TestInvestmentRepository, transactionId: Long? = null) =
        AddTransactionViewModel(
            investmentId = 1L,
            transactionId = transactionId,
            addTransactionUseCase = AddTransactionUseCase(repo),
            updateTransactionUseCase = UpdateTransactionUseCase(repo),
            deleteTransactionUseCase = DeleteTransactionUseCase(repo),
            repository = repo,
        )

    private fun deposit(amount: Double, id: Long = 1L) = Transaction(
        id = id,
        investmentId = 1L,
        type = TransactionType.DEPOSITO,
        date = LocalDate.of(2026, 9, 21),
        quantity = 0.0,
        price = 0.0,
        commission = 0.0,
        total = amount,
        currency = Currency.MXN,
    )

    private fun buy(quantity: Double, price: Double, id: Long = 2L) = Transaction(
        id = id,
        investmentId = 1L,
        type = TransactionType.COMPRA,
        date = LocalDate.of(2026, 9, 22),
        quantity = quantity,
        price = price,
        commission = 0.0,
        total = quantity * price,
        currency = Currency.MXN,
    )

    @Test
    fun `loads investment header`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(id = 1)))
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        val header = viewModel.uiState.value.investmentHeader
        assertNotNull(header)
        assertEquals("VOO", header!!.name)
        assertEquals("GBM · Renta variable", header.subtitle)
    }

    @Test
    fun `computes total for compra`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(id = 1)))
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        viewModel.onTypeSelected(TransactionType.COMPRA.name)
        viewModel.onQuantityChange("2")
        viewModel.onPriceChange("100")
        viewModel.onCommissionChange("1")

        assertEquals(201.0, viewModel.uiState.value.total, 0.001)
    }

    @Test
    fun `computes total for deposito as amount`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(id = 1)))
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        viewModel.onTypeSelected(TransactionType.DEPOSITO.name)
        viewModel.onQuantityChange("1500")

        assertEquals(1_500.0, viewModel.uiState.value.total, 0.001)
    }

    @Test
    fun `invalid submit sets field errors`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(id = 1)))
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        viewModel.onTypeSelected(TransactionType.COMPRA.name)
        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertTrue(state.fieldErrors.containsKey("quantity"))
        assertTrue(state.fieldErrors.containsKey("price"))
    }

    @Test
    fun `valid submit emits event and updates investment`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(id = 1)),
            transactions = mapOf(1L to listOf(deposit(10_000.0))),
        )
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        viewModel.onTypeSelected(TransactionType.COMPRA.name)
        viewModel.onQuantityChange("2")
        viewModel.onPriceChange("100")
        viewModel.onCommissionChange("1")
        viewModel.submit()
        advanceUntilIdle()

        val savedId = viewModel.savedEvents.first()
        assertEquals(1L, savedId)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(2.0, updated.quantity, 0.001)
        assertEquals(9_799.0, updated.cashBalance, 0.001)
    }

    @Test
    fun `business error is surfaced`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(id = 1)),
            transactions = mapOf(1L to listOf(deposit(1_000.0))),
        )
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        viewModel.onTypeSelected(TransactionType.VENTA.name)
        viewModel.onQuantityChange("20")
        viewModel.onPriceChange("100")
        viewModel.submit()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.formError)
    }

    @Test
    fun `edit mode loads existing transaction`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(id = 1)),
            transactions = mapOf(1L to listOf(deposit(10_000.0), buy(10.0, 100.0))),
        )
        val viewModel = viewModel(repo, transactionId = 2L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEditing)
        assertEquals(TransactionType.COMPRA, state.type)
        assertEquals("10", state.quantityText)
        assertEquals("100", state.priceText)
    }

    @Test
    fun `edit mode updates transaction and emits event`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(id = 1)),
            transactions = mapOf(1L to listOf(deposit(10_000.0), buy(10.0, 100.0))),
        )
        val viewModel = viewModel(repo, transactionId = 2L)
        advanceUntilIdle()

        viewModel.onQuantityChange("5")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(1L, viewModel.savedEvents.first())
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(5.0, updated.quantity, 0.001)
        assertEquals(9_500.0, updated.cashBalance, 0.001)
    }

    @Test
    fun `delete mode removes transaction and emits event`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(id = 1)),
            transactions = mapOf(1L to listOf(deposit(10_000.0), buy(10.0, 100.0))),
        )
        val viewModel = viewModel(repo, transactionId = 2L)
        advanceUntilIdle()

        viewModel.delete()
        advanceUntilIdle()

        assertEquals(1L, viewModel.savedEvents.first())
        assertTrue(repo.getTransactions(1).none { it.id == 2L })
        assertEquals(0.0, repo.observeInvestment(1).first()!!.quantity, 0.001)
    }

    @Test
    fun `deleting the funding deposit surfaces a business error`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(id = 1)),
            transactions = mapOf(1L to listOf(deposit(10_000.0), buy(1.0, 50.0))),
        )
        val viewModel = viewModel(repo, transactionId = 1L)
        advanceUntilIdle()

        viewModel.delete()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.formError)
        assertTrue(repo.getTransactions(1).any { it.id == 1L })
    }

    @Test
    fun `double submit creates only one transaction`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(id = 1)),
            transactions = mapOf(1L to listOf(deposit(10_000.0))),
        )
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        viewModel.onTypeSelected(TransactionType.COMPRA.name)
        viewModel.onQuantityChange("2")
        viewModel.onPriceChange("100")
        viewModel.submit()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(2, repo.getTransactions(1).size)
    }

    @Test
    fun `missing transaction in edit mode surfaces an error`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(id = 1)))
        val viewModel = viewModel(repo, transactionId = 99L)
        advanceUntilIdle()

        assertEquals("No existe el movimiento solicitado", viewModel.uiState.value.formError)
    }

    @Test
    fun `edit mode surfaces a business error when ledger breaks`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(id = 1)),
            transactions = mapOf(1L to listOf(deposit(100.0), buy(1.0, 50.0))),
        )
        val viewModel = viewModel(repo, transactionId = 2L)
        advanceUntilIdle()

        viewModel.onQuantityChange("10")
        viewModel.submit()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.formError)
        assertEquals(1.0, repo.getTransactions(1).first { it.id == 2L }.quantity, 0.001)
    }
}
