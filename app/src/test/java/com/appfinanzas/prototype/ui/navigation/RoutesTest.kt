package com.appfinanzas.prototype.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutesTest {
    @Test
    fun `builds investment detail route with id`() {
        assertEquals("investmentDetail/42", Routes.investmentDetail(42L))
    }

    @Test
    fun `builds transaction route with investment id`() {
        assertEquals("addTransaction/7", Routes.addTransaction(7L))
    }

    @Test
    fun `route patterns retain id arguments`() {
        assertEquals("investmentDetail/{investmentId}", Routes.INVESTMENT_DETAIL)
        assertEquals("addTransaction/{investmentId}", Routes.ADD_TRANSACTION)
        assertEquals("editInvestment/{investmentId}", Routes.EDIT_INVESTMENT)
        assertEquals("editTransaction/{investmentId}/{transactionId}", Routes.EDIT_TRANSACTION)
        assertEquals("investmentId", Routes.ARG_INVESTMENT_ID)
        assertEquals("transactionId", Routes.ARG_TRANSACTION_ID)
    }

    @Test
    fun `builds edit routes with ids`() {
        assertEquals("editInvestment/42", Routes.editInvestment(42L))
        assertEquals("editTransaction/7/9", Routes.editTransaction(7L, 9L))
    }
}
