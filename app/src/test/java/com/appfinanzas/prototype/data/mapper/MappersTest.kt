package com.appfinanzas.prototype.data.mapper

import com.appfinanzas.prototype.domain.model.Currency
import com.appfinanzas.prototype.domain.model.Institution
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.ProjectionStrategy
import com.appfinanzas.prototype.domain.model.Transaction
import com.appfinanzas.prototype.domain.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {

    @Test
    fun `investment maps entity round trip`() {
        val domain = Investment(
            id = 3,
            name = "VOO",
            description = "Vanguard S&P 500 ETF",
            symbol = "VOO",
            type = InvestmentType.ETF_FONDO,
            institution = Institution(id = 1, name = "GBM", kind = "Casa de Bolsa"),
            currency = Currency.USD,
            currentPrice = 432.18,
            priceChange = 1.24,
            dailyChangePercentage = 0.29,
            quantity = 34.0,
            investedCapital = 160_000.0,
            currentValue = 185_430.0,
            dailyValueChange = 537.75,
            returnPercentage = 12.4,
            history = listOf(.8f, .6f, .4f),
        )

        val entity = InvestmentMapper.toEntity(domain)
        val restored = InvestmentMapper.toDomain(entity, domain.institution)

        assertEquals(domain, restored)
    }

    @Test
    fun `investment projection configuration round trips`() {
        val domain = Investment(
            id = 5,
            name = "VOO",
            description = "Vanguard",
            symbol = "VOO",
            type = InvestmentType.ETF_FONDO,
            institution = Institution(id = 1, name = "GBM", kind = "Casa de Bolsa"),
            currency = Currency.MXN,
            currentPrice = 100.0,
            priceChange = 0.0,
            dailyChangePercentage = 0.0,
            quantity = 1.0,
            investedCapital = 1_000.0,
            currentValue = 1_000.0,
            dailyValueChange = 0.0,
            returnPercentage = 0.0,
            history = emptyList(),
            projectionStrategy = ProjectionStrategy.MONTE_CARLO,
            projectionReturn = 12.5,
            projectionVolatility = 18.0,
        )

        val entity = InvestmentMapper.toEntity(domain)
        val restored = InvestmentMapper.toDomain(entity, domain.institution)

        assertEquals(domain, restored)
        assertEquals(ProjectionStrategy.MONTE_CARLO, restored.projectionStrategy)
        assertEquals(12.5, restored.projectionReturn!!, 0.001)
    }

    @Test
    fun `transaction maps entity round trip`() {
        val domain = Transaction(
            id = 9,
            investmentId = 3,
            type = TransactionType.COMPRA,
            date = LocalDate.of(2025, 1, 10),
            quantity = 2.0,
            price = 400.0,
            commission = 1.5,
            total = 801.5,
            currency = Currency.USD,
        )

        val entity = TransactionMapper.toEntity(domain)
        val restored = TransactionMapper.toDomain(entity)

        assertEquals(domain, restored)
    }

    @Test
    fun `float list csv round trip`() {
        assertEquals("0.8,0.6,0.4", listOf(.8f, .6f, .4f).toCsv())
        assertEquals(listOf(.8f, .6f, .4f), "0.8,0.6,0.4".toFloatList())
        assertEquals(emptyList<Float>(), "".toFloatList())
    }
}