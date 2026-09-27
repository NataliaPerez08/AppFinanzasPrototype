package com.appfinanzas.prototype.ui.screens.investments

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.InvestmentCategory
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.usecase.GetInvestments
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InvestmentsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = InvestmentsViewModel(
        GetInvestments(
            TestInvestmentRepository(
                investments = listOf(
                    sampleInvestment(id = 1, currentValue = 1_000.0, investedCapital = 800.0),
                    sampleInvestment(id = 2, name = "CETES", type = InvestmentType.CETES, currentValue = 500.0, investedCapital = 500.0),
                    sampleInvestment(id = 3, name = "NU", type = InvestmentType.SOFIPO, currentValue = 300.0, investedCapital = 300.0),
                ),
            ),
        ),
    )

    @Test
    fun `loads totals and all investments`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1_800.0, state.totalValue, 0.001)
        assertEquals(200.0, state.totalProfit, 0.001)
        assertEquals(3, state.investments.size)
        assertEquals(null, state.filter)
    }

    @Test
    fun `filter narrows the list but keeps portfolio totals`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.selectFilter(InvestmentCategory.RENTA_FIJA)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.investments.size)
        assertEquals("CETES", state.investments[0].name)
        assertEquals(InvestmentCategory.RENTA_FIJA, state.filter)
        assertEquals(1_800.0, state.totalValue, 0.001)
    }

    @Test
    fun `clearing filter restores all investments`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.selectFilter(InvestmentCategory.SOFIPO)
        advanceUntilIdle()
        viewModel.selectFilter(null)
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.investments.size)
    }

    @Test
    fun `loads error state when repository fails`() = runTest {
        val viewModel = InvestmentsViewModel(
            GetInvestments(TestInvestmentRepository(error = RuntimeException("boom"))),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals("boom", state.error)
    }
}