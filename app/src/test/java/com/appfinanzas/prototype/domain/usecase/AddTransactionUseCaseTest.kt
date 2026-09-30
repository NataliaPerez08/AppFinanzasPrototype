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

class AddTransactionUseCaseTest {

    private val seedDate: LocalDate = LocalDate.of(2026, 9, 21)

    private fun form(
        type: TransactionType,
        quantity: String = "2",
        price: String = "100",
        commission: String = "1",
        date: String = "22/09/2026",
    ) = TransactionForm(
        type = type,
        dateText = date,
        quantityText = quantity,
        priceText = price,
        commissionText = commission,
        currency = Currency.MXN,
    )

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

    private fun buy(quantity: Double, price: Double, commission: Double = 0.0, id: Long = 2) = Transaction(
        id = id,
        investmentId = 1,
        type = TransactionType.COMPRA,
        date = seedDate,
        quantity = quantity,
        price = price,
        commission = commission,
        total = quantity * price + commission,
        currency = Currency.MXN,
    )

    private fun repository(vararg transactions: Transaction) = TestInvestmentRepository(
        investments = listOf(
            sampleInvestment(
                id = 1,
                quantity = 0.0,
                investedCapital = 0.0,
                currentValue = 0.0,
                currentPrice = 0.0,
            ),
        ),
        transactions = mapOf(1L to transactions.toList()),
    )

    @Test
    fun `compra increases quantity and reduces cash`() = runTest {
        val repo = repository(deposit(10_000.0))
        val result = AddTransactionUseCase(repo).execute(1, form(TransactionType.COMPRA))

        assertTrue(result is AddTransactionResult.Success)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(2.0, updated.quantity, 0.001)
        assertEquals(9_799.0, updated.cashBalance, 0.001)
        assertEquals(10_000.0, updated.investedCapital, 0.001)
        assertEquals(100.5, updated.averageCost, 0.001)
    }

    @Test
    fun `deposito increases cash and capital`() = runTest {
        val repo = repository(deposit(1_000.0))
        val result = AddTransactionUseCase(repo).execute(
            1,
            form(TransactionType.DEPOSITO, quantity = "500", price = "", commission = ""),
        )

        assertTrue(result is AddTransactionResult.Success)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(1_500.0, updated.cashBalance, 0.001)
        assertEquals(1_500.0, updated.investedCapital, 0.001)
    }

    @Test
    fun `venta reduces quantity and realizes profit`() = runTest {
        val repo = repository(deposit(10_000.0), buy(quantity = 10.0, price = 100.0))
        val result = AddTransactionUseCase(repo).execute(
            1,
            form(TransactionType.VENTA, quantity = "4", price = "120"),
        )

        assertTrue(result is AddTransactionResult.Success)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(6.0, updated.quantity, 0.001)
        assertEquals(9_479.0, updated.cashBalance, 0.001)
        assertEquals(79.0, updated.realizedProfit, 0.001)
    }

    @Test
    fun `venta exceeding position is a business error`() = runTest {
        val repo = repository(deposit(10_000.0))
        val result = AddTransactionUseCase(repo).execute(
            1,
            form(TransactionType.VENTA, quantity = "20", price = "100"),
        )

        assertTrue(result is AddTransactionResult.BusinessError)
        assertEquals(0.0, repo.observeInvestment(1).first()!!.quantity, 0.001)
    }

    @Test
    fun `compra exceeding cash is a business error`() = runTest {
        val repo = repository(deposit(100.0))
        val result = AddTransactionUseCase(repo).execute(
            1,
            form(TransactionType.COMPRA, quantity = "10", price = "100", commission = "0"),
        )

        assertTrue(result is AddTransactionResult.BusinessError)
    }

    @Test
    fun `retiro exceeding cash is a business error`() = runTest {
        val repo = repository(deposit(100.0))
        val result = AddTransactionUseCase(repo).execute(
            1,
            form(TransactionType.RETIRO, quantity = "5000", price = "", commission = ""),
        )

        assertTrue(result is AddTransactionResult.BusinessError)
    }

    @Test
    fun `dividendo increases cash without changing position`() = runTest {
        val repo = repository(deposit(10_000.0))
        val result = AddTransactionUseCase(repo).execute(
            1,
            form(TransactionType.DIVIDENDO, quantity = "50", price = "", commission = ""),
        )

        assertTrue(result is AddTransactionResult.Success)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(0.0, updated.quantity, 0.001)
        assertEquals(10_050.0, updated.cashBalance, 0.001)
        assertEquals(10_000.0, updated.investedCapital, 0.001)
    }

    @Test
    fun `comision reduces cash and not capital`() = runTest {
        val repo = repository(deposit(1_000.0))
        val result = AddTransactionUseCase(repo).execute(
            1,
            form(TransactionType.COMISION, quantity = "30", price = "", commission = ""),
        )

        assertTrue(result is AddTransactionResult.Success)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(970.0, updated.cashBalance, 0.001)
        assertEquals(1_000.0, updated.investedCapital, 0.001)
    }

    @Test
    fun `invalid form returns field errors`() = runTest {
        val repo = repository(deposit(1_000.0))
        val result = AddTransactionUseCase(repo).execute(
            1,
            TransactionForm(
                type = TransactionType.COMPRA,
                dateText = "",
                quantityText = "",
                priceText = "",
                commissionText = "",
                currency = Currency.MXN,
            ),
        )

        assertTrue(result is AddTransactionResult.Error)
    }

    @Test
    fun `retiro total empties cash and capital`() = runTest {
        val repo = repository(deposit(1_000.0))
        val result = AddTransactionUseCase(repo).execute(
            1,
            form(TransactionType.RETIRO, quantity = "1000", price = "", commission = ""),
        )

        assertTrue(result is AddTransactionResult.Success)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(0.0, updated.cashBalance, 0.001)
        assertEquals(0.0, updated.investedCapital, 0.001)
    }

    @Test
    fun `retiro of zero is rejected before persisting`() = runTest {
        val repo = repository(deposit(1_000.0))
        val before = repo.observeInvestment(1).first()!!
        val result = AddTransactionUseCase(repo).execute(
            1,
            form(TransactionType.RETIRO, quantity = "0", price = "", commission = ""),
        )

        assertTrue(result is AddTransactionResult.Error)
        assertEquals(before.cashBalance, repo.observeInvestment(1).first()!!.cashBalance, 0.001)
        assertEquals(1, repo.getTransactions(1).size)
    }

    @Test
    fun `negative retiro is rejected before persisting`() = runTest {
        val repo = repository(deposit(1_000.0))
        val before = repo.observeInvestment(1).first()!!
        val result = AddTransactionUseCase(repo).execute(
            1,
            form(TransactionType.RETIRO, quantity = "-50", price = "", commission = ""),
        )

        assertTrue(result is AddTransactionResult.Error)
        assertEquals(before.cashBalance, repo.observeInvestment(1).first()!!.cashBalance, 0.001)
        assertEquals(1, repo.getTransactions(1).size)
    }
}
