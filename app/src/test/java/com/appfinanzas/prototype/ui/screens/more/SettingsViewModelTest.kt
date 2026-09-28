package com.appfinanzas.prototype.ui.screens.more

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.usecase.GetSettings
import com.appfinanzas.prototype.domain.usecase.SaveSettings
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
class SettingsViewModelTest {

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
        SettingsViewModel(GetSettings(repo), SaveSettings(repo))

    @Test
    fun `loads settings with defaults`() = runTest {
        val viewModel = viewModel(TestInvestmentRepository())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(Currency.MXN, state.baseCurrency)
        assertEquals("4.0", state.inflationText)
        assertEquals("2.0", state.isrText)
    }

    @Test
    fun `saves settings to repository`() = runTest {
        val repo = TestInvestmentRepository()
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        viewModel.onCurrencySelected(Currency.USD.name)
        viewModel.onInflationChange("5.5")
        viewModel.onIsrChange("3.0")
        viewModel.save()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertTrue(state.saved)

        val persisted = repo.observeSettings().first()
        assertEquals(Currency.USD, persisted.baseCurrency)
        assertEquals(5.5, persisted.estimatedInflation, 0.001)
        assertEquals(3.0, persisted.estimatedIsr, 0.001)
    }

    @Test
    fun `invalid inflation shows form error and does not persist`() = runTest {
        val repo = TestInvestmentRepository()
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        viewModel.onInflationChange("abc")
        viewModel.save()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.formError)
        assertEquals(4.0, repo.observeSettings().first().estimatedInflation, 0.001)
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
}