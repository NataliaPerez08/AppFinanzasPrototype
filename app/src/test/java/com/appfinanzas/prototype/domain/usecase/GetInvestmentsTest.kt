package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.InvestmentCategory
import com.appfinanzas.prototype.domain.model.InvestmentType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetInvestmentsTest {

    @Test
    fun `computes totals across all investments`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(
                sampleInvestment(id = 1, currentValue = 1_000.0, investedCapital = 800.0),
                sampleInvestment(id = 2, name = "CETES", type = InvestmentType.CETES, currentValue = 500.0, investedCapital = 500.0),
            ),
        )
        val data = GetInvestments(repo).observe(filter = null).first()

        assertEquals(1_500.0, data.totalValue, 0.001)
        assertEquals(200.0, data.totalProfit, 0.001)
        assertEquals(2, data.investments.size)
    }

    @Test
    fun `filters investments by category`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(
                sampleInvestment(id = 1, currentValue = 100.0),
                sampleInvestment(id = 2, name = "CETES", type = InvestmentType.CETES, currentValue = 200.0),
                sampleInvestment(id = 3, name = "NU", type = InvestmentType.SOFIPO, currentValue = 300.0),
            ),
        )
        val rentaFija = GetInvestments(repo).observe(filter = InvestmentCategory.RENTA_FIJA).first()

        assertEquals(listOf(2L), rentaFija.investments.map { it.id })
        assertEquals(600.0, rentaFija.totalValue, 0.001)
    }
}