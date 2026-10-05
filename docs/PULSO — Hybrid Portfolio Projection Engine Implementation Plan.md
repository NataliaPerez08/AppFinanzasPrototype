# PULSO — Hybrid Portfolio Projection Engine

## Objective

Implement a modular portfolio projection system capable of combining different projection methodologies depending on the characteristics of each investment.

The system must support:

- Fixed-rate projections.
- Compound-interest projections.
- Historical-return projections.
- Monte Carlo simulations.
- Manual projections.
- Portfolio-level aggregation.
- Projection aggregation by institution, account, and asset.
- Growth attribution.
- Contributions and withdrawals.
- Estimated taxes.
- Inflation-adjusted projections.
- Projection scenarios.
- Projection confidence ranges.

The fundamental architecture must follow:

```text
Asset
  ↓
Account
  ↓
Institution
  ↓
Portfolio
```

Each asset may use a different projection strategy.

The portfolio projection must aggregate the results without forcing all investments to use the same financial model.

---

# Core Principles

## 1. Separate projection from aggregation

Projection engines calculate the future behavior of individual investments.

Aggregation combines those projections into:

- account projections;
- institution projections;
- portfolio projections.

Do not implement a single monolithic projection function.

---

## 2. Use a common projection contract

Every projection engine must produce a standardized result.

The aggregation layer must not need to know how the result was calculated.

Example architecture:

```text
                    ProjectionEngine
                           │
       ┌───────────────────┼───────────────────┐
       ▼                   ▼                   ▼
Fixed / Compound      Historical          Monte Carlo
       │                   │                   │
       └───────────────────┼───────────────────┘
                           ▼
                    ProjectionResult
                           │
                           ▼
                 Portfolio Aggregator
```

---

## 3. Separate contributions from investment returns

Never present deposits as investment gains.

The system must distinguish:

```text
Portfolio Growth
    =
Contributions
+ Investment Returns
+ Distributions
- Withdrawals
- Estimated Taxes
```

This distinction must exist at the domain level, not only in the UI.

---

# Phase 1 — Projection Domain Model

## Goal

Create the common domain model used by every projection strategy.

Create:

```kotlin
enum class ProjectionStrategy {
    FIXED_RATE,
    COMPOUND_INTEREST,
    HISTORICAL_RETURN,
    MONTE_CARLO,
    MANUAL
}
```

Create a common engine interface.

Example:

```kotlin
interface ProjectionEngine {
    fun project(input: ProjectionInput): ProjectionResult
}
```

The exact implementation may use `suspend`, `Flow`, use cases, repositories, or other existing project conventions where appropriate.

Do not unnecessarily introduce new architectural patterns if equivalent abstractions already exist.

### ProjectionInput

It should contain at least:

```kotlin
data class ProjectionInput(
    val currentValue: BigDecimal,
    val horizonMonths: Int,
    val contributions: List<Contribution>,
    val withdrawals: List<Withdrawal>,
    val parameters: ProjectionParameters
)
```

Adapt types to the existing money/value representation used by the project.

Do not introduce `Double` for monetary values if the project already uses a safer monetary representation.

### ProjectionResult

Create a normalized result capable of representing deterministic and probabilistic projections.

Conceptually:

```kotlin
data class ProjectionResult(
    val currentValue: BigDecimal,
    val projectedValue: BigDecimal,
    val expectedGain: BigDecimal,
    val contributions: BigDecimal,
    val withdrawals: BigDecimal,

    val p10: BigDecimal?,
    val p25: BigDecimal?,
    val p50: BigDecimal?,
    val p75: BigDecimal?,
    val p90: BigDecimal?,

    val strategy: ProjectionStrategy
)
```

Deterministic engines may leave probability fields empty.

Monte Carlo projections must populate them.

### Acceptance Criteria

- All projection engines implement the same contract.
- Projection logic is independent from UI code.
- Projection logic is independently testable.
- Monetary calculations do not introduce avoidable floating-point errors.
- Existing portfolio calculations continue working.

---

# Phase 2 — Deterministic Projection Engines

## Goal

Implement predictable projection methods first.

Create:

```text
FixedRateProjectionEngine
CompoundInterestProjectionEngine
```

## Fixed Rate

Designed for instruments where the expected rate is known or explicitly configured.

Possible use cases:

- CETES;
- fixed-term investments;
- fixed-rate debt;
- manually configured instruments.

Consider:

- current principal;
- annual rate;
- maturity;
- contributions;
- withdrawals.

## Compound Interest

