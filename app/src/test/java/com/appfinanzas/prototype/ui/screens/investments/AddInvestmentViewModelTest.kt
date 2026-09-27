package com.appfinanzas.prototype.ui.screens.investments

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.usecase.AddInvestmentUseCase
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
class AddInvestmentViewModelTest {

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
        AddInvestmentViewModel(AddInvestmentUseCase(repo), repo)

    @Test
    fun `loads institutions`() = runTest {
        val viewModel = viewModel(TestInvestmentRepository())
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.institutions.size)
    }

    @Test
    fun `field updates clear errors for that field`() = runTest {
        val viewModel = viewModel(TestInvestmentRepository())
        advanceUntilIdle()

        viewModel.onNameChange("Apple")
        assertEquals("Apple", viewModel.uiState.value.name)

        viewModel.onTypeSelected(InvestmentType.ACCION.name)
        assertEquals(InvestmentType.ACCION, viewModel.uiState.value.type)

        viewModel.onInstitutionSelected("1")
        assertEquals(1L, viewModel.uiState.value.institutionId)

        viewModel.onCurrencySelected(Currency.USD.name)
        assertEquals(Currency.USD, viewModel.uiState.value.currency)
    }

    @Test
    fun `invalid submit sets field errors`() = runTest {
        val viewModel = viewModel(TestInvestmentRepository())
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertTrue(state.fieldErrors.isNotEmpty())
        assertTrue(state.fieldErrors.containsKey("type"))
        assertTrue(state.fieldErrors.containsKey("name"))
    }

    @Test
    fun `valid submit saves and emits id`() = runTest {
        val repo = TestInvestmentRepository()
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        viewModel.onTypeSelected(InvestmentType.ACCION.name)
        viewModel.onInstitutionSelected("1")
        viewModel.onNameChange("Apple")
        viewModel.onSymbolChange("AAPL")
        viewModel.onCurrencySelected(Currency.USD.name)
        viewModel.onInitialValueChange("10000")
        viewModel.onDateChange("21/09/2026")
        viewModel.submit()
        advanceUntilIdle()

        val savedId = viewModel.savedEvents.first()
        assertTrue(savedId > 0)
        assertNotNull(repo.observeInvestments().first().firstOrNull { it.id == savedId })
    }

    @Test
    fun `business error is surfaced`() = runTest {
        val viewModel = viewModel(TestInvestmentRepository())
        advanceUntilIdle()

        viewModel.onTypeSelected(InvestmentType.ACCION.name)
        viewModel.onInstitutionSelected("999")
        viewModel.onNameChange("Apple")
        viewModel.onSymbolChange("AAPL")
        viewModel.onCurrencySelected(Currency.USD.name)
        viewModel.onInitialValueChange("10000")
        viewModel.onDateChange("21/09/2026")
        viewModel.submit()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.formError)
    }
}