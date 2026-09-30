package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.validation.InvestmentEditForm
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateInvestmentUseCaseTest {

    private fun validForm() = InvestmentEditForm(
        type = InvestmentType.ACCION,
        institutionId = 2,
        name = "Cetes",
        symbol = "CETES",
        currency = Currency.USD,
    )

    @Test
    fun `updates editable metadata`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(id = 1)))
        val result = UpdateInvestmentUseCase(repo).execute(1, validForm())

        assertTrue(result is UpdateInvestmentResult.Success)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals("CETES", updated.name)
        assertEquals("CETES", updated.symbol)
        assertEquals(InvestmentType.ACCION, updated.type)
        assertEquals(Currency.USD, updated.currency)
        assertEquals(2L, updated.institution.id)
    }

    @Test
    fun `invalid form returns field errors`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(id = 1)))
        val result = UpdateInvestmentUseCase(repo).execute(1, InvestmentEditForm())

        assertTrue(result is UpdateInvestmentResult.Error)
    }

    @Test
    fun `unknown investment is a business error`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(id = 1)))
        val result = UpdateInvestmentUseCase(repo).execute(99, validForm())

        assertTrue(result is UpdateInvestmentResult.BusinessError)
    }

    @Test
    fun `unknown institution is a business error`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(id = 1)))
        val result = UpdateInvestmentUseCase(repo).execute(1, validForm().copy(institutionId = 999))

        assertTrue(result is UpdateInvestmentResult.BusinessError)
    }
}
