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
        assertEquals("investmentId", Routes.ARG_INVESTMENT_ID)
    }
}
