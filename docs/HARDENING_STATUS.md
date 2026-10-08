# HARDENING_STATUS — PULSO

Estado de cumplimiento del **Hardening Fases 1–5** (`HARDENING_PLAN.md`) y de la
integración del roadmap (`ROADMAP_UI_INTEGRATION.md`), verificado con código y tests.

## Resumen

| Métrica | Valor |
|---|---|
| Tests unitarios (JVM) | 263 / 263 |
| Tests instrumentados (Android) | 43 / 43 |
| Flujos E2E de aplicación | 11 en `AppE2EFlowsTest`: crear/cancelar inversión, compra, editar/eliminar movimiento, eliminar inversión, recreación de Activity (dashboard y formularios), ciclo de vida completo, histórico fuera de orden y reactividad de proyección |
| Migración Room 1 → 2 | Validada |
| `assembleDebug` | OK |
| `lintDebug` | OK |
| Smoke físico (API 36, Android 16) | PASS — flujo E2E completo + terminación/reapertura real de la app con datos persistidos (2026-10-07) |

Última verificación: `motorola edge 60 fusion` (API 36, Android 16) + JVM local,
2026-10-07 — `scripts/device_smoke_test.sh` PASS: 263/263 unitarios, 43/43
instrumentados, instalación limpia, flujo E2E completo de UI, revisión de
crashes/ANR y terminación/reapertura real de la app con datos persistidos.

---

## Definition of Done — Hardening

> Nota: "P0" aquí significa que el flujo existe y funciona en el camino feliz. La
> persistencia tras reinicio real de la app sigue pendiente; la cobertura de casos
> límite se detalla en «Pendientes y huecos de cobertura».

- [x] **Flujos P0 implementados (camino feliz).** Ledger, venta, retiro,
      editar/eliminar inversión y movimientos, atomicidad y validaciones.
      Evidencia: `AddTransactionUseCaseTest`, `UpdateTransactionUseCaseTest`,
      `DeleteTransactionUseCaseTest`, `UpdateInvestmentUseCaseTest`,
      `DeleteInvestmentUseCaseTest`, `LedgerCalculatorTest`, `RoomInvestmentRepositoryTest`,
      `InvestmentFlowIntegrationTest`, `AppE2EFlowsTest`.
- [x] **Flujos P0 con cobertura completa.** Precisión de centavos y acumulación
      (`LedgerCalculatorTest`, `AddTransactionUseCaseTest`), retiros total/cero/negativo,
      ventas parcial/total/inválida en instrumentado (`InvestmentFlowIntegrationTest`) y
      edición/borrado de movimientos desde UI (`AddTransactionViewModelTest`,
      `AppE2EFlowsTest`).
- [x] **Flujos existentes sin regresiones.** 167 unit + 33 instrumentados en verde.
- [x] **Operaciones inválidas rechazadas antes de persistir.**
      `LedgerCalculator.validate`, `TransactionFormValidator`, `InvestmentFormValidator`.
- [x] **Sin posiciones negativas.** Invariantes de `LedgerCalculator` + `LedgerCalculatorTest`.
- [x] **Sin registros huérfanos.** Cascade verificado en
      `RoomInvestmentRepositoryTest.deletesInvestmentAndCascadesTransactions`.
- [x] **Dashboard y detalle con métricas coincidentes.**
      `InvestmentFlowIntegrationTest.dashboardAndDetailMetricsMatch`.
- [x] **Cambios Room reflejados por Flow.**
      `InvestmentFlowIntegrationTest.roomChangesPropagateThroughFlows`.
- [x] **Datos persistentes tras cerrar/reabrir la app.**
      `PersistenceFlowTest.dataSurvivesDatabaseReopen` cierra y reabre la BD, y
      el smoke físico termina la app real (`am force-stop`) y verifica tras
      reabrirla el total persistido (`$9,999.00 MXN`) en el dashboard.
- [x] **Fórmulas financieras probadas.** `LedgerCalculatorTest`, `ProjectionCalculatorTest`.
- [x] **Tests existentes y nuevos en verde.**
- [x] **`test`, build y lint ejecutados.**

### Migraciones

- [x] `MIGRATION_1_2` explícita, sin `fallbackToDestructiveMigration`.
- [x] Esquemas exportados (`app/schemas/…/1.json`, `2.json`).
- [x] `MigrationTest.migrate1To2_preservesExistingData` valida estructura y datos.

---

## Definition of Done — Roadmap de integración (Fases 1–5)

