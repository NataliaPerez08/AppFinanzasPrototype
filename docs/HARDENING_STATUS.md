# HARDENING_STATUS — PULSO

Estado de cumplimiento del **Hardening Fases 1–5** (`HARDENING_PLAN.md`) y de la
integración del roadmap (`ROADMAP_UI_INTEGRATION.md`), verificado con código y tests.

## Resumen

| Métrica | Valor |
|---|---|
| Tests unitarios (JVM) | 167 / 167 |
| Tests instrumentados (Android) | 33 / 33 |
| Flujos E2E de aplicación | 5 (crear inversión, registrar compra, eliminar inversión, editar movimiento, eliminar movimiento) |
| Migración Room 1 → 2 | Validada |
| `assembleDebug` | OK |
| `lintDebug` | OK |

Última verificación: emulador `Medium_Phone` (API 37, x86_64) + JVM local.

---

## Definition of Done — Hardening

> Nota: "P0" aquí significa que el flujo existe y funciona en el camino feliz; la
> cobertura de casos límite se detalla en «Pendientes y huecos de cobertura».

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
- [x] **Flujos existentes sin regresiones.** 159 unit + 24 instrumentados en verde.
- [x] **Operaciones inválidas rechazadas antes de persistir.**
      `LedgerCalculator.validate`, `TransactionFormValidator`, `InvestmentFormValidator`.
- [x] **Sin posiciones negativas.** Invariantes de `LedgerCalculator` + `LedgerCalculatorTest`.
- [x] **Sin registros huérfanos.** Cascade verificado en
      `RoomInvestmentRepositoryTest.deletesInvestmentAndCascadesTransactions`.
- [x] **Dashboard y detalle con métricas coincidentes.**
      `InvestmentFlowIntegrationTest.dashboardAndDetailMetricsMatch`.
- [x] **Cambios Room reflejados por Flow.**
      `InvestmentFlowIntegrationTest.roomChangesPropagateThroughFlows`.
- [ ] **Datos persistentes tras cerrar/reabrir la app (parcial).**
      `PersistenceFlowTest.dataSurvivesDatabaseReopen` cierra y reabre la BD;
      falta probar terminación y apertura real de la app.
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
- [ ] Eliminar una inversión sin movimientos (los seeds siempre crean depósito inicial).
- [x] Back/cancelación en formularios desde UI instrumentada. `AppE2EFlowsTest` cubre
      cancelar nueva inversión, nuevo movimiento y diálogo de borrado.
- [x] Recreación de Activity en dashboard: `recreatingActivity_preservesDashboardData`.
- [x] Doble-Save: `AddInvestmentViewModelTest` y `AddTransactionViewModelTest` verifican
      que dos envíos consecutivos solo persistan un registro. La recreación y re-entrada
      de Activity siguen pendientes.
- [x] IDs inexistentes en edición: inversión y movimiento muestran un error en lugar de
      quedar esperando indefinidamente.
- [ ] Propagación reactiva completa `DB → Flow → ViewModel → UI`: la emisión reactiva
      intermedia solo se asserta para una compra (`roomChangesPropagateThroughFlows`);
      edición y borrado ya tienen E2E de UI (`editTransaction_throughUi_updatesQuantity`,
      `deleteTransaction_throughUi_removesMovement`) pero sin aserción de la emisión
      intermedia del Flow.
- [ ] E2E único de ciclo de vida completo
      (institución→inversión→depósito→compra→venta→retiro→eliminar).
- [x] Invariante de distribución: tests verifican que las distribuciones por categoría,
      institución y moneda reconcilian con el patrimonio total.
- [x] Fechas: `LedgerCalculatorTest` verifica movimientos de varias fechas insertados
      fuera de orden.
- [x] Estados `NaN`/`Infinity`: proyecciones y series mensuales inválidas se normalizan
      y tienen cobertura de tests.
- [x] Proyección: cobertura de rendimiento negativo, inflación negativa y portafolio
      positivo.
- [ ] Múltiples monedas en un mismo portafolio (conversión).
- [ ] Fechas históricas / movimientos fuera de orden en pruebas de UI.

### P2 — robustez

- [x] Volumen (20 instituciones / 100 inversiones / 1 000 movimientos) y rendimiento.
      `LedgerVolumeTest` pasó en JVM. `RoomVolumeTest` pasó en dispositivo físico
      `moto g24 - 14`: seed 4.599 s, consulta de inversiones 16.9 ms, consulta
      promedio por inversión 3.1 ms, primera emisión del Flow 66.0 ms y
      recomputación de ledgers 269.3 ms.
- [ ] Optimización basada en mediciones.

> Nota: P0 cerrado y verificado ejecutando `:app:testDebugUnitTest` (167/167),
> `:app:connectedDebugAndroidTest` (33/33) y `:app:lintDebug` sobre `moto g24 - 14`
> (API 37). P1/P2 siguen pendientes.

---

## Comandos de verificación

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:lintDebug
```
