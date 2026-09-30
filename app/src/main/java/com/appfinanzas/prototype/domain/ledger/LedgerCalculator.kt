package com.appfinanzas.prototype.domain.ledger

import com.appfinanzas.prototype.domain.money.Rounding
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType

data class LedgerState(
    val cashBalance: Double,
    val quantity: Double,
    val averageCost: Double,
    val investedCapital: Double,
    val realizedProfit: Double,
    val currentPrice: Double,
    val currentValue: Double,
    val returnPercentage: Double,
)

object LedgerCalculator {

    fun recompute(currentPrice: Double, transactions: List<Transaction>): LedgerState {
        var cash = 0.0
        var quantity = 0.0
        var averageCost = 0.0
        var contributed = 0.0
        var realized = 0.0
        var marketPrice = currentPrice

        for (transaction in ordered(transactions)) {
            when (transaction.type) {
                TransactionType.DEPOSITO -> {
                    cash = Rounding.money(cash + transaction.total)
                    contributed = Rounding.money(contributed + transaction.total)
                }
                TransactionType.RETIRO -> {
                    cash = Rounding.money(cash - transaction.total)
                    contributed = Rounding.money(maxOf(0.0, contributed - transaction.total))
                }
                TransactionType.COMPRA -> {
                    val cost = purchaseCost(transaction)
                    cash = Rounding.money(cash - cost)
                    val newQuantity = quantity + transaction.quantity
                    averageCost = if (newQuantity > 0.0) {
                        Rounding.money((averageCost * quantity + cost) / newQuantity)
                    } else {
                        0.0
                    }
                    quantity = newQuantity
                    marketPrice = transaction.price
                }
                TransactionType.VENTA -> {
                    val proceeds = saleProceeds(transaction)
                    realized = Rounding.money(realized + proceeds - averageCost * transaction.quantity)
                    cash = Rounding.money(cash + proceeds)
                    quantity -= transaction.quantity
                    marketPrice = transaction.price
                }
                TransactionType.COMISION -> {
                    cash = Rounding.money(cash - transaction.total)
                }
                TransactionType.DIVIDENDO,
                TransactionType.INTERES -> {
                    cash = Rounding.money(cash + transaction.total)
                }
            }
        }

        val currentValue = Rounding.money(cash + quantity * marketPrice)
        val performance = if (contributed > 0.0) {
            Rounding.percentage((currentValue - contributed) / contributed * 100.0)
        } else {
            0.0
        }

        return LedgerState(
            cashBalance = cash,
            quantity = quantity,
            averageCost = averageCost,
            investedCapital = contributed,
            realizedProfit = realized,
            currentPrice = marketPrice,
            currentValue = currentValue,
            returnPercentage = performance,
        )
    }

    fun validate(transactions: List<Transaction>): List<String> {
        val errors = mutableListOf<String>()
        var cash = 0.0
        var quantity = 0.0

        for (transaction in ordered(transactions)) {
            when (transaction.type) {
                TransactionType.DEPOSITO -> cash += transaction.total
                TransactionType.RETIRO -> {
                    if (transaction.total > cash + 0.0001) {
                        errors += "El retiro excede el efectivo disponible"
                    }
                    cash -= transaction.total
                }
                TransactionType.COMPRA -> {
                    val cost = purchaseCost(transaction)
                    if (cost > cash + 0.0001) {
                        errors += "El efectivo disponible no alcanza para la compra"
                    }
                    cash -= cost
                    quantity += transaction.quantity
                }
                TransactionType.VENTA -> {
                    if (transaction.quantity > quantity + 0.0001) {
                        errors += "La cantidad excede la posición actual"
                    }
                    cash += saleProceeds(transaction)
                    quantity -= transaction.quantity
                }
                TransactionType.COMISION -> cash -= transaction.total
                TransactionType.DIVIDENDO,
                TransactionType.INTERES -> cash += transaction.total
            }

            if (cash < -0.0001) {
                errors += "El efectivo disponible no puede ser negativo"
            }
            if (quantity < -0.0001) {
                errors += "La cantidad de unidades no puede ser negativa"
            }
        }

        return errors.distinct()
    }

    fun purchaseCost(transaction: Transaction): Double =
        Rounding.money(transaction.quantity * transaction.price + transaction.commission)

    fun saleProceeds(transaction: Transaction): Double =
        Rounding.money(transaction.quantity * transaction.price - transaction.commission)

    private fun ordered(transactions: List<Transaction>): List<Transaction> =
        transactions.sortedWith(compareBy({ it.date }, { it.id }))
}
