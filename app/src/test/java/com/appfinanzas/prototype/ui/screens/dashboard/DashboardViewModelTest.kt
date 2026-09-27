package com.appfinanzas.prototype.ui.screens.dashboard

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.usecase.GetDashboardSummary
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
class DashboardViewModelTest {

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
        val viewModel = DashboardViewModel(GetDashboardSummary(TestInvestmentRepository()))
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads content`() = runTest {
        val viewModel = DashboardViewModel(
            GetDashboardSummary(
                TestInvestmentRepository(
                    investments = listOf(sampleInvestment(currentValue = 1_000.0, investedCapital = 800.0)),
                    history = listOf(.8f, .6f),
                ),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertEquals(1_000.0, state.portfolioValue, 0.001)
        assertEquals(200.0, state.profit, 0.001)
        assertEquals(25.0, state.performance, 0.001)
        assertEquals(listOf(.8f, .6f), state.history)
        assertEquals(1, state.allocation.size)
        assertEquals("Renta variable", state.allocation[0].label)
        assertEquals(100f, state.allocation[0].percentage, 0.001f)
    }

    @Test
    fun `loads empty state when there are no investments`() = runTest {
        val viewModel = DashboardViewModel(GetDashboardSummary(TestInvestmentRepository()))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `loads error state when repository fails`() = runTest {
        val viewModel = DashboardViewModel(
            GetDashboardSummary(TestInvestmentRepository(error = RuntimeException("boom"))),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals("boom", state.error)
    }

    @Test
    fun `retry reloads after error`() = runTest {
        val viewModel = DashboardViewModel(
            GetDashboardSummary(TestInvestmentRepository(error = RuntimeException("boom"))),
        )
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        viewModel.retry()
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }
}