Designed for investments where returns are periodically reinvested.

Typical use case:

```text
SOFIPO
```

Support:

- principal;
- annual rate;
- compounding frequency;
- reinvestment;
- projection horizon;
- periodic contributions;
- withdrawals.

Do not hardcode this engine specifically to SOFIPOs.

SOFIPO is a consumer of the compound-interest engine, not the engine itself.

### Tests

At minimum test:

```text
100,000 principal
10% annual rate
12 months
no contributions
```

against the mathematically expected result.

Also test:

- monthly contributions;
- zero rate;
- negative rate where supported;
- different compounding frequencies;
- horizons shorter than one year;
- horizons longer than one year.

---

# Phase 3 — Historical Return Engine

## Goal

Allow investments with sufficient historical data to use their observed performance for projections.

Create:

```text
HistoricalReturnProjectionEngine
```

Calculate or retrieve:

```text
cumulativeReturn
annualizedReturn
annualVolatility
observationCount
samplePeriod
```

The engine must validate whether enough historical data exists.

Do not silently extrapolate from extremely short periods.

Create a configurable eligibility rule.

Example concept:

```text
47 days of data
→ insufficient

3 years of data
→ eligible
```

The exact threshold should be centralized and configurable.

### Output

Historical projections should provide at least:

```text
expectedAnnualReturn
projectedValue
expectedGain
historicalSamplePeriod
```

Volatility should also be calculated or made available because it will later feed Monte Carlo.

---

# Phase 4 — Monte Carlo Engine

## Goal

Implement probabilistic projections for variable-return investments.

Create:

```text
MonteCarloProjectionEngine
```

Initial implementation should support:

```text
expected annual return
annual volatility
projection horizon
periodic contributions
withdrawals
simulation count
```

Default simulation count:

```text
10,000
```

Make this configurable.

Supported initial horizons should include:

```text
3 months
6 months
1 year
3 years
5 years
10 years
```

Do not hardcode the engine exclusively to these horizons.

## Output

Monte Carlo must return a distribution rather than only one expected value.

Calculate:

```text
P10
P25
P50
P75
P90
mean
```

Use:

```text
P50
```

as the primary central projection presented to the user unless product requirements specify otherwise.

Do not imply that P50 is guaranteed.

### Testing

Provide deterministic random seeds in tests.

Test statistical properties instead of depending on arbitrary random outputs.

---

# Phase 5 — Portfolio Aggregation

## Goal

Aggregate projections through the investment hierarchy.

Implement a projection aggregator such as:

```text
PortfolioProjectionEngine
```

or equivalent naming consistent with the existing architecture.

Aggregation hierarchy:

```text
Asset
  ↓
Account
  ↓
Institution
  ↓
Portfolio
```

Example:

```text
GBM
├── Trading
│   ├── AAPL
│   ├── VOO
│   └── FUNO
│
└── Smart Cash

Klar
└── SOFIPO

CETES Directo
└── CETES
```

Each asset may use a different strategy.

Example:

```text
AAPL
→ MONTE_CARLO

VOO
→ MONTE_CARLO

FUNO
→ MONTE_CARLO

SOFIPO
→ COMPOUND_INTEREST

CETES
→ FIXED_RATE
```

The aggregator must be able to produce:

```text
AssetProjection
AccountProjection
InstitutionProjection
PortfolioProjection
```

Do not duplicate calculation logic at each level.

---

# Phase 6 — Portfolio-Level Monte Carlo

## Goal

Generate statistically meaningful probability ranges for the complete portfolio.

Do NOT calculate:

```text
Portfolio P10 =
Asset A P10
+ Asset B P10
+ Asset C P10
```

Instead calculate portfolio values per simulation.

Conceptually:

```text
Simulation #1

SOFIPO
+ CETES
+ Stocks
+ ETFs
+ FIBRAs
────────────
Portfolio Total #1


Simulation #2

SOFIPO
+ CETES
+ Stocks
+ ETFs
+ FIBRAs
────────────
Portfolio Total #2


...

Simulation #10,000
```

Then calculate percentiles from the portfolio-total distribution:

```text
P10
P25
P50
P75
P90
```

Deterministic investments should contribute their deterministic projected value to each simulation.

Probabilistic assets should contribute their simulated value.

## Future Compatibility

Design the implementation so correlations between assets can be introduced later.

Do not implement advanced correlation models unless necessary for the current version.

Document the independence assumption used by the initial Monte Carlo implementation.

---

# Phase 7 — Growth Attribution

## Goal

Explain where projected portfolio growth comes from.

