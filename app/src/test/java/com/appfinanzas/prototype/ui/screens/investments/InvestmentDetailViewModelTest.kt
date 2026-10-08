package com.appfinanzas.prototype.ui.screens.investments

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.usecase.AddPricePointUseCase
import com.appfinanzas.prototype.domain.usecase.DeleteInvestmentUseCase
import com.appfinanzas.prototype.domain.usecase.GetInvestmentDetail
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class InvestmentDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads investment detail with transactions`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(id = 5, currentValue = 1_000.0, investedCapital = 800.0)),
            transactions = mapOf(
                5L to listOf(
                    Transaction(
                        id = 1,
                        investmentId = 5,
                        type = TransactionType.COMPRA,
                        date = LocalDate.of(2025, 1, 1),
                        quantity = 1.0,
                        price = 100.0,
                        commission = 0.0,
                        total = 100.0,
                        currency = Currency.MXN,
                    ),
                ),
            ),
        )
        val viewModel = InvestmentDetailViewModel(
            5L,
            GetInvestmentDetail(repo),
            DeleteInvestmentUseCase(repo),
            AddPricePointUseCase(repo),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertEquals("VOO", state.name)
        assertEquals(1_000.0, state.currentValue, 0.001)
        assertEquals(200.0, state.profit, 0.001)
        assertEquals(1, state.transactions.size)
        assertEquals("COMPRA", state.transactions[0].typeLabel)
        assertEquals("01/01/2025", state.transactions[0].dateLabel)
    }

    @Test
    fun `loads empty state for unknown investment`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(id = 1)))
        val viewModel = InvestmentDetailViewModel(
            99L,
            GetInvestmentDetail(repo),
            DeleteInvestmentUseCase(repo),
            AddPricePointUseCase(repo),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `loads error state when repository fails`() = runTest {
        val repo = TestInvestmentRepository(error = RuntimeException("boom"))
        val viewModel = InvestmentDetailViewModel(
            1L,
            GetInvestmentDetail(repo),
            DeleteInvestmentUseCase(repo),
            AddPricePointUseCase(repo),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals("boom", state.error)
    }

    @Test
    fun `saves a price point and reflects the count`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(id = 5)))
        val viewModel = InvestmentDetailViewModel(
            5L,
            GetInvestmentDetail(repo),
            DeleteInvestmentUseCase(repo),
            AddPricePointUseCase(repo),
        )
        advanceUntilIdle()

        viewModel.onPriceChange("123.45")
        viewModel.savePricePoint()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.priceCount)
        assertTrue(viewModel.uiState.value.priceSaved)
        assertEquals("", viewModel.uiState.value.priceText)
    }
}