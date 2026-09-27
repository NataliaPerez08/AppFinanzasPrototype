package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.validation.TransactionForm
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddTransactionUseCaseTest {

    private fun form(
        type: TransactionType,
        quantity: String = "2",
        price: String = "100",
        commission: String = "1",
    ) = TransactionForm(
        type = type,
        dateText = "21/09/2026",
        quantityText = quantity,
        priceText = price,
        commissionText = commission,
        currency = Currency.MXN,
    )

    private suspend fun repositoryWithInvestment() = TestInvestmentRepository(
        investments = listOf(
            sampleInvestment(
                id = 1,
                quantity = 10.0,
                investedCapital = 800.0,
                currentValue = 1_000.0,
                currentPrice = 100.0,
            ),
        ),
    )

    @Test
    fun `compra increases quantity and capital`() = runTest {
        val repo = repositoryWithInvestment()
        val result = AddTransactionUseCase(repo).execute(1, form(TransactionType.COMPRA))

        assertTrue(result is AddTransactionResult.Success)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(12.0, updated.quantity, 0.001)
        assertEquals(1_001.0, updated.investedCapital, 0.001)
        assertEquals(1_200.0, updated.currentValue, 0.001)
        assertEquals(1, repo.observeTransactions(1).first().size)
    }

    @Test
    fun `venta reduces quantity`() = runTest {
        val repo = repositoryWithInvestment()
        val result = AddTransactionUseCase(repo).execute(1, form(TransactionType.VENTA))

        assertTrue(result is AddTransactionResult.Success)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(8.0, updated.quantity, 0.001)
        assertEquals(800.0 - 201.0, updated.investedCapital, 0.001)
    }

    @Test
    fun `venta exceeding position is a business error`() = runTest {
        val repo = repositoryWithInvestment()
        val result = AddTransactionUseCase(repo).execute(1, form(TransactionType.VENTA, quantity = "20"))

        assertTrue(result is AddTransactionResult.BusinessError)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(10.0, updated.quantity, 0.001)
    }

    @Test
    fun `dividendo does not change position`() = runTest {
        val repo = repositoryWithInvestment()
        val result = AddTransactionUseCase(repo).execute(
            1,
            TransactionForm(
                type = TransactionType.DIVIDENDO,
                dateText = "21/09/2026",
                quantityText = "50",
                priceText = "",
                commissionText = "",
                currency = Currency.MXN,
            ),
        )

        assertTrue(result is AddTransactionResult.Success)
        val updated = repo.observeInvestment(1).first()!!
        assertEquals(10.0, updated.quantity, 0.001)
        assertEquals(800.0, updated.investedCapital, 0.001)
    }

    @Test
    fun `retiro exceeding capital is a business error`() = runTest {
        val repo = repositoryWithInvestment()
        val result = AddTransactionUseCase(repo).execute(
            1,
            TransactionForm(
                type = TransactionType.RETIRO,
                dateText = "21/09/2026",
                quantityText = "5000",
                priceText = "",
                commissionText = "",
                currency = Currency.MXN,
            ),
        )

        assertTrue(result is AddTransactionResult.BusinessError)
    }

    @Test
    fun `invalid form returns field errors`() = runTest {
        val repo = repositoryWithInvestment()
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
}