Create a model equivalent to:

```text
ProjectedGrowthBreakdown
```

At minimum separate:

```text
Contributions
Investment Returns
Distributions
Withdrawals
Estimated Taxes
```

Support attribution by:

```text
Institution
Account
Asset
Asset type
```

Example:

```text
Projected Growth                 +98,400

Contributions                    +60,000
Investment Returns               +38,400

Returns by institution:

GBM                              +19,300
Klar                             +12,300
CETES Directo                     +6,800
```

The following must never be treated as investment return:

```text
Deposits
Transfers between owned accounts
Principal moved between investments
```

Avoid double-counting internal transfers.

---

# Phase 8 — Inflation and Tax Adjustments

## Goal

Separate nominal projections from estimated real purchasing-power growth.

Implement these adjustments as a layer after the core investment projection.

Architecture:

```text
Portfolio Projection
        ↓
Tax Adjustment
        ↓
Inflation Adjustment
        ↓
Real Projection
```

Do not embed inflation calculations separately into every projection engine.

Output should support:

```text
Nominal projected value
Estimated taxes
After-tax projected value
Inflation impact
Real projected value
```

Example:

```text
Current Portfolio                 $520,000

Nominal Projection                $618,400
Contributions                      +60,000
Investment Returns                 +38,400

Estimated Taxes                     -4,200
Estimated Inflation Impact         -21,300

Estimated Real Value              $592,900
```

Inflation and tax assumptions must be visible and configurable.

Do not present tax calculations as definitive tax advice.

---

# Phase 9 — Projection UI

## Goal

Create a dedicated projection experience consistent with the current PULSO design system.

Primary view:

```text
PROJECTION · 12 MONTHS

              $618,400
                 P50

             +$98,400

P10                             P90
$574k ├─────────●───────────────┤ $672k

3M   6M   [1Y]   3Y   5Y   10Y
```

Below it show:

```text
GROWTH SOURCES

Contributions               +$60,000
Investment returns          +$38,400
```

Then:

```text
BY INSTITUTION

GBM                         +$19,300
Klar                        +$12,300
CETES Directo                +$6,800
```

Allow drill-down:

```text
Portfolio
    ↓
Institution
    ↓
Account
    ↓
Asset
```

Each level should show:

- current value;
- projected value;
- projected gain;
- contributions;
- projection strategy;
- probability range when applicable.

---

# Phase 10 — Projection Strategy Configuration

## Goal

Allow automatic strategy selection while retaining user control.

Provide:

```text
Projection Method

● Automatic
○ Fixed rate
○ Compound interest
○ Historical return
○ Monte Carlo
○ Manual
```

Automatic mode should select a recommended strategy according to investment characteristics.

Initial mapping may follow:

```text
SOFIPO
→ COMPOUND_INTEREST

CETES
→ FIXED_RATE

Fixed-rate instrument
→ FIXED_RATE

Stock
→ MONTE_CARLO

ETF
→ MONTE_CARLO

FIBRA
→ MONTE_CARLO

Custom asset
→ HISTORICAL_RETURN or MANUAL
```

Do not rely only on asset names or institution names.

Use structured asset/instrument types where available.

The selected projection method must always be visible to the user.

---

# Phase 11 — Projection Scenarios

## Goal

Allow users to explore alternative assumptions without modifying real portfolio data.

Create:

```text
ProjectionScenario
```

Initial scenarios:

```text
BASE
PESSIMISTIC
OPTIMISTIC
CUSTOM
```

Scenario parameters may override:

```text
Expected return
Volatility
Inflation
Tax estimate
Monthly contribution
Interest rates
```

Scenario modifications must never mutate:

```text
transactions
historical values
actual balances
investment configuration
```

Scenarios are projection-only data.

---

# Phase 12 — Testing and Validation

## Unit Tests

Cover:

- fixed-rate projection;
- compound interest;
- monthly contributions;
- withdrawals;
- historical annualization;
- historical eligibility;
- Monte Carlo percentiles;
- portfolio aggregation;
- growth attribution;
- inflation adjustment;
- tax adjustment;
- scenario overrides.

## Edge Cases

Test:

```text
Empty portfolio
Single investment
Zero-value investment
Zero interest
Negative returns
Very high volatility
No historical data
Insufficient historical data
Large portfolio
Hundreds of positions
Very long horizon
No contributions
Large periodic contributions
Withdrawals exceeding expected gains
```

## Monte Carlo Tests

Use deterministic seeds.

Tests should validate:

