package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.validation.TransactionForm
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateTransactionUseCaseTest {

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
        investments = listOf(
            sampleInvestment(
                id = 1,
                quantity = 10.0,
                investedCapital = 1_000.0,
                currentValue = 1_000.0,
                currentPrice = 100.0,
            ),
        ),
        transactions = mapOf(1L to transactions.toList()),
    )

    private fun compra(quantity: String, price: String) = TransactionForm(
        type = TransactionType.COMPRA,
        dateText = "21/09/2026",
        quantityText = quantity,
        priceText = price,
        commissionText = "0",
        currency = Currency.MXN,
    )

    @Test
    fun `updates a transaction and recalculates the ledger`() = runTest {
        val repo = repo(deposit(1_000.0), buy(10.0, 100.0))
        val result = UpdateTransactionUseCase(repo).execute(1, 2, compra("5", "100"))

        assertTrue(result is UpdateTransactionResult.Success)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(5.0, updated.quantity, 0.001)
        assertEquals(500.0, updated.cashBalance, 0.001)
    }

    @Test
    fun `rejects an update that breaks the ledger`() = runTest {
        val repo = repo(deposit(1_000.0), buy(10.0, 100.0))
        val result = UpdateTransactionUseCase(repo).execute(1, 2, compra("50", "100"))

        assertTrue(result is UpdateTransactionResult.BusinessError)
        assertEquals(10.0, repo.observeInvestment(1).first()!!.quantity, 0.001)
    }

    @Test
    fun `unknown transaction is a business error`() = runTest {
        val repo = repo(deposit(1_000.0))
        val result = UpdateTransactionUseCase(repo).execute(1, 99, compra("1", "100"))

        assertTrue(result is UpdateTransactionResult.BusinessError)
    }

    @Test
    fun `invalid form returns field errors`() = runTest {
        val repo = repo(deposit(1_000.0))
        val result = UpdateTransactionUseCase(repo).execute(
            1,
            1,
            TransactionForm(type = TransactionType.COMPRA, dateText = "", quantityText = "", priceText = "", currency = Currency.MXN),
        )

        assertTrue(result is UpdateTransactionResult.Error)
    }
}
