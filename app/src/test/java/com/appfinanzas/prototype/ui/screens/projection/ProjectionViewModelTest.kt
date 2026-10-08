package com.appfinanzas.prototype.ui.screens.projection

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.usecase.GetHybridProjection
import com.appfinanzas.prototype.domain.usecase.GetSettings
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectionViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(repo: TestInvestmentRepository) = ProjectionViewModel(
        getHybridProjection = GetHybridProjection(),
        getSettings = GetSettings(repo),
        repository = repo,
    )

    @Test
    fun `loads current value settings defaults and a probability range`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(currentValue = 1_000.0)),
        )
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertEquals(1_000.0, state.currentValue, 0.001)
        assertEquals("8.5", state.expectedReturnText)
        assertEquals("4.0", state.inflationText)
        assertEquals("2.0", state.isrText)
        assertEquals("15.0", state.volatilityText)
        assertEquals(1_085.0, state.nominal, 0.001)
        assertEquals(85.0, state.expectedGain, 0.001)
        assertNotNull(state.p10)
        assertNotNull(state.p90)
        assertTrue(state.p10!! <= state.p90!!)
        assertNotNull(state.chartBand)
        assertEquals(13, state.chartBand!!.mid.size)
        assertEquals(1, state.institutions.size)
        assertEquals("Monte Carlo", state.institutions[0].assets[0].strategyLabel)
        assertEquals(3, state.scenarios.size)
        assertEquals("Base", state.scenarios[0].label)
    }

    @Test
    fun `recomputes when inputs change`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(currentValue = 1_000.0)))
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        viewModel.onExpectedReturnChange("10")
        viewModel.onInflationChange("3")
        viewModel.onIsrChange("1")

        val state = viewModel.uiState.value
        assertEquals(1_100.0, state.nominal, 0.001)
        assertEquals(1_089.0, state.afterTax, 0.1)
        assertEquals(1_057.3, state.real, 0.1)
    }

    @Test
    fun `changing horizon reprojects`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(currentValue = 1_000.0)))
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        viewModel.onHorizonSelected(36)

        val state = viewModel.uiState.value
        assertEquals(36, state.horizonMonths)
        assertEquals(1_000.0 * 1.085 * 1.085 * 1.085, state.nominal, 0.01)
        assertEquals(37, state.chartBand!!.mid.size)
    }

    @Test
    fun `settings contribution is reported as contributions`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(currentValue = 1_000.0)))
        repo.saveSettings(AppSettings(monthlyContribution = 1_000.0))
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(12_000.0, state.contributions, 0.01)
        assertTrue(state.nominal > 1_085.0)
    }

    @Test
    fun `loads empty state when there is no portfolio`() = runTest {
        val viewModel = viewModel(TestInvestmentRepository())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
        assertNull(state.p10)
    }

    @Test
    fun `loads error state when repository fails`() = runTest {
        val viewModel = viewModel(TestInvestmentRepository(error = RuntimeException("boom")))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals("boom", state.error)
    }

    @Test
    fun `retry reloads after error`() = runTest {
        val viewModel = viewModel(TestInvestmentRepository(error = RuntimeException("boom")))
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        viewModel.retry()
        assertTrue(viewModel.uiState.value.isLoading)
    }
}
