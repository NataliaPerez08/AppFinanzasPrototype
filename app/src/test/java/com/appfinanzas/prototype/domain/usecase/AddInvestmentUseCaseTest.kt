package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.validation.InvestmentForm
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddInvestmentUseCaseTest {

    private fun validForm() = InvestmentForm(
        type = InvestmentType.ACCION,
        institutionId = 1,
        name = "Apple",
        symbol = "AAPL",
        currency = Currency.USD,
        initialValueText = "10000",
        dateText = "21/09/2026",
    )

    @Test
    fun `saves investment and initial deposit`() = runTest {
        val repo = TestInvestmentRepository()
        val result = AddInvestmentUseCase(repo).execute(validForm())

        assertTrue(result is AddInvestmentResult.Success)
        val id = (result as AddInvestmentResult.Success).investmentId
        assertTrue(id > 0)

        val saved = repo.observeInvestments().first()
        assertEquals(1, saved.size)
        assertEquals("APPLE", saved[0].name)
        assertEquals("AAPL", saved[0].symbol)
        assertEquals(10_000.0, saved[0].currentValue, 0.001)

        val transactions = repo.observeTransactions(id).first()
        assertEquals(1, transactions.size)
        assertEquals(TransactionType.DEPOSITO, transactions[0].type)
        assertEquals(10_000.0, transactions[0].total, 0.001)
    }

    @Test
    fun `returns field errors for invalid form`() = runTest {
        val result = AddInvestmentUseCase(TestInvestmentRepository()).execute(InvestmentForm())

        assertTrue(result is AddInvestmentResult.Error)
        val errors = (result as AddInvestmentResult.Error).errors
        assertTrue(errors.isNotEmpty())
    }

    @Test
    fun `returns business error for unknown institution`() = runTest {
        val repo = TestInvestmentRepository()
        val result = AddInvestmentUseCase(repo).execute(validForm().copy(institutionId = 999))

        assertTrue(result is AddInvestmentResult.BusinessError)
    }
}