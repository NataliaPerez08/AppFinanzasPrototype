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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GetInvestmentDetailTest {

    @Test
    fun `loads investment with transactions`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment(id = 7)),
            transactions = mapOf(
                7L to listOf(
                    Transaction(
                        id = 1,
                        investmentId = 7,
                        type = TransactionType.COMPRA,
                        date = LocalDate.of(2025, 1, 1),
                        quantity = 1.0,
                        price = 100.0,
                        commission = 0.0,
                        total = 100.0,
                        currency = Currency.MXN,
                    ),
                ),
            ),
        )
        val detail = GetInvestmentDetail(repo).observe(investmentId = 7).first()

        assertNotNull(detail)
        assertEquals(7L, detail!!.investment.id)
        assertEquals(1, detail.transactions.size)
    }

    @Test
    fun `returns null for unknown investment`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(id = 1)))
        val detail = GetInvestmentDetail(repo).observe(investmentId = 99).first()

        assertNull(detail)
    }
}