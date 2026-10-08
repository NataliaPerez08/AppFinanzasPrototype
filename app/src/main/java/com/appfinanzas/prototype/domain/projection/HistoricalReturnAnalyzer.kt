package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.domain.model.PricePoint
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.pow
import kotlin.math.sqrt

data class HistoricalEligibility(
    val minObservations: Int = 12,
    val minSpanDays: Long = 90,
)

data class HistoricalStats(
    val cumulativeReturn: Double,
    val annualizedReturn: Double,
    val annualVolatility: Double,
    val observationCount: Int,
    val samplePeriodDays: Long,
    val firstDate: LocalDate,
    val lastDate: LocalDate,
)

object HistoricalReturnAnalyzer {

    fun analyze(
        history: List<PricePoint>,
        eligibility: HistoricalEligibility = HistoricalEligibility(),
    ): HistoricalStats? {
        val points = history
            .filter { it.price > 0.0 && it.price.isFinite() }
            .sortedBy { it.date }
        if (points.size < eligibility.minObservations) return null

        val first = points.first()
        val last = points.last()
        val spanDays = ChronoUnit.DAYS.between(first.date, last.date)
        if (spanDays <= 0L || spanDays < eligibility.minSpanDays) return null

        val years = spanDays / 365.25
        val growth = last.price / first.price
        val periodReturns = points.zipWithNext { previous, next -> next.price / previous.price - 1.0 }
        val mean = periodReturns.average()
        val variance = periodReturns.sumOf { (it - mean) * (it - mean) } / periodReturns.size
        val periodsPerYear = periodReturns.size / years

        return HistoricalStats(
            cumulativeReturn = growth - 1.0,
            annualizedReturn = growth.pow(1.0 / years) - 1.0,
            annualVolatility = sqrt(variance) * sqrt(periodsPerYear),
            observationCount = points.size,
            samplePeriodDays = spanDays,
            firstDate = first.date,
            lastDate = last.date,
        )
    }
}
