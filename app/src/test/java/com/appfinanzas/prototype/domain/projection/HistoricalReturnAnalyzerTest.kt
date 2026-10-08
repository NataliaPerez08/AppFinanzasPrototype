package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.data.priceHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoricalReturnAnalyzerTest {

    private val loose = HistoricalEligibility(minObservations = 3, minSpanDays = 30)

    @Test
    fun `returns null when there are too few observations`() {
        val history = priceHistory(prices = listOf(100.0, 105.0), stepDays = 30)

        assertNull(HistoricalReturnAnalyzer.analyze(history, loose))
    }

    @Test
    fun `returns null when the sample span is too short`() {
        val history = priceHistory(prices = listOf(100.0, 101.0, 102.0, 103.0), stepDays = 5)

        assertNull(HistoricalReturnAnalyzer.analyze(history, loose))
    }

    @Test
    fun `ignores invalid price points`() {
        val history = priceHistory(prices = listOf(100.0, 0.0, 105.0, 108.0, Double.NaN), stepDays = 30)

        val stats = HistoricalReturnAnalyzer.analyze(history, loose)

        assertNotNull(stats)
        assertEquals(3, stats!!.observationCount)
    }

    @Test
    fun `computes cumulative annualized and sample period`() {
        val prices = List(13) { 100.0 + it }
        val history = priceHistory(prices = prices, stepDays = 30)

        val stats = HistoricalReturnAnalyzer.analyze(history, loose)!!

        assertEquals(0.12, stats.cumulativeReturn, 0.001)
        assertEquals(13, stats.observationCount)
        assertEquals(360, stats.samplePeriodDays)
        assertTrue(stats.annualizedReturn > 0.11)
        assertTrue(stats.annualizedReturn < 0.13)
    }

    @Test
    fun `annualizes volatility from observed returns`() {
        val history = priceHistory(prices = listOf(100.0, 110.0, 99.0, 115.0, 100.0), stepDays = 30)

        val stats = HistoricalReturnAnalyzer.analyze(history, loose)!!

        assertTrue(stats.annualVolatility > 0.0)
        assertTrue(stats.annualVolatility.isFinite())
    }
}
