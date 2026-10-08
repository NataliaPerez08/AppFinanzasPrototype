# PROJECTION_ENGINE_STATUS — PULSO

Estado de implementación del plan
`PULSO — Hybrid Portfolio Projection Engine Implementation Plan.md`,
verificado con código y tests (JVM 263/263, 2026-10-07).

## Entregado

| Versión | Fases del plan | Contenido | Tests |
|---|---|---|---|
| V1 | 1, 2, 3, 5 | Contrato común (`ProjectionContract`, `ProjectionInput`/`ProjectionResult`), `FixedRateProjectionEngine`, `CompoundInterestProjectionEngine`, `HistoricalReturnProjectionEngine` + `HistoricalReturnAnalyzer` (elegibilidad configurable), `ManualProjectionEngine`, agregación en `PortfolioProjectionEngine` | Una suite por motor en `domain/projection` |
| V2 | 4, 6, 7, 8 | `MonteCarloProjectionEngine` (semilla y simulaciones configurables, percentiles), distribución de portafolio por simulación (no suma de percentiles por activo), `ProjectedGrowthBreakdown` (contribuciones ≠ rendimientos), `ProjectionAdjustments` (ISR e inflación: nominal ≠ después de impuestos ≠ real) | `MonteCarloProjectionEngineTest`, `PortfolioProjectionEngineTest`, `ProjectedGrowthBreakdownTest`, `ProjectionAdjustmentsTest` |
| V3 | 9, 10, 11, 12 | UI de proyección con P50, rango P10–P90, fuentes de crecimiento, desglose por institución expandible con activos y estrategia visible; selección de estrategia por inversión (`investment.projectionStrategy`) con recomendación automática (`ProjectionStrategyResolver`, visible en alta y detalle de inversión); escenarios preset Base/Pesimista/Optimista/Personalizado (`ProjectionScenario.presets`); edge cases y volumen (300 posiciones) | `ProjectionScenarioTest`, `ProjectionEdgeCasesTest`, `PortfolioProjectionVolumeTest`, smoke físico (`scripts/device_smoke_test.sh`) |

La invariante `P10 ≤ P25 ≤ P50 ≤ P75 ≤ P90` está garantizada por tests en
motor y agregación.

## Desviaciones del plan (registradas)

- **Moneda.** El plan sugería evitar `Double`; el proyecto mantiene `Double`
  con redondeo a 2 decimales (decisión global registrada en
  `HARDENING_STATUS.md`).
- **Jerarquía.** El plan contemplaba Asset → Account → Institution →
  Portfolio; el dominio no tiene entidad de cuenta, la agregación es
  Institution → Asset. Agregar el nivel cuenta requiere modelar cuentas
  primero (fuera de alcance actual).
- **Monte Carlo.** Suposición de independencia entre activos (el plan pedía
  documentarla); correlaciones quedan para una fase posterior.

## Pendientes / deuda

- Rangos probabilísticos por activo: solo existen a nivel portafolio
  (ver `SECURITY_AND_INSETS_IMPLEMENTATION_REPORT.md`).
