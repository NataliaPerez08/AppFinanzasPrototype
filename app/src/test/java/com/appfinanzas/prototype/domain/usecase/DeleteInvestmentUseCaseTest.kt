package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteInvestmentUseCaseTest {

    private fun deposit(amount: Double, id: Long = 1) = Transaction(
        id = id,
        investmentId = 1,
        type = TransactionType.DEPOSITO,
        date = LocalDate.of(2026, 9, 21),
        quantity = 0.0,
        price = 0.0,
        commission = 0.0,
        total = amount,
        currency = Currency.MXN,
    )

    @Test
    fun `deletes investment and its transactions`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(id = 1)),
            transactions = mapOf(1L to listOf(deposit(1_000.0))),
        )
        val result = DeleteInvestmentUseCase(repo).execute(1)

        assertTrue(result is DeleteInvestmentResult.Success)
        assertTrue(repo.observeInvestments().first().isEmpty())
        assertTrue(repo.observeTransactions(1).first().isEmpty())
    }

    @Test
    fun `deletes investment without transactions`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(id = 1)),
        )

        val result = DeleteInvestmentUseCase(repo).execute(1)

        assertTrue(result is DeleteInvestmentResult.Success)
        assertTrue(repo.observeInvestments().first().isEmpty())
        assertTrue(repo.observeTransactions(1).first().isEmpty())
    }

    @Test
    fun `unknown investment is a business error`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(id = 1)))
        val result = DeleteInvestmentUseCase(repo).execute(99)

        assertTrue(result is DeleteInvestmentResult.BusinessError)
    }
}
