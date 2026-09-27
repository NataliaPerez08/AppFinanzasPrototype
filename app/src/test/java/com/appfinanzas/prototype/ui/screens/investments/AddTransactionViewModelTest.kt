package com.appfinanzas.prototype.ui.screens.investments

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.usecase.AddTransactionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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

    private fun viewModel(repo: TestInvestmentRepository) =
        AddTransactionViewModel(1L, AddTransactionUseCase(repo), repo)

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
            investments = listOf(sampleInvestment(id = 1, quantity = 10.0)),
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
        assertEquals(12.0, updated.quantity, 0.001)
    }

    @Test
    fun `business error is surfaced`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(id = 1, quantity = 2.0)),
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
}