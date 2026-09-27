package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.InvestmentCategory
import com.appfinanzas.prototype.domain.model.InvestmentType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetDashboardSummaryTest {

    private val useCase = GetDashboardSummary(TestInvestmentRepository())

    @Test
    fun `summarizes empty portfolio`() = runTest {
        val summary = useCase.summarize(investments = emptyList(), history = listOf(.5f))

        assertEquals(0.0, summary.portfolioValue, 0.001)
        assertEquals(0.0, summary.investedCapital, 0.001)
        assertEquals(0.0, summary.profit, 0.001)
        assertEquals(0.0, summary.performance, 0.001)
        assertEquals(0, summary.investmentCount)
        assertEquals(emptyList<Any>(), summary.allocation)
    }

    @Test
    fun `computes portfolio totals and performance`() = runTest {
        val summary = useCase.summarize(
            investments = listOf(
                sampleInvestment(id = 1, currentValue = 1_000.0, investedCapital = 800.0, dailyValueChange = 20.0),
                sampleInvestment(id = 2, name = "CETES", type = InvestmentType.CETES, currentValue = 500.0, investedCapital = 500.0),
            ),
            history = listOf(.5f),
        )

        assertEquals(1_500.0, summary.portfolioValue, 0.001)
        assertEquals(1_300.0, summary.investedCapital, 0.001)
        assertEquals(200.0, summary.profit, 0.001)
        assertEquals(15.384, summary.performance, 0.001)
        assertEquals(30.0, summary.dailyChange, 0.001)
        assertEquals(2, summary.investmentCount)
    }

    @Test
    fun `groups allocation by category sorted descending`() = runTest {
        val summary = useCase.summarize(
            investments = listOf(
                sampleInvestment(id = 1, currentValue = 100.0),
                sampleInvestment(id = 2, name = "CETES", type = InvestmentType.CETES, currentValue = 200.0),
                sampleInvestment(id = 3, name = "AAPL", type = InvestmentType.ACCION, currentValue = 300.0),
                sampleInvestment(id = 4, name = "NU", type = InvestmentType.SOFIPO, currentValue = 50.0),
            ),
            history = emptyList(),
        )

        assertEquals(3, summary.allocation.size)
        assertEquals(InvestmentCategory.RENTA_VARIABLE, summary.allocation[0].category)
        assertEquals(400.0, summary.allocation[0].value, 0.001)
        assertEquals(InvestmentCategory.RENTA_FIJA, summary.allocation[1].category)
        assertEquals(InvestmentCategory.SOFIPO, summary.allocation[2].category)
    }

    @Test
    fun `observes combined flows`() = runTest {
        val repo = TestInvestmentRepository(
            investments = listOf(sampleInvestment()),
            history = listOf(.8f, .6f),
        )
        val summary = GetDashboardSummary(repo).observe().first()

        assertEquals(1_000.0, summary.portfolioValue, 0.001)
        assertEquals(listOf(.8f, .6f), summary.history)
    }
}