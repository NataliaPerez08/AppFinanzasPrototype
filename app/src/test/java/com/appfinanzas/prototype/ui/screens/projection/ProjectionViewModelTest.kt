package com.appfinanzas.prototype.ui.screens.projection

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.usecase.GetProjection
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
        getProjection = GetProjection(),
        getSettings = GetSettings(repo),
        repository = repo,
    )

    @Test
    fun `loads current value and settings defaults`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(currentValue = 1_000.0)),
        )
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertEquals(1_000.0, state.currentValue, 0.001)
        assertEquals("4.0", state.inflationText)
        assertEquals("2.0", state.isrText)
        assertEquals(1_085.0, state.nominal, 0.001)
        assertEquals(1_063.3, state.afterIsr, 0.1)
        assertEquals(3, state.scenarios.size)
        assertFalse(state.chartPoints.isEmpty())
    }

    @Test
    fun `recomputes when inputs change`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(currentValue = 1_000.0)))
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        viewModel.onExpectedReturnChange("10")
        viewModel.onInflationChange("3")
        viewModel.onIsrChange("1")

        assertEquals(1_100.0, viewModel.uiState.value.nominal, 0.001)
        assertEquals(1_089.0, viewModel.uiState.value.afterIsr, 0.1)
        assertEquals(1_057.3, viewModel.uiState.value.real, 0.1)
    }

    @Test
    fun `loads empty state when there is no portfolio`() = runTest {
        val viewModel = viewModel(TestInvestmentRepository())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
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