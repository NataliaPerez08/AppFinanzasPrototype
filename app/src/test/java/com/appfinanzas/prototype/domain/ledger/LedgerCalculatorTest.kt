package com.appfinanzas.prototype.domain.ledger

import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LedgerCalculatorTest {

    private val date: LocalDate = LocalDate.of(2026, 9, 1)

    private fun tx(
        id: Long,
        type: TransactionType,
        quantity: Double = 0.0,
        price: Double = 0.0,
        commission: Double = 0.0,
        total: Double = 0.0,
        day: Long = 0,
    ) = Transaction(
        id = id,
        investmentId = 1,
        type = type,
        date = date.plusDays(day),
        quantity = quantity,
        price = price,
        commission = commission,
        total = total,
        currency = Currency.MXN,
    )

    private fun deposit(amount: Double, id: Long = 1, day: Long = 0) =
        tx(id = id, type = TransactionType.DEPOSITO, total = amount, day = day)

    private fun buy(quantity: Double, price: Double, commission: Double = 0.0, id: Long, day: Long = 0) =
        tx(id = id, type = TransactionType.COMPRA, quantity = quantity, price = price, commission = commission, total = quantity * price + commission, day = day)

    private fun sell(quantity: Double, price: Double, commission: Double = 0.0, id: Long, day: Long = 0) =
        tx(id = id, type = TransactionType.VENTA, quantity = quantity, price = price, commission = commission, total = quantity * price - commission, day = day)

    @Test
    fun `deposit only becomes available cash`() {
        val state = LedgerCalculator.recompute(0.0, listOf(deposit(1_000.0)))

        assertEquals(1_000.0, state.cashBalance, 0.001)
        assertEquals(1_000.0, state.investedCapital, 0.001)
        assertEquals(0.0, state.quantity, 0.001)
        assertEquals(1_000.0, state.currentValue, 0.001)
        assertEquals(0.0, state.returnPercentage, 0.001)
    }

    @Test
    fun `buy converts cash into units`() {
        val state = LedgerCalculator.recompute(0.0, listOf(deposit(1_000.0), buy(5.0, 100.0, id = 2)))

        assertEquals(500.0, state.cashBalance, 0.001)
        assertEquals(5.0, state.quantity, 0.001)
        assertEquals(100.0, state.averageCost, 0.001)
        assertEquals(1_000.0, state.investedCapital, 0.001)
        assertEquals(1_000.0, state.currentValue, 0.001)
    }

    @Test
    fun `partial sale realizes profit`() {
        val state = LedgerCalculator.recompute(
            0.0,
            listOf(deposit(1_000.0), buy(10.0, 100.0, id = 2), sell(4.0, 120.0, id = 3)),
        )

        assertEquals(6.0, state.quantity, 0.001)
        assertEquals(480.0, state.cashBalance, 0.001)
        assertEquals(80.0, state.realizedProfit, 0.001)
        assertEquals(1_200.0, state.currentValue, 0.001)
        assertEquals(20.0, state.returnPercentage, 0.001)
    }

    @Test
    fun `total sale closes position`() {
        val state = LedgerCalculator.recompute(
            0.0,
            listOf(deposit(1_000.0), buy(10.0, 100.0, id = 2), sell(10.0, 100.0, id = 3)),
        )

        assertEquals(0.0, state.quantity, 0.001)
        assertEquals(1_000.0, state.cashBalance, 0.001)
        assertEquals(0.0, state.realizedProfit, 0.001)
    }

    @Test
    fun `withdrawal reduces cash and capital`() {
        val state = LedgerCalculator.recompute(
            0.0,
            listOf(deposit(1_000.0), tx(id = 2, type = TransactionType.RETIRO, total = 300.0)),
        )

        assertEquals(700.0, state.cashBalance, 0.001)
        assertEquals(700.0, state.investedCapital, 0.001)
    }

    @Test
    fun `commission reduces cash without changing capital`() {
        val state = LedgerCalculator.recompute(
            0.0,
            listOf(deposit(1_000.0), tx(id = 2, type = TransactionType.COMISION, total = 25.0)),
        )

        assertEquals(975.0, state.cashBalance, 0.001)
        assertEquals(1_000.0, state.investedCapital, 0.001)
    }

    @Test
    fun `dividend and interest increase cash`() {
        val state = LedgerCalculator.recompute(
            0.0,
            listOf(
                deposit(1_000.0),
                tx(id = 2, type = TransactionType.DIVIDENDO, total = 40.0),
                tx(id = 3, type = TransactionType.INTERES, total = 10.0),
            ),
        )

        assertEquals(1_050.0, state.cashBalance, 0.001)
        assertEquals(0.0, state.quantity, 0.001)
    }

    @Test
    fun `same day ordering uses stable id`() {
        val state = LedgerCalculator.recompute(
            0.0,
            listOf(buy(5.0, 100.0, id = 2), deposit(1_000.0, id = 1)),
        )

        assertEquals(5.0, state.quantity, 0.001)
        assertEquals(500.0, state.cashBalance, 0.001)
    }

    @Test
    fun `rounds monetary values to two decimals`() {
        val state = LedgerCalculator.recompute(0.0, listOf(deposit(100.0), buy(3.0, 33.333, id = 2)))

        assertEquals(0.0, state.cashBalance, 0.01)
    }

    @Test
    fun `validate rejects sale above position`() {
        val errors = LedgerCalculator.validate(listOf(deposit(1_000.0), sell(5.0, 100.0, id = 2)))
        assertTrue(errors.isNotEmpty())
    }

    @Test
    fun `validate rejects withdrawal above cash`() {
        val errors = LedgerCalculator.validate(
            listOf(deposit(100.0), tx(id = 2, type = TransactionType.RETIRO, total = 500.0)),
        )
        assertTrue(errors.isNotEmpty())
    }

    @Test
    fun `validate rejects purchase above cash`() {
        val errors = LedgerCalculator.validate(listOf(deposit(50.0), buy(5.0, 100.0, id = 2)))
        assertTrue(errors.isNotEmpty())
    }

    @Test
    fun `validate accepts a consistent ledger`() {
        val errors = LedgerCalculator.validate(
            listOf(deposit(1_000.0), buy(5.0, 100.0, id = 2), sell(2.0, 120.0, id = 3)),
        )
        assertFalse(errors.isNotEmpty())
    }
}
