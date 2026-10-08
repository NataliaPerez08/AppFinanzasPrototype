package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.domain.money.CurrencyConverter
import com.appfinanzas.prototype.domain.model.AppSettings
import com.appfinanzas.prototype.domain.model.Investment
import com.appfinanzas.prototype.domain.model.InvestmentType
import com.appfinanzas.prototype.domain.model.PricePoint
import com.appfinanzas.prototype.domain.model.ProjectionStrategy
import kotlin.random.Random

data class AssetProjection(
    val investmentId: Long,
    val label: String,
    val investmentType: InvestmentType,
    val strategy: ProjectionStrategy,
    val currentValue: Double,
    val projectedValue: Double,
    val expectedGain: Double,
    val contributions: Double,
    val withdrawals: Double,
)

data class InstitutionProjection(
    val name: String,
    val currentValue: Double,
    val projectedValue: Double,
    val expectedGain: Double,
    val contributions: Double,
    val withdrawals: Double,
    val assets: List<AssetProjection>,
)

data class PortfolioProjection(
    val currentValue: Double,
    val projectedValue: Double,
    val expectedGain: Double,
    val contributions: Double,
    val withdrawals: Double,
    val assets: List<AssetProjection>,
    val institutions: List<InstitutionProjection>,
)

class PortfolioProjectionEngine(
    private val engines: Map<ProjectionStrategy, ProjectionEngine> = mapOf(
        ProjectionStrategy.FIXED_RATE to FixedRateProjectionEngine(),
        ProjectionStrategy.COMPOUND_INTEREST to CompoundInterestProjectionEngine(),
        ProjectionStrategy.HISTORICAL_RETURN to HistoricalReturnProjectionEngine(),
        ProjectionStrategy.MANUAL to ManualProjectionEngine(),
    ),
    private val monteCarloEngine: MonteCarloProjectionEngine = MonteCarloProjectionEngine(),
    private val eligibility: HistoricalEligibility = HistoricalEligibility(),
) {

    private data class AssetSpec(
        val investmentId: Long,
        val label: String,
        val investmentType: InvestmentType,
        val institutionName: String,
        val strategy: ProjectionStrategy,
        val input: ProjectionInput,
    )

    fun project(
        investments: List<Investment>,
        settings: AppSettings,
        horizonMonths: Int,
        parameters: ProjectionParameters,
        historyByInvestment: Map<Long, List<PricePoint>> = emptyMap(),
        monthlyContribution: Double = 0.0,
    ): PortfolioProjection {
        val assets = specs(investments, settings, horizonMonths, parameters, historyByInvestment, monthlyContribution)
            .map { spec ->
                spec to centralEngine(spec.strategy).project(spec.input).toAssetProjection(spec)
            }

        val institutions = assets
            .groupBy { (spec, _) -> spec.institutionName }
            .map { (name, group) ->
                InstitutionProjection(
                    name = name,
                    currentValue = group.sumOf { it.second.currentValue },
                    projectedValue = group.sumOf { it.second.projectedValue },
                    expectedGain = group.sumOf { it.second.expectedGain },
                    contributions = group.sumOf { it.second.contributions },
                    withdrawals = group.sumOf { it.second.withdrawals },
                    assets = group.map { it.second },
                )
            }

        val assetProjections = assets.map { it.second }
        return PortfolioProjection(
            currentValue = assetProjections.sumOf { it.currentValue },
            projectedValue = assetProjections.sumOf { it.projectedValue },
            expectedGain = assetProjections.sumOf { it.expectedGain },
            contributions = assetProjections.sumOf { it.contributions },
            withdrawals = assetProjections.sumOf { it.withdrawals },
            assets = assetProjections,
            institutions = institutions,
        )
    }

    fun projectDistribution(
        investments: List<Investment>,
        settings: AppSettings,
        horizonMonths: Int,
        parameters: ProjectionParameters,
        historyByInvestment: Map<Long, List<PricePoint>> = emptyMap(),
        monthlyContribution: Double = 0.0,
    ): ProjectionDistribution? {
        val specs = specs(investments, settings, horizonMonths, parameters, historyByInvestment, monthlyContribution)
        val probabilistic = specs.filter { it.strategy == ProjectionStrategy.MONTE_CARLO }
        if (probabilistic.isEmpty()) return null

        val deterministicTotal = specs
            .filter { it.strategy != ProjectionStrategy.MONTE_CARLO }
            .sumOf { centralEngine(it.strategy).project(it.input).projectedValue }

        val randoms = probabilistic.mapIndexed { index, _ ->
            Random(MonteCarloProjectionEngine.DEFAULT_SEED + index)
        }
        val totals = ArrayList<Double>(monteCarloEngine.simulations)
        repeat(monteCarloEngine.simulations) {
            var total = deterministicTotal
            probabilistic.forEachIndexed { index, spec ->
                total += monteCarloEngine.simulateFinal(spec.input, randoms[index])
            }
            totals += total
        }
        return ProjectionDistribution.of(totals)
    }

    fun projectSeries(
        investments: List<Investment>,
        settings: AppSettings,
        horizonMonths: Int,
        parameters: ProjectionParameters,
        historyByInvestment: Map<Long, List<PricePoint>> = emptyMap(),
        monthlyContribution: Double = 0.0,
    ): ProjectionSeries {
        val horizon = horizonMonths.coerceAtLeast(0)
        val specs = specs(investments, settings, horizon, parameters, historyByInvestment, monthlyContribution)
        val probabilistic = specs.filter { it.strategy == ProjectionStrategy.MONTE_CARLO }
        val deterministic = specs.filter { it.strategy != ProjectionStrategy.MONTE_CARLO }

        val deterministicTotals = DoubleArray(horizon + 1)
        deterministic.forEach { spec ->
            for (month in 0..horizon) {
                deterministicTotals[month] += if (month == 0) {
                    spec.input.currentValue
                } else {
                    centralEngine(spec.strategy).project(spec.input.copy(horizonMonths = month)).projectedValue
                }
            }
        }

        if (probabilistic.isEmpty()) {
            return ProjectionSeries(
                months = (0..horizon).map { month ->
                    val value = deterministicTotals[month]
                    ProjectionDistribution(value, value, value, value, value, value)
                },
                hasProbability = false,
            )
        }

        val randoms = probabilistic.mapIndexed { index, _ ->
            Random(MonteCarloProjectionEngine.DEFAULT_SEED + index)
        }
        // ponytail: (horizon+1) x simulations doubles (~10MB at 10Y/10k) kept in memory for the band
        val totalsByMonth = List(horizon + 1) { DoubleArray(monteCarloEngine.simulations) }
        for (simulation in 0 until monteCarloEngine.simulations) {
            val paths = probabilistic.mapIndexed { index, spec ->
                monteCarloEngine.simulatePath(spec.input, randoms[index])
            }
            for (month in 0..horizon) {
                var total = deterministicTotals[month]
                paths.forEach { total += it[month] }
                totalsByMonth[month][simulation] = total
            }
        }
        return ProjectionSeries(
            months = totalsByMonth.map { ProjectionDistribution.of(it.toList()) },
            hasProbability = true,
        )
    }

    private fun specs(
        investments: List<Investment>,
        settings: AppSettings,
        horizonMonths: Int,
        parameters: ProjectionParameters,
        historyByInvestment: Map<Long, List<PricePoint>>,
        monthlyContribution: Double,
    ): List<AssetSpec> {
        val converted = investments.map { investment ->
            investment to CurrencyConverter.convert(
                investment.currentValue,
                investment.currency,
                settings.baseCurrency,
                settings.usdToMxnRate,
            )
        }
        val totalValue = converted.sumOf { it.second }
        return converted.map { (investment, value) ->
            val history = historyByInvestment[investment.id].orEmpty()
            val resolved = investment.projectionStrategy ?: ProjectionStrategyResolver.resolve(investment.type)
            val stats = HistoricalReturnAnalyzer.analyze(history, eligibility)
            // ponytail: historical assets without enough data fall back to compound instead of fabricating a trend
            val strategy = if (resolved == ProjectionStrategy.HISTORICAL_RETURN && stats == null) {
                ProjectionStrategy.COMPOUND_INTEREST
            } else {
                resolved
            }
            // Observed performance drives probabilistic assets, unless the user overrode the parameters.
            val hasOverride = investment.projectionReturn != null || investment.projectionVolatility != null
            var effectiveParameters = if (stats != null && resolved == ProjectionStrategy.MONTE_CARLO && !hasOverride) {
                parameters.copy(
                    annualReturn = stats.annualizedReturn * 100.0,
                    annualVolatility = stats.annualVolatility * 100.0,
                )
            } else {
                parameters
            }
            investment.projectionReturn?.let { effectiveParameters = effectiveParameters.copy(annualReturn = it) }
            investment.projectionVolatility?.let { effectiveParameters = effectiveParameters.copy(annualVolatility = it) }
            val share = if (totalValue > 0.0) value / totalValue else 0.0
            val contribution = monthlyContribution.finiteOrZero() * share
            AssetSpec(
                investmentId = investment.id,
                label = investment.name,
                investmentType = investment.type,
                institutionName = investment.institution.name,
                strategy = strategy,
                input = ProjectionInput(
                    currentValue = value,
                    horizonMonths = horizonMonths,
                    contributions = if (contribution > 0.0) listOf(Contribution(contribution)) else emptyList(),
                    parameters = effectiveParameters,
                    history = history,
                ),
            )
        }
    }

    private fun centralEngine(strategy: ProjectionStrategy): ProjectionEngine =
        engines[strategy]
            ?: engines.getValue(ProjectionStrategy.COMPOUND_INTEREST)

    private fun ProjectionResult.toAssetProjection(spec: AssetSpec) = AssetProjection(
        investmentId = spec.investmentId,
        label = spec.label,
        investmentType = spec.investmentType,
        strategy = spec.strategy,
        currentValue = currentValue,
        projectedValue = projectedValue,
        expectedGain = expectedGain,
        contributions = contributions,
        withdrawals = withdrawals,
    )
}
