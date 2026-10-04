package com.appfinanzas.prototype.domain.ledger

import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LedgerVolumeTest {

    private val date: LocalDate = LocalDate.of(2026, 1, 1)

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

    private fun deposit(amount: Double, id: Long, day: Long) =
        tx(id = id, type = TransactionType.DEPOSITO, total = amount, day = day)

    private fun buy(quantity: Double, price: Double, id: Long, day: Long) =
        tx(id = id, type = TransactionType.COMPRA, quantity = quantity, price = price, total = quantity * price, day = day)

    private fun sell(quantity: Double, price: Double, id: Long, day: Long) =
        tx(id = id, type = TransactionType.VENTA, quantity = quantity, price = price, total = quantity * price, day = day)

    @Test
    fun `1000 transactions recompute under 50ms`() {
        val transactions = mutableListOf<Transaction>()
        var id = 1L
        var day = 0L
        var cash = 0.0
        var qty = 0.0

        repeat(1000) { i ->
            when (i % 4) {
                0 -> {
                    val amount = 100.0 + (i % 50)
                    transactions += deposit(amount, id++, day)
                    cash += amount
                }
                1 -> {
                    if (cash > 0) {
                        val q = 1.0 + (i % 10)
                        val p = 10.0 + (i % 5)
                        val cost = q * p
                        if (cost <= cash) {
                            transactions += buy(q, p, id++, day)
                            cash -= cost
                            qty += q
                        }
                    }
                }
                2 -> {
                    if (qty > 0) {
                        val q = minOf(qty, 1.0 + (i % 5))
                        val p = 12.0 + (i % 8)
                        transactions += sell(q, p, id++, day)
                        cash += q * p
                        qty -= q
                    }
                }
                3 -> {
                    transactions += tx(id = id++, type = TransactionType.DIVIDENDO, total = 5.0 + (i % 20), day = day)
                    cash += 5.0 + (i % 20)
                }
            }
            if (i % 10 == 9) day++
        }

        val start = System.nanoTime()
        val state = LedgerCalculator.recompute(15.0, transactions)
        val elapsedMs = (System.nanoTime() - start) / 1_000_000.0

        assertTrue("recompute took ${elapsedMs}ms, expected < 50ms", elapsedMs < 50.0)
        assertTrue("quantity should be non-negative", state.quantity >= 0.0)
        assertTrue("cash should be non-negative", state.cashBalance >= 0.0)
    }

    @Test
    fun `1000 transactions validate under 50ms`() {
        val transactions = mutableListOf<Transaction>()
        var id = 1L
        var day = 0L
        var cash = 0.0
        var qty = 0.0

        repeat(1000) { i ->
            when (i % 4) {
                0 -> {
                    val amount = 100.0 + (i % 50)
                    transactions += deposit(amount, id++, day)
                    cash += amount
                }
                1 -> {
                    if (cash > 0) {
                        val q = 1.0 + (i % 10)
                        val p = 10.0 + (i % 5)
                        val cost = q * p
                        if (cost <= cash) {
                            transactions += buy(q, p, id++, day)
                            cash -= cost
                            qty += q
                        }
                    }
                }
                2 -> {
                    if (qty > 0) {
                        val q = minOf(qty, 1.0 + (i % 5))
                        val p = 12.0 + (i % 8)
                        transactions += sell(q, p, id++, day)
                        cash += q * p
                        qty -= q
                    }
                }
                3 -> {
                    transactions += tx(id = id++, type = TransactionType.DIVIDENDO, total = 5.0 + (i % 20), day = day)
                    cash += 5.0 + (i % 20)
                }
            }
            if (i % 10 == 9) day++
        }

        val start = System.nanoTime()
        val errors = LedgerCalculator.validate(transactions)
        val elapsedMs = (System.nanoTime() - start) / 1_000_000.0

        assertTrue("validate took ${elapsedMs}ms, expected < 50ms", elapsedMs < 50.0)
        assertTrue("should have no errors for consistent ledger", errors.isEmpty())
    }

    @Test
    fun `5000 transactions recompute under 200ms`() {
        val transactions = mutableListOf<Transaction>()
        var id = 1L
        var day = 0L
        var cash = 0.0
        var qty = 0.0

        repeat(5000) { i ->
            when (i % 4) {
                0 -> {
                    val amount = 100.0 + (i % 50)
                    transactions += deposit(amount, id++, day)
                    cash += amount
                }
                1 -> {
                    if (cash > 0) {
                        val q = 1.0 + (i % 10)
                        val p = 10.0 + (i % 5)
                        val cost = q * p
                        if (cost <= cash) {
                            transactions += buy(q, p, id++, day)
                            cash -= cost
                            qty += q
                        }
                    }
                }
                2 -> {
                    if (qty > 0) {
                        val q = minOf(qty, 1.0 + (i % 5))
                        val p = 12.0 + (i % 8)
                        transactions += sell(q, p, id++, day)
                        cash += q * p
                        qty -= q
                    }
                }
                3 -> {
                    transactions += tx(id = id++, type = TransactionType.DIVIDENDO, total = 5.0 + (i % 20), day = day)
                    cash += 5.0 + (i % 20)
                }
            }
            if (i % 10 == 9) day++
        }

        val start = System.nanoTime()
        val state = LedgerCalculator.recompute(15.0, transactions)
        val elapsedMs = (System.nanoTime() - start) / 1_000_000.0

        assertTrue("recompute took ${elapsedMs}ms, expected < 200ms", elapsedMs < 200.0)
        assertTrue("quantity should be non-negative", state.quantity >= 0.0)
        assertTrue("cash should be non-negative", state.cashBalance >= 0.0)
    }

    @Test
    fun `1000 mixed transactions produce consistent state`() {
        val transactions = mutableListOf<Transaction>()
        var id = 1L
        var day = 0L
        var cash = 0.0
        var qty = 0.0
        var contributed = 0.0

        repeat(1000) { i ->
            when (i % 5) {
                0 -> {
                    val amount = 500.0
                    transactions += deposit(amount, id++, day)
                    cash += amount
                    contributed += amount
                }
                1 -> {
                    val q = 2.0
                    val p = 50.0
                    val cost = q * p
                    if (cost <= cash) {
                        transactions += buy(q, p, id++, day)
                        cash -= cost
                        qty += q
                    }
                }
                2 -> {
                    if (qty > 0) {
                        val q = 1.0
                        val p = 60.0
                        transactions += sell(q, p, id++, day)
                        cash += q * p
                        qty -= q
                    }
                }
                3 -> {
                    val amount = 50.0
                    if (amount <= cash) {
                        transactions += tx(id = id++, type = TransactionType.RETIRO, total = amount, day = day)
                        cash -= amount
                        contributed -= amount
                    }
                }
                4 -> {
                    transactions += tx(id = id++, type = TransactionType.COMISION, total = 5.0, day = day)
                    cash -= 5.0
                }
            }
            if (i % 7 == 6) day++
        }

        val state = LedgerCalculator.recompute(55.0, transactions)

        assertTrue("quantity non-negative", state.quantity >= 0.0)
        assertTrue("cash non-negative", state.cashBalance >= 0.0)
        assertEquals(contributed, state.investedCapital, 0.01)
    }
}
