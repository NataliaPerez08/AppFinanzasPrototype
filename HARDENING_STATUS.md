# HARDENING_STATUS — App Finanzas

Estado de cumplimiento del **Hardening Fases 1–5** (`HARDENING_PLAN.md`) y de la
integración del roadmap (`ROADMAP_UI_INTEGRATION.md`), verificado con código y tests.

## Resumen

| Métrica | Valor |
|---|---|
| Tests unitarios (JVM) | 144 / 144 |
| Tests instrumentados (Android) | 18 / 18 |
| Flujos E2E de aplicación | 3 (crear inversión, registrar compra, eliminar inversión) |
| Migración Room 1 → 2 | Validada |
| `assembleDebug` | OK |
| `lintDebug` | OK |

Última verificación: emulador `Medium_Phone` (API 37, x86_64) + JVM local.

---

## Definition of Done — Hardening

- [x] **Flujos P0 funcionales.** Ledger consistente, venta, retiro, editar/eliminar
      inversión y movimientos, atomicidad, precisión y validaciones.
      Evidencia: `AddTransactionUseCaseTest`, `UpdateTransactionUseCaseTest`,
      `DeleteTransactionUseCaseTest`, `UpdateInvestmentUseCaseTest`,
      `DeleteInvestmentUseCaseTest`, `LedgerCalculatorTest`, `RoomInvestmentRepositoryTest`,
      `InvestmentFlowIntegrationTest`, `AppE2EFlowsTest`.
- [x] **Flujos existentes sin regresiones.** 144 unit + 18 instrumentados en verde.
- [x] **Operaciones inválidas rechazadas antes de persistir.**
      `LedgerCalculator.validate`, `TransactionFormValidator`, `InvestmentFormValidator`.
- [x] **Sin posiciones negativas.** Invariantes de `LedgerCalculator` + `LedgerCalculatorTest`.
- [x] **Sin registros huérfanos.** Cascade verificado en
      `RoomInvestmentRepositoryTest.deletesInvestmentAndCascadesTransactions`.
- [x] **Dashboard y detalle con métricas coincidentes.**
      `InvestmentFlowIntegrationTest.dashboardAndDetailMetricsMatch`.
- [x] **Cambios Room reflejados por Flow.**
      `InvestmentFlowIntegrationTest.roomChangesPropagateThroughFlows`.
- [x] **Datos persistentes después de cerrar la app.**
      `PersistenceFlowTest.dataSurvivesDatabaseReopen` (cierra y reabre la BD).
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
- `InvestmentFlowIntegrationTest`: creación, compra/venta/retiro, borrado con
  cascade, reactividad y consistencia dashboard/detalle.
- `PersistenceFlowTest`: persistencia tras reapertura.
- `AppE2EFlowsTest`: flujos E2E de UI vía `MainActivity` (crear, registrar compra,
  eliminar inversión) con `AppContainer` inyectable.

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

## Pendientes (del roadmap, no bloqueantes)

### P1
- [ ] Editar/eliminar movimientos e inversiones: falta prueba de recreación de Activity.
- [ ] Back/cancelación en formularios desde UI instrumentada.
- [ ] Múltiples monedas en un mismo portafolio (conversión).
- [ ] Fechas históricas / movimientos fuera de orden en pruebas de UI.

### P2
- [ ] Volumen (20 instituciones / 100 inversiones / 1 000 movimientos) y rendimiento.
- [ ] Optimización basada en mediciones.

---

## Comandos de verificación

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:lintDebug
```
