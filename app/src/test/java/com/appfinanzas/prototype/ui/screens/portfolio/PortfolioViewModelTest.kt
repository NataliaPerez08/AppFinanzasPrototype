package com.appfinanzas.prototype.ui.screens.portfolio

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.usecase.GetPortfolioSummary
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
class PortfolioViewModelTest {

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
    fun `starts in loading state`() = runTest {
        val viewModel = PortfolioViewModel(GetPortfolioSummary(TestInvestmentRepository()))
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads content with distributions`() = runTest {
        val gbm = Institution(id = 1, name = "GBM", kind = "Casa de Bolsa")
        val nu = Institution(id = 3, name = "NU", kind = "SOFIPO")
        val viewModel = PortfolioViewModel(
            GetPortfolioSummary(
                TestInvestmentRepository(
                    investments = listOf(
                        sampleInvestment(id = 1, institution = gbm, currency = Currency.MXN, currentValue = 3_000.0, investedCapital = 2_000.0),
                        sampleInvestment(id = 2, name = "NU", institution = nu, currency = Currency.MXN, currentValue = 1_000.0, investedCapital = 1_000.0),
                        sampleInvestment(id = 3, name = "CETES", type = InvestmentType.CETES, institution = gbm, currency = Currency.MXN, currentValue = 1_000.0, investedCapital = 900.0),
                    ),
                ),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertEquals(5_000.0, state.portfolioValue, 0.001)
        assertEquals(3_900.0, state.investedCapital, 0.001)
        assertEquals(1_100.0, state.profit, 0.001)
        assertEquals(28.205, state.performance, 0.001)
        assertEquals(2, state.byInstitution.size)
        assertEquals("GBM", state.byInstitution[0].label)
        assertEquals(80f, state.byInstitution[0].percentage, 0.001f)
        assertEquals(2, state.byCategory.size)
        assertEquals(1, state.byCurrency.size)
    }

    @Test
    fun `loads empty state`() = runTest {
        val viewModel = PortfolioViewModel(GetPortfolioSummary(TestInvestmentRepository()))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `loads error state when repository fails`() = runTest {
        val viewModel = PortfolioViewModel(
            GetPortfolioSummary(TestInvestmentRepository(error = RuntimeException("boom"))),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals("boom", state.error)
    }

    @Test
    fun `retry reloads after error`() = runTest {
        val viewModel = PortfolioViewModel(
            GetPortfolioSummary(TestInvestmentRepository(error = RuntimeException("boom"))),
        )
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        viewModel.retry()
        assertTrue(viewModel.uiState.value.isLoading)
    }
}