- [x] El proyecto compila.
- [x] Los tests existentes pasan.
- [x] La nueva UI está integrada (design system, navegación, componentes).
- [x] Dashboard / Inversiones / Detalle usan datos reales del dominio.
- [x] Se pueden crear inversiones (incluye E2E de UI).
- [x] Se pueden registrar movimientos.
- [x] Los cambios actualizan patrimonio/métricas por Flow.
- [x] Patrimonio muestra agregaciones reales.
- [x] Proyección usa el motor financiero.
- [x] Inflación e ISR separados del rendimiento nominal.
- [x] Instituciones configurables.
- [x] Sin cifras financieras hardcodeadas en producción (solo previews/tests).
- [x] Los Composables no contienen lógica financiera.
- [x] Room no es accedido directamente desde UI.
- [x] Loading, Empty y Error implementados.
- [x] Formato monetario y porcentual centralizados.
- [x] Navegación utiliza IDs.
- [x] Sistema visual consistente.
- [x] Base preparada para crecer más allá del uso personal.

---

## Cobertura de tests

### Unitarios (JVM)
- Ledger: `LedgerCalculatorTest`.
- Casos de uso: `AddInvestmentUseCaseTest`, `AddTransactionUseCaseTest`,
  `UpdateTransactionUseCaseTest`, `DeleteTransactionUseCaseTest`,
  `UpdateInvestmentUseCaseTest`, `DeleteInvestmentUseCaseTest`.
- Validaciones: `InvestmentFormValidatorTest`, `TransactionFormValidatorTest`.
- Agregaciones: `GetDashboardSummaryTest`, `GetPortfolioSummaryTest`,
  `GetInvestmentDetailTest`, `GetInvestmentsTest`, `ProjectionCalculatorTest`.
- ViewModels de todas las pantallas.
- Formatters, mappers, rutas.

### Instrumentados (Android)
- `RoomInvestmentRepositoryTest`: atomicidad, cascade, update/delete.
- `MigrationTest`: migración 1 → 2.
- `InvestmentFlowIntegrationTest`: creación, compra/venta/retiro, venta
  parcial/total/inválida, borrado con cascade, reactividad y consistencia
  dashboard/detalle.
- `PersistenceFlowTest`: persistencia tras reapertura.
- `AppE2EFlowsTest`: flujos E2E de UI vía `MainActivity` (crear inversión, registrar
  compra, editar movimiento, eliminar movimiento, eliminar inversión) con
  `AppContainer` inyectable.

### Seam de test
`AppContainer.setRepositoryForTest` / `resetForTest` permiten montar la app real
sobre una base Room en memoria durante los tests instrumentados.
Requiere Espresso 3.7.0 (corrige `InputManager.getInstance` eliminado en API 34+).

---

## Bug detectado y corregido durante este hardening

- **Orden de transacciones del mismo día.** Al validar/recalcular, una transacción
  nueva tenía `id = 0`, por lo que se ordenaba antes que movimientos existentes de
  la misma fecha y podía rechazar operaciones válidas (p. ej. una venta posterior a
  una compra del mismo día). Corregido en `AddTransactionUseCase` ordenando la
  transacción entrante como la más reciente (`id = Long.MAX_VALUE`).
  Reproducido por `InvestmentFlowIntegrationTest` y cubierto por tests.

---

## Pendientes y huecos de cobertura

Cobertura revisada por inspección estática de `app/src/test` y `app/src/androidTest`
durante la validación QA de flujos. Los huecos que tocan integridad financiera se elevan a P0.

### P0 — integridad financiera (cerrado)

- [x] **Precisión monetaria.** `LedgerCalculatorTest` cubre `$0.01`, 1 000 depósitos
      de un centavo sin error acumulado, montos grandes, compra fraccionaria y
      rendimiento negativo. (Se mantiene `Double` con redondeo a 2 decimales.)
- [x] **Retiros.** Total, cero y negativo cubiertos en `LedgerCalculatorTest`,
      `TransactionFormValidatorTest` y `AddTransactionUseCaseTest`; el retiro superior
      al saldo se rechaza antes de persistir.
- [x] **Ventas en instrumentado.** `InvestmentFlowIntegrationTest` cubre venta parcial
      100→60, venta total 100→0 y venta inválida (150 > 100) rechazada sin tocar Room.
- [x] **Editar/eliminar por tipo.** `AddTransactionViewModelTest` y `AppE2EFlowsTest`
      cubren edición y borrado de movimientos (incluido el rechazo al borrar el
      depósito de fondeo) desde ViewModel y UI.

### P1 — flujos y estados

- [x] Cancelar edición de inversión y cancelar el diálogo de borrado. Los formularios
      exponen `Cancelar` y la navegación vuelve mediante `popBackStack`.
- [x] Eliminar una inversión sin movimientos. Cubierto en
      `DeleteInvestmentUseCaseTest`, `RoomInvestmentRepositoryTest` y
      `InvestmentFlowIntegrationTest`.
- [x] Back/cancelación en formularios desde UI instrumentada. `AppE2EFlowsTest` cubre
      cancelar nueva inversión, nuevo movimiento y diálogo de borrado.
