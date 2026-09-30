package com.appfinanzas.prototype.domain.ledger

import com.appfinanzas.prototype.domain.model.Investment

fun Investment.withLedger(state: LedgerState): Investment = copy(
    quantity = state.quantity,
    averageCost = state.averageCost,
    cashBalance = state.cashBalance,
    investedCapital = state.investedCapital,
    realizedProfit = state.realizedProfit,
    currentPrice = state.currentPrice,
    currentValue = state.currentValue,
    returnPercentage = state.returnPercentage,
)
