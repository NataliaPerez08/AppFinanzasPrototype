# PULSO

**Patrimonio e inversiones.** Aplicación Android de finanzas personales (Kotlin + Jetpack Compose) con
persistencia local en Room, arquitectura por capas y sistema visual
neo-brutalista.

## Estado

Fases 1–5 del roadmap de integración UI completadas sobre la arquitectura
existente. Hardening P0 aplicado: ledger consistente, operaciones atómicas en
Room, editar/eliminar inversiones y movimientos, validaciones y precisión
monetaria. Extensiones posteriores: conversión multi-moneda (MXN/USD), motor de
proyección híbrido (tasa fija, interés compuesto, histórico, Monte Carlo,
escenarios) y App Lock con PIN + biometría. Ver `HARDENING_STATUS.md`,
`PROJECTION_ENGINE_STATUS.md` y `SECURITY_AND_INSETS_IMPLEMENTATION_REPORT.md`
para el detalle verificado.

## Stack

- Kotlin, Jetpack Compose (Material 3 como infraestructura)
- Navigation Compose, ViewModel, StateFlow, Coroutines
- Room, Repository Pattern, Use Cases (capa de dominio)

## Arquitectura

```text
UI (Compose) → ViewModel/UiState → Use Cases → Repository → Room
```

La UI no contiene lógica financiera ni accede a Room. Los valores demo solo se
usan en previews y tests.

## Pantallas

Dashboard, Inversiones, Detalle de inversión, Nueva/Editar inversión,
Registrar/Editar movimiento, Patrimonio, Proyección y Más (Instituciones,
Configuración).

## Formato

Formato monetario, porcentual y de fecha centralizado (`MoneyFormatter`,
`PercentageFormatter`, `DateFormatter`). Soporte inicial para `MXN` y `USD`.

## Ejecutar

1. Abre la carpeta del proyecto en Android Studio.
2. Espera la sincronización de Gradle.
3. Ejecuta `app` en un emulador/dispositivo Android 8+ (API 26+).

## Tests

```bash
./gradlew :app:testDebugUnitTest           # unitarios (JVM)
./gradlew :app:connectedDebugAndroidTest   # instrumentados (requiere emulador/dispositivo)
./gradlew :app:lintDebug
```

Cobertura: ledger, casos de uso, repositorio Room, migraciones, flujos de
integración, E2E de UI, motor de proyección híbrido y App Lock. Detalle en
`HARDENING_STATUS.md` y `PROJECTION_ENGINE_STATUS.md`.

## Documentación

Todos los documentos viven en `docs/`.

- `AGENTS.md` — arquitectura, sistema visual y convenciones del proyecto.
- `ROADMAP_UI_INTEGRATION.md` — fases 1–5 de integración UI.
- `HARDENING_PLAN.md` — plan de hardening.
- `HARDENING_STATUS.md` — estado de cumplimiento, pendientes y huecos de cobertura.
- `PULSO — Hybrid Portfolio Projection Engine Implementation Plan.md` — plan del motor de proyección híbrido.
- `PROJECTION_ENGINE_STATUS.md` — estado de implementación del motor de proyección.
- `PULSO_SECURITY_AND_INSETS_IMPLEMENTATION.md` — especificación de App Lock e insets.
- `SECURITY_AND_INSETS_IMPLEMENTATION_REPORT.md` — reporte de implementación de App Lock e insets.
- `PULSO_APP_NAMING_UPDATE.md` — guía de naming y branding PULSO.
- `FASE_6_PRODUCTION_RELEASE_GOOGLE_PLAY.md` — plan de release 1.0 y Google Play.
- `RELEASE_SIGNING.md` — configuración segura de firma de producción.