- [x] Recreación de Activity en dashboard: `recreatingActivity_preservesDashboardData`.
- [x] Doble-Save y reentrada: `AddInvestmentViewModelTest` y `AddTransactionViewModelTest`
      verifican envíos consecutivos; `AppE2EFlowsTest` verifica que los formularios de
      inversión y movimiento conservan sus campos tras recrear la Activity.
- [x] IDs inexistentes en edición: inversión y movimiento muestran un error en lugar de
      quedar esperando indefinidamente.
- [x] Propagación reactiva `DB → Flow → Repository`: `roomChangesPropagateThroughFlows`,
      `editingTransaction_emitsUpdatedInvestmentThroughFlow` y
      `deletingTransaction_emitsRecomputedInvestmentThroughFlow` verifican las emisiones
      inicial y posterior para compra, edición y borrado. La propagación hasta UiState
      queda cubierta por los E2E de UI correspondientes.
- [x] E2E único de ciclo de vida completo en `AppE2EFlowsTest`:
      institución→inversión→depósito→compra→venta→retiro→eliminar.
- [x] Invariante de distribución: tests verifican que las distribuciones por categoría,
      institución y moneda reconcilian con el patrimonio total.
- [x] Fechas: `LedgerCalculatorTest` verifica movimientos de varias fechas insertados
      fuera de orden.
- [x] Estados `NaN`/`Infinity`: proyecciones y series mensuales inválidas se normalizan
      y tienen cobertura de tests.
- [x] Proyección: cobertura de rendimiento negativo, inflación negativa y portafolio
      positivo.
- [x] Múltiples monedas en un mismo portafolio. `CurrencyConverter` convierte MXN/USD
      usando el tipo USD/MXN persistido en configuración; agregados y proyección usan
      la moneda base. Cubierto por `CurrencyConverterTest` y
      `GetPortfolioSummaryTest`.
- [x] Fechas históricas / movimientos fuera de orden en UI. `AppE2EFlowsTest`
      inserta un depósito con fecha anterior después de una compra y verifica el
      orden cronológico y el ledger recalculado.

### P2 — robustez

- [x] Volumen (20 instituciones / 100 inversiones / 1 000 movimientos) y rendimiento.
      `LedgerVolumeTest` pasó en JVM. `RoomVolumeTest` pasó en dispositivo físico
      `moto g24 - 14`: seed 4.599 s, consulta de inversiones 16.9 ms, consulta
      promedio por inversión 3.1 ms, primera emisión del Flow 66.0 ms y
      recomputación de ledgers 269.3 ms.
- [x] Optimización basada en mediciones. Los tiempos medidos están por debajo de los
      umbrales definidos y no justifican cambios especulativos en Room, Flow o el
      cálculo del ledger. Se conserva la implementación actual como baseline.
      `assembleRelease` y `bundleRelease` también pasan después de la integración FX.

> Nota: P0 cerrado y verificado ejecutando `:app:testDebugUnitTest` (175/175),
> `:app:connectedDebugAndroidTest` (41/41) y `:app:lintDebug` sobre `moto g24 - 14`
> (API 37). La conversión multi-moneda está implementada y la suite posterior a FX
> pasó `41/41` en AVD `test` (API 35). P2 de volumen y rendimiento está cerrado
> según las mediciones registradas.

---

## Extensiones posteriores al hardening

- **Multi-moneda.** `CurrencyConverter` convierte MXN/USD con el tipo
      persistido en configuración; agregados y proyección usan la moneda base
      (`CurrencyConverterTest`, `GetPortfolioSummaryTest`).
- **Motor de proyección híbrido.** Plan completo implementado (V1–V3) — ver
      `PROJECTION_ENGINE_STATUS.md`.
- **App Lock + insets.** PIN de 4 dígitos (PBKDF2), biometría, cooldown y
      auto-lock; corrección global de window insets — ver
      `SECURITY_AND_INSETS_IMPLEMENTATION_REPORT.md`.
- **Smoke físico.** `scripts/device_smoke_test.sh` ejecuta en un dispositivo
      físico: build, tests, instalación limpia (`pm clear`), flujo E2E completo
      de UI, revisión de crashes/ANR y terminación/reapertura real de la app
      (`am force-stop` + relanzamiento con el total persistido verificado).
      PASS en `motorola edge 60 fusion` (API 36, Android 16), 2026-10-07.
      El script mantiene la pantalla activa (`svc power stayon usb`) y falla
      rápido si el keyguard sigue activo.
- **Flake corregido.** `editingTransaction_emitsUpdatedInvestmentThroughFlow` y
      `deletingTransaction_emitsRecomputedInvestmentThroughFlow` racéaban con el
      executor real de Room (el `take(2)` podía perder la emisión pre-operación);
      reescritos con `Channel` + `receive()` secuencial, determinista.

---

## Comandos de verificación

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```
