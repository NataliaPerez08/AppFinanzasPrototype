package com.appfinanzas.prototype.ui.screens.more

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.domain.usecase.AddInstitutionUseCase
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
class InstitutionsViewModelTest {

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
        InstitutionsViewModel(AddInstitutionUseCase(repo), repo)

    @Test
    fun `loads institutions`() = runTest {
        val viewModel = viewModel(TestInvestmentRepository())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.institutions.size)
        assertEquals("GBM", state.institutions[0].name)
    }

    @Test
    fun `toggles add form`() = runTest {
        val viewModel = viewModel(TestInvestmentRepository())
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAddForm)
        viewModel.toggleAddForm()
        assertTrue(viewModel.uiState.value.showAddForm)
        viewModel.toggleAddForm()
        assertFalse(viewModel.uiState.value.showAddForm)
    }

    @Test
    fun `adds institution and updates list`() = runTest {
        val repo = TestInvestmentRepository()
        val viewModel = viewModel(repo)
        advanceUntilIdle()

        viewModel.toggleAddForm()
        viewModel.onNameChange("Klar")
        viewModel.onKindChange("SOFIPO")
        viewModel.submitAdd()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertFalse(state.showAddForm)
        assertEquals(3, state.institutions.size)
        assertEquals("Klar", state.institutions[2].name)
    }

    @Test
    fun `blank name shows form error`() = runTest {
        val viewModel = viewModel(TestInvestmentRepository())
        advanceUntilIdle()

        viewModel.toggleAddForm()
        viewModel.submitAdd()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertNotNull(state.formError)
        assertTrue(state.showAddForm)
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