- reproducibility with a fixed seed;
- percentile ordering;
- expected distribution properties;
- deterministic assets remaining deterministic;
- portfolio aggregation correctness.

Always guarantee:

```text
P10 <= P25 <= P50 <= P75 <= P90
```

---

# Recommended Delivery Order

## V1 — Hybrid Projection Foundation

Implement:

```text
Phase 1
Phase 2
Phase 3
Phase 5
```

Deliver:

- common projection architecture;
- fixed-rate projections;
- compound-interest projections;
- historical projections;
- portfolio aggregation.

At this point PULSO should already support hybrid portfolio projections.

---

## V2 — Probabilistic Projection

Implement:

```text
Phase 4
Phase 6
Phase 7
Phase 8
```

Deliver:

- Monte Carlo;
- portfolio probability ranges;
- growth attribution;
- inflation adjustment;
- estimated tax adjustment.

---

## V3 — Product Experience

Implement:

```text
Phase 9
Phase 10
Phase 11
Phase 12
```

Deliver:

- projection UI;
- drill-down;
- strategy selection;
- scenarios;
- complete validation suite.

---

# Architecture Target

Final conceptual architecture:

```text
                    Projection Service
                           │
         ┌─────────────────┼──────────────────┐
         │                 │                  │
         ▼                 ▼                  ▼
    Fixed Rate        Historical         Monte Carlo
         │                 │                  │
         │          Compound Interest         │
         │                 │                  │
         └─────────────────┼──────────────────┘
                           ▼
                    Asset Projection
                           │
                           ▼
                  Portfolio Aggregator
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
      Growth Attribution        Portfolio Distribution
              │                         │
              └────────────┬────────────┘
                           ▼
                   Adjustment Layer
                    Tax / Inflation
                           │
                           ▼
                     Projection UI
```

---

# Implementation Rules

1. Inspect the existing architecture before creating new packages, repositories, entities, or abstractions.

2. Reuse existing money, investment, institution, account, transaction, Room, repository, Flow, and domain models whenever possible.

3. Do not perform financial calculations inside Compose UI components.

4. Keep projection engines independently testable.

5. Do not couple projection algorithms directly to Room entities.

6. Domain models should remain independent from persistence models where the existing architecture supports that separation.

7. Avoid `Double` for stored monetary values.

8. Prevent internal transfers from being counted as investment gains or new contributions.

9. Do not silently fabricate missing historical data.

10. Every projected value must retain information about the strategy and assumptions used to produce it.

11. Deterministic and probabilistic projections must coexist in the same portfolio.

12. Do not present Monte Carlo outputs as guarantees.

13. Preserve backward compatibility with existing portfolio calculations.

14. Prefer incremental migrations over destructive database migrations.

15. Add tests before replacing any existing projection behavior.

---

# Agent Execution Instructions

Before implementation:

1. Inspect the current project structure.
2. Locate existing investment/domain models.
3. Locate portfolio and performance calculations.
4. Locate Room entities and migrations.
5. Locate current projection/scenario implementation.
6. Locate existing tests.
7. Produce a short impact analysis.

Then implement phases sequentially.

For each phase:

```text
1. Inspect existing implementation.
2. Identify reusable components.
3. Implement the smallest required domain changes.
4. Add/update tests.
5. Run relevant unit tests.
6. Run build/compile validation.
7. Fix regressions before continuing.
8. Report completed changes.
```

Do not proceed to a later phase while the project is failing compilation or relevant tests.

If an architectural conflict is discovered, prefer adapting this specification to the existing clean architecture rather than duplicating infrastructure.

---

# Definition of Done

The feature is complete when PULSO can take a portfolio containing, for example:

```text
Klar
└── SOFIPO

CETES Directo
└── CETES

GBM
├── Stock
├── ETF
└── FIBRA
```

and independently project:

```text
SOFIPO
→ compound interest

CETES
→ fixed-rate model

Stock
→ Monte Carlo / historical

ETF
→ Monte Carlo / historical

FIBRA
→ Monte Carlo / historical
```

then combine them into one portfolio projection showing:

```text
Current portfolio value

Projected portfolio value

P10
P25
P50
P75
P90

Expected investment return

Expected contributions

Expected withdrawals

Growth contribution by institution

Growth contribution by account

Growth contribution by asset

Nominal projection

After-tax estimate

Inflation-adjusted real projection
```

The user must be able to understand both:

> **How much could my total portfolio be worth?**

and:

> **Which investments are expected to contribute to that growth?**

without confusing new contributions with actual investment performance.