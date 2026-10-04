package com.appfinanzas.prototype.domain.usecase

import com.appfinanzas.prototype.data.TestInvestmentRepository
import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.InvestmentCategory
import com.appfinanzas.prototype.domain.model.InvestmentType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPortfolioSummaryTest {

    private val useCase = GetPortfolioSummary(TestInvestmentRepository())

    @Test
    fun `computes portfolio metrics`() = runTest {
        val summary = useCase.summarize(
            listOf(
                sampleInvestment(id = 1, currentValue = 1_000.0, investedCapital = 800.0),
                sampleInvestment(id = 2, name = "CETES", type = InvestmentType.CETES, currentValue = 500.0, investedCapital = 500.0),
            ),
        )

        assertEquals(1_500.0, summary.portfolioValue, 0.001)
        assertEquals(1_300.0, summary.investedCapital, 0.001)
        assertEquals(200.0, summary.profit, 0.001)
        assertEquals(15.384, summary.performance, 0.001)
        assertEquals(2, summary.investmentCount)
    }

    @Test
    fun `empty portfolio yields zeros`() = runTest {
        val summary = useCase.summarize(emptyList())

        assertEquals(0.0, summary.portfolioValue, 0.001)
        assertEquals(0.0, summary.profit, 0.001)
        assertEquals(0.0, summary.performance, 0.001)
        assertTrue(summary.byCategory.isEmpty())
        assertTrue(summary.byInstitution.isEmpty())
        assertTrue(summary.byCurrency.isEmpty())
    }

    @Test
    fun `groups allocation by category`() = runTest {
        val summary = useCase.summarize(
            listOf(
                sampleInvestment(id = 1, currentValue = 100.0, type = InvestmentType.ACCION),
                sampleInvestment(id = 2, name = "CETES", type = InvestmentType.CETES, currentValue = 200.0),
                sampleInvestment(id = 3, name = "NU", type = InvestmentType.SOFIPO, currentValue = 300.0),
            ),
        )

        assertEquals(3, summary.byCategory.size)
        assertEquals(InvestmentCategory.SOFIPO, summary.byCategory[0].category)
        assertEquals(300.0, summary.byCategory[0].value, 0.001)
    }

    @Test
    fun `groups allocation by institution and currency`() = runTest {
        val gbm = Institution(id = 1, name = "GBM", kind = "Casa de Bolsa")
        val nu = Institution(id = 3, name = "NU", kind = "SOFIPO")
        val summary = useCase.summarize(
            listOf(
                sampleInvestment(id = 1, institution = gbm, currency = Currency.MXN, currentValue = 1_000.0),
                sampleInvestment(id = 2, name = "NU", institution = nu, currency = Currency.MXN, currentValue = 2_000.0),
                sampleInvestment(id = 3, name = "VOO", institution = gbm, currency = Currency.USD, currentValue = 3_000.0),
            ),
        )

        assertEquals("GBM", summary.byInstitution[0].label)
        assertEquals(61_000.0, summary.byInstitution[0].value, 0.001)
        assertEquals("NU", summary.byInstitution[1].label)
        assertEquals(2_000.0, summary.byInstitution[1].value, 0.001)

        assertEquals(2, summary.byCurrency.size)
        assertEquals("USD", summary.byCurrency[0].label)
        assertEquals(60_000.0, summary.byCurrency[0].value, 0.001)
        assertEquals("MXN", summary.byCurrency[1].label)
        assertEquals(3_000.0, summary.byCurrency[1].value, 0.001)
    }

    @Test
    fun `every allocation dimension reconciles to portfolio value`() = runTest {
        val summary = useCase.summarize(
            listOf(
                sampleInvestment(id = 1, currentValue = 100.0),
                sampleInvestment(id = 2, name = "CETES", type = InvestmentType.CETES, currentValue = 200.0),
                sampleInvestment(id = 3, name = "NU", institution = Institution(3, "NU", "SOFIPO"), currentValue = 300.0),
            ),
        )

        assertEquals(summary.portfolioValue, summary.byCategory.sumOf { it.value }, 0.001)
        assertEquals(summary.portfolioValue, summary.byInstitution.sumOf { it.value }, 0.001)
        assertEquals(summary.portfolioValue, summary.byCurrency.sumOf { it.value }, 0.001)
    }

    @Test
    fun `aportacion no se contabiliza como rendimiento`() = runTest {
        val summary = useCase.summarize(
            listOf(
                sampleInvestment(id = 1, currentValue = 10_000.0, investedCapital = 10_000.0),
            ),
        )

        assertEquals(0.0, summary.profit, 0.001)
        assertEquals(0.0, summary.performance, 0.001)
    }

    @Test
    fun `observes flow from repository`() = runTest {
        val repo = TestInvestmentRepository(investments = listOf(sampleInvestment(currentValue = 500.0)))
        val summary = GetPortfolioSummary(repo).observe().first()

        assertEquals(500.0, summary.portfolioValue, 0.001)
    }
}
