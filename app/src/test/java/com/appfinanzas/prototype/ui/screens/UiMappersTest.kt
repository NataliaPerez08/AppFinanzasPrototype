package com.appfinanzas.prototype.ui.screens

import com.appfinanzas.prototype.data.sampleInvestment
import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.InvestmentDetail
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.InvestmentsData
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import com.appfinanzas.prototype.domain.model.NamedAllocation
import com.appfinanzas.prototype.domain.model.PortfolioSummary
import com.appfinanzas.prototype.ui.screens.investments.toUiState
import com.appfinanzas.prototype.ui.screens.portfolio.toUiState
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class UiMappersTest {

    @Test
    fun `investment maps to ui row`() {
        val investment = sampleInvestment(
            id = 4,
            name = "CETES",
            type = InvestmentType.CETES,
            currentValue = 500.0,
            returnPercentage = 7.1,
        )

        val data = InvestmentsData(totalValue = 1_000.0, totalProfit = 100.0, investments = listOf(investment))
        val state = data.toUiState(filter = null)

        assertEquals(1_000.0, state.totalValue, 0.001)
        assertEquals(100.0, state.totalProfit, 0.001)
        assertEquals(1, state.investments.size)
        val row = state.investments[0]
        assertEquals(4L, row.id)
        assertEquals("CETES", row.name)
        assertEquals("GBM · Renta fija", row.subtitle)
        assertEquals(500.0, row.value, 0.001)
        assertEquals(7.1, row.change, 0.001)
        assertEquals(Currency.MXN, row.currency)
    }

    @Test
    fun `dashboard allocation is mapped to percentages`() {
        val investment = sampleInvestment(currentValue = 400.0)
        val data = InvestmentsData(totalValue = 400.0, totalProfit = 0.0, investments = listOf(investment))
        val ui = data.toUiState(filter = null)

        assertEquals(400.0, ui.totalValue, 0.001)
        assertEquals("Renta variable", ui.investments[0].subtitle.split(" · ")[1])
    }

    @Test
    fun `investment detail maps transactions with formatted dates`() {
        val investment = sampleInvestment(id = 1, currentValue = 1_000.0, investedCapital = 800.0)
        val detail = InvestmentDetail(
            investment = investment,
            transactions = listOf(
                Transaction(
                    id = 1,
                    investmentId = 1,
                    type = TransactionType.DEPOSITO,
                    date = LocalDate.of(2025, 6, 5),
                    quantity = 0.0,
                    price = 0.0,
                    commission = 0.0,
                    total = 500.0,
                    currency = Currency.MXN,
                ),
            ),
        )

        val state = detail.toUiState()

        assertEquals("VOO", state.name)
        assertEquals("Descripción VOO · GBM · Renta variable", state.subtitle)
        assertEquals(200.0, state.profit, 0.001)
        assertEquals(25.0, state.performance, 0.001)
        assertEquals(1, state.transactions.size)
        assertEquals("DEPOSITO", state.transactions[0].typeLabel)
        assertEquals("05/06/2025", state.transactions[0].dateLabel)
    }

    @Test
    fun `portfolio summary maps allocations to percentages`() {
        val summary = PortfolioSummary(
            portfolioValue = 5_000.0,
            investedCapital = 4_000.0,
            profit = 1_000.0,
            performance = 25.0,
            byCategory = emptyList(),
            byInstitution = listOf(
                NamedAllocation("GBM", 3_000.0),
                NamedAllocation("NU", 2_000.0),
            ),
            byCurrency = listOf(NamedAllocation("MXN", 5_000.0)),
            investmentCount = 2,
        )

        val state = summary.toUiState()

        assertEquals(5_000.0, state.portfolioValue, 0.001)
        assertEquals(1_000.0, state.profit, 0.001)
        assertEquals(25.0, state.performance, 0.001)
        assertFalse(state.isEmpty)
        assertEquals("GBM", state.byInstitution[0].label)
        assertEquals(60f, state.byInstitution[0].percentage, 0.001f)
        assertEquals("NU", state.byInstitution[1].label)
        assertEquals(40f, state.byInstitution[1].percentage, 0.001f)
        assertEquals("MXN", state.byCurrency[0].label)
        assertEquals(100f, state.byCurrency[0].percentage, 0.001f)
    }
}