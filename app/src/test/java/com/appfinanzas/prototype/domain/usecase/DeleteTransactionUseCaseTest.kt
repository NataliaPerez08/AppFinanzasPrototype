package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteTransactionUseCaseTest {

    private val seedDate: LocalDate = LocalDate.of(2026, 9, 21)

    private fun deposit(amount: Double, id: Long = 1) = Transaction(
        id = id,
        investmentId = 1,
        type = TransactionType.DEPOSITO,
        date = seedDate,
        quantity = 0.0,
        price = 0.0,
        commission = 0.0,
        total = amount,
        currency = Currency.MXN,
    )

    private fun buy(quantity: Double, price: Double, id: Long = 2) = Transaction(
        id = id,
        investmentId = 1,
        type = TransactionType.COMPRA,
        date = seedDate,
        quantity = quantity,
        price = price,
        commission = 0.0,
        total = quantity * price,
        currency = Currency.MXN,
    )

    private fun repo(vararg transactions: Transaction) = TestInvestmentRepository(
        investments = listOf(sampleInvestment(id = 1, quantity = 0.0, currentValue = 0.0, investedCapital = 0.0, currentPrice = 0.0)),
        transactions = mapOf(1L to transactions.toList()),
    )

    @Test
    fun `deletes a transaction and restores the ledger`() = runTest {
        val repo = repo(deposit(1_000.0), buy(10.0, 100.0))
        val result = DeleteTransactionUseCase(repo).execute(1, 2)

        assertTrue(result is DeleteTransactionResult.Success)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(0.0, updated.quantity, 0.001)
        assertEquals(1_000.0, updated.cashBalance, 0.001)
        assertEquals(1, repo.observeTransactions(1).first().size)
    }

    @Test
    fun `rejects deleting a funding deposit`() = runTest {
        val repo = repo(deposit(1_000.0), buy(10.0, 100.0))
        val result = DeleteTransactionUseCase(repo).execute(1, 1)

        assertTrue(result is DeleteTransactionResult.BusinessError)
    }

    @Test
    fun `unknown transaction is a business error`() = runTest {
        val repo = repo(deposit(1_000.0))
        val result = DeleteTransactionUseCase(repo).execute(1, 99)

        assertTrue(result is DeleteTransactionResult.BusinessError)
    }
}
