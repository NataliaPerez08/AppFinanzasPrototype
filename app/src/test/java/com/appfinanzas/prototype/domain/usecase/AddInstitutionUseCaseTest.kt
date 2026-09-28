package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.data.TestInvestmentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddInstitutionUseCaseTest {

    @Test
    fun `adds institution and persists it`() = runTest {
        val repo = TestInvestmentRepository()
        val result = AddInstitutionUseCase(repo).execute(name = "Klar", kind = "SOFIPO")

        assertTrue(result is AddInstitutionResult.Success)
        val saved = repo.observeInstitutions().first()
        assertEquals(3, saved.size)
        assertEquals("Klar", saved[2].name)
        assertEquals("SOFIPO", saved[2].kind)
    }

    @Test
    fun `blank name is an error`() = runTest {
        val repo = TestInvestmentRepository()
        val result = AddInstitutionUseCase(repo).execute(name = "  ", kind = "Banco")

        assertTrue(result is AddInstitutionResult.Error)
        assertEquals("El nombre es obligatorio", (result as AddInstitutionResult.Error).message)
        assertEquals(2, repo.observeInstitutions().first().size)
    }

    @Test
    fun `blank kind is an error`() = runTest {
        val repo = TestInvestmentRepository()
        val result = AddInstitutionUseCase(repo).execute(name = "Klar", kind = "")

        assertTrue(result is AddInstitutionResult.Error)
        assertEquals("El tipo es obligatorio", (result as AddInstitutionResult.Error).message)
    }
}