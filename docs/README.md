# App Finanzas

Aplicación Android de finanzas personales (Kotlin + Jetpack Compose) con
persistencia local en Room, arquitectura por capas y sistema visual
neo-brutalista.

## Estado

Fases 1–5 del roadmap de integración UI completadas sobre la arquitectura
existente. Hardening P0 aplicado: ledger consistente, operaciones atómicas en
Room, editar/eliminar inversiones y movimientos, validaciones y precisión
monetaria. Ver `HARDENING_STATUS.md` para el detalle verificado.

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

1. Abre la carpeta `AppFinanzasPrototype` en Android Studio.
2. Espera la sincronización de Gradle.
3. Ejecuta `app` en un emulador/dispositivo Android 8+ (API 26+).

## Tests

```powershell
.\gradlew.bat :app:testDebugUnitTest           # unitarios (JVM)
.\gradlew.bat :app:connectedDebugAndroidTest   # instrumentados (requiere emulador/dispositivo)
.\gradlew.bat :app:lintDebug
```

Cobertura: ledger, casos de uso, repositorio Room, migraciones, flujos de
integración y E2E de UI. Detalle en `HARDENING_STATUS.md`.

## Documentación

Todos los documentos viven en `docs/`.

- `AGENTS.md` — arquitectura, sistema visual y convenciones del proyecto.
- `ROADMAP_UI_INTEGRATION.md` — fases 1–5 de integración UI.
- `HARDENING_PLAN.md` — plan de hardening.
- `HARDENING_STATUS.md` — estado de cumplimiento, pendientes y huecos de cobertura.
- `FASE_6_PRODUCTION_RELEASE_GOOGLE_PLAY.md` — plan de release 1.0 y Google Play.
