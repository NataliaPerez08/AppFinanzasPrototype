# Fase 6 — Production Release & Google Play

## Objetivo

Convertir **PULSO** desde su estado actual de prototipo funcional y técnicamente estable a una **versión 1.0 lista para distribución en Google Play**.

Esta fase no debe introducir nuevas funcionalidades de producto salvo que sean necesarias para publicación, estabilidad, seguridad, compatibilidad o cumplimiento.

El objetivo final es producir un **Android App Bundle (`.aab`) firmado, probado y conforme a los requisitos vigentes de Google Play**, listo para Internal Testing, Closed Testing si aplica y posterior publicación en Production.

---

# Contexto actual

La aplicación ya cuenta con una base funcional considerable:

- Jetpack Compose.
- Arquitectura basada en ViewModel / Use Case / Repository.
- Persistencia con Room.
- Migraciones de base de datos.
- Flujos reactivos con Flow.
- Dashboard con datos persistidos.
- Gestión de inversiones.
- Gestión de instituciones financieras.
- Registro de compras, ventas, depósitos y retiros.
- Cálculo de patrimonio.
- Distribuciones.
- Proyecciones.
- Configuración persistente.
- Estimaciones de inflación e ISR.
- Suite de pruebas unitarias.
- Suite de pruebas instrumentadas.
- Flujos E2E principales.

El proyecto todavía conserva algunos elementos de prototipo y requiere hardening de release antes de publicar.

---

# Estado de referencia

Configuración detectada actualmente:

```kotlin
compileSdk = 36
targetSdk = 36

versionCode = 1
versionName = "1.0.0"
```

Application ID anterior:

```text
com.appfinanzas.prototype
```

Application ID de producción definido:

```text
com.pulsofinanzas.app
```

Los paquetes Kotlin internos `com.appfinanzas.prototype` se conservan para evitar
un refactor de código sin impacto en el identificador Android. El `namespace`, el
`applicationId`, el Manifest, el smoke test y las referencias a `R` usan ya el ID
de producción.

---

# Prioridades

## P0 — Bloqueantes para publicación

Los siguientes puntos deben completarse antes de considerar una build candidata a producción.

### P0.1 — Migrar a Android API 36

Actualizar el proyecto para utilizar:

```kotlin
compileSdk = 36
targetSdk = 36
```

Revisar compatibilidad de:

- Android Gradle Plugin.
- Gradle.
- Kotlin.
- Compose.
- Room.
- Navigation.
- Lifecycle.
- Testing libraries.

No actualizar dependencias innecesariamente si la versión actual ya es compatible.

### Criterios de aceptación

- [x] `compileSdk = 36`.
- [x] `targetSdk = 36`.
- [x] El proyecto compila correctamente.
- [x] Todos los tests unitarios pasan.
- [x] Todos los tests instrumentados pasan en AVD API 35 y en dispositivo físico API 36 (`motorola edge 60 fusion`, Android 16, 43/43).
- [x] La aplicación inicia correctamente en Android 16 (smoke físico completo).
- [x] No existen crashes relacionados con cambios de comportamiento de API 36 (revisión de logcat acotada al paquete durante el smoke físico).
- [x] Lint no contiene errores bloqueantes.

### Verificación

```bash
./gradlew clean
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

---

## P0.2 — Definir Application ID definitivo

El Application ID anterior contenía:

```text
com.appfinanzas.prototype
```

El Application ID definitivo es `com.pulsofinanzas.app`.

No publicar la aplicación con un package que incluya `prototype`.

Definir un identificador definitivo antes de crear la publicación estable.

Ejemplo:

```text
com.<developer-or-company>.appfinanzas
```

El valor final debe ser aprobado antes de subir el primer release de producción.

Actualizar:

- `applicationId`.
- `namespace` si se decide alinearlo.
- paquetes Kotlin si corresponde.
- tests.
- imports.
- AndroidManifest.
- deep links si existen.
- FileProvider authorities si existen.
- cualquier referencia hardcoded al package.

### Criterios de aceptación

- [x] No aparece `prototype` en el Application ID final.
- [x] La aplicación compila después del cambio.
- [x] Tests pasan después del cambio.
- [x] No quedan referencias inválidas al package antiguo.
- [ ] Se puede instalar la aplicación en un dispositivo limpio.

---

## P0.3 — Preparar versión 1.0

Reemplazar la versión de prototipo.

Configuración inicial sugerida:

```kotlin
versionCode = 1
versionName = "1.0.0"
```

En caso de haber subido previamente builds a Play Console, usar un `versionCode` superior al último utilizado.

### Criterios de aceptación

- [x] `versionName` no contiene `prototype`.
- [x] `versionCode` es válido y único, pendiente de confirmar contra Play Console.
- [x] La versión mostrada en la aplicación, si existe, coincide con la build.

---

## P0.4 — Configurar build de Release

Crear una configuración de producción explícita.

Ejemplo base:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true

        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

No asumir que las reglas actuales de R8/ProGuard son correctas.

Verificar especialmente:

- Room.
- Serialization.
- Reflection.
- Navigation.
- Compose.
- cualquier librería que requiera keep rules.

### Criterios de aceptación

- [x] `assembleRelease` funciona con firma externa de prueba.
- [x] `bundleRelease` funciona con firma externa de prueba.
- [ ] No existen crashes sólo presentes en release.
- [x] R8 no elimina clases necesarias en la build validada.
- [x] Los recursos no utilizados pueden reducirse de forma segura en la build validada.
- [x] El APK release firmado se instaló y abrió correctamente con
      `com.pulsofinanzas.app/com.appfinanzas.prototype.MainActivity`.

### Verificación

```bash
./gradlew assembleRelease
./gradlew bundleRelease
```

Artefacto esperado:

```text
app/build/outputs/bundle/release/app-release.aab
```

---

## P0.5 — Firma de producción

Configurar firma para Release.

No guardar:

- keystore.
- passwords.
- alias secrets.
- credenciales.

dentro del repositorio.

Utilizar uno de los siguientes mecanismos:

- `keystore.properties` excluido por `.gitignore`.
- variables de entorno.
- secrets del CI.

Activar Google Play App Signing al configurar la aplicación en Play Console.

### Criterios de aceptación

- [x] El repositorio no contiene secretos.
- [x] `.gitignore` protege archivos de firma.
- [x] El bundle de Release queda correctamente firmado con credenciales externas de prueba.
- [x] Se documenta el procedimiento para regenerar una build firmada.
- [ ] El acceso al keystore está respaldado de forma segura.

---

## P0.6 — Verificar Release real

No considerar una aplicación lista porque `debug` funciona.

Todas las pruebas críticas deben realizarse también con una variante `release`.

Validar:

- cold start.
- navegación.
- creación de inversión.
- modificación.
- eliminación.
- movimientos.
- dashboard.
- gráficos.
- proyecciones.
- configuración.
- persistencia.
- migraciones.
- recreación de Activity.
- reinicio completo de la aplicación.

### Criterios de aceptación

- [x] La APK release puede instalarse.
- [x] No existen crashes durante el arranque validado.
- [x] No existen ANRs durante el arranque validado.
- [ ] Los cálculos coinciden con Debug.
- [ ] Persistencia funciona correctamente.
- [ ] Navegación funciona correctamente.

---

# P1 — Hardening funcional

## P1.1 — Lifecycle y recreación

Probar explícitamente:

- rotación.
- cambio de configuración.
- Activity recreation.
- process recreation cuando sea razonable.
- navegación después de recreación.

Verificar especialmente formularios de:

- inversión.
- movimiento.
- institución.
- configuración.

### Criterios de aceptación

- [x] No se duplican registros.
- [x] No se pierde estado crítico.
- [x] No se generan crashes.
- [x] Los ViewModels recuperan correctamente su estado.

Evidencia: `AppE2EFlowsTest.recreatingActivity_preservesDashboardData` y
`recreatingActivity_preservesOpenInvestmentAndTransactionForms` (la recreación
de Activity cubre rotación/cambio de configuración); doble-save cubierto por
`AddInvestmentViewModelTest` y `AddTransactionViewModelTest`.

---

## P1.2 — Navegación Back / Cancel

Validar todos los formularios.

Casos:

```text
Abrir formulario
→ modificar campos
→ Back
```

```text
Abrir formulario
→ modificar campos
→ Cancel
```

```text
Guardar
→ regresar
→ verificar que no se guarde dos veces
```

Decidir explícitamente si los cambios sin guardar:

- se descartan automáticamente; o
- requieren confirmación.

La experiencia debe ser consistente.

### Criterios de aceptación

- [x] Back nunca produce datos parciales.
- [x] Cancel nunca guarda información accidentalmente.
- [x] Save no produce registros duplicados.
- [x] No existen pantallas sin salida.

Evidencia: `AppE2EFlowsTest.investmentForm_cancelKeepsEmpty_createPersistsInvestment`,
`cancelNewTransaction_keepsInvestmentUnchanged` y
`deleteInvestmentDialog_cancelKeepsInvestment_confirmRemovesIt`. Los cambios
sin guardar se descartan al volver (sin confirmación), de forma consistente en
todos los formularios.

---

## P1.3 — Operaciones históricas fuera de orden

Probar movimientos insertados con fechas anteriores a movimientos existentes.

Ejemplo:

```text
2026-01-10 compra
2026-01-15 venta

posteriormente insertar:

2026-01-05 depósito
```

Validar que:

- posiciones.
- costo promedio.
- rendimiento.
- patrimonio.
- gráficas.
- histórico.

se recalculen correctamente.

### Criterios de aceptación

- [x] El orden de inserción no altera el resultado financiero.
- [x] El cálculo depende de la fecha efectiva.
- [x] No aparecen saldos imposibles.
- [x] No aparecen valores `NaN`.
- [x] No aparecen valores infinitos.

Evidencia: `AppE2EFlowsTest.historicalTransactionInsertedOutOfOrder_throughUi_recomputesLedger`;
`LedgerCalculatorTest` cubre varias fechas insertadas fuera de orden;
proyecciones y series inválidas se normalizan con cobertura de tests.

---

## P1.4 — Instalación limpia

Probar en un dispositivo/emulador sin datos previos.

Flujo:

```text
Instalar
→ abrir
→ dashboard vacío
→ crear institución
→ crear inversión
→ registrar depósito
→ registrar compra
→ cerrar
→ volver a abrir
```

### Criterios de aceptación

- [x] First launch sin crash.
- [x] Estado vacío correcto.
- [x] No se requieren datos ficticios.
- [x] Persistencia correcta tras reinicio.

Evidencia: `scripts/device_smoke_test.sh` instala en un dispositivo físico,
hace `pm clear`, verifica el dashboard vacío, recorre el flujo completo desde
cero y termina la app (`am force-stop`) para reabrirla y verificar el total
persistido en el dashboard.

---

## P1.5 — Upgrade / migraciones

Crear una instalación con una versión anterior de la base de datos.

Actualizar sobre ella con la versión actual.

Verificar:

- migración Room.
- integridad.
- foreign keys.
- cálculos.
- índices.
- datos históricos.

Nunca utilizar:

```kotlin
fallbackToDestructiveMigration()
```

como solución para producción, salvo decisión explícita de producto y únicamente cuando la pérdida de datos sea aceptable.

### Criterios de aceptación

- [ ] Upgrade conserva todos los datos.
- [ ] Las migraciones son deterministas.
- [ ] No hay pérdida silenciosa.
- [ ] Los tests de migración pasan.

---

# P1 — Robustez financiera

## P1.6 — Validaciones de dinero

Verificar todos los inputs financieros.

Rechazar cuando corresponda:

- `NaN`.
- infinito.
- cantidades negativas inválidas.
- precio negativo.
- cero cuando no tenga sentido.
- strings no numéricos.
- números extremadamente grandes.
- precision inválida.

Considerar uso consistente de:

```text
BigDecimal
```

o una estrategia monetaria equivalente donde sea necesario.

### Criterios de aceptación

- [x] No existen cálculos con `NaN`.
- [x] No existen resultados infinitos.
- [x] El redondeo está definido.
- [x] Las operaciones financieras críticas tienen tests.

Evidencia: `TransactionFormValidator` / `InvestmentFormValidator` rechazan
NaN, negativos, cero inválido y extremos antes de persistir; redondeo definido
(`Double` con redondeo a 2 decimales, decisión registrada en
`HARDENING_STATUS.md`); operaciones críticas cubiertas por `LedgerCalculatorTest`
y suites de casos de uso.

---

## P1.7 — Eliminación y dependencias

Probar eliminación de:

- institución con inversiones.
- inversión con movimientos.
- movimiento histórico.

Definir comportamiento explícito:

- prohibir.
- cascade.
- soft delete.

No permitir pérdida accidental de información financiera.

### Criterios de aceptación

- [ ] Todas las relaciones tienen comportamiento definido.
- [ ] Se muestran confirmaciones cuando corresponde.
- [ ] No quedan referencias huérfanas.
- [ ] Room foreign keys siguen siendo válidas.

---

# P1 — Performance

## P1.8 — Dataset de estrés

Crear datos sintéticos:

```text
20 instituciones
100 inversiones
1,000 movimientos
```

Opcionalmente ejecutar también:

```text
50 instituciones
500 inversiones
10,000 movimientos
```

Medir:

- tiempo de apertura.
- dashboard.
- scroll.
- filtros.
- gráficas.
- recomposición Compose.
- queries Room.

### Criterios de aceptación

- [x] No existen ANRs.
- [x] El dashboard sigue siendo usable.
- [x] Las listas mantienen scroll fluido.
- [x] No se realizan queries innecesarias por recomposición.
- [ ] Los cálculos pesados no bloquean el Main Thread.

Evidencia: `RoomVolumeTest` en `moto g24` (seed 4.599 s, consulta de
inversiones 16.9 ms, 3.1 ms por inversión, primera emisión 66.0 ms) y
`scripts/device_smoke_test.sh` con revisión de crashes/ANR en dispositivo
físico; la UI no accede a Room (Flows colectados en ViewModel). Pendiente:
la recomputación de ledgers se midió en 269.3 ms para 1 000 movimientos
(abajo de umbrales, baseline conservado — ver `HARDENING_STATUS.md` P2), sin
verificación de dispatcher.

---

# P1 — Compatibilidad

Probar como mínimo:

```text
API 26
API intermedia soportada
API 36
```

Validar:

- pantalla pequeña.
- pantalla grande.
- densidades distintas.
- navegación por gestos.
- fuente aumentada.

### Criterios de aceptación

- [ ] No hay layouts rotos.
- [ ] No hay texto crítico cortado.
- [ ] Todos los CTA son accesibles.
- [ ] Los gráficos siguen siendo legibles.

---

# P1 — Accesibilidad

Revisar:

- content descriptions.
- touch targets.
- contraste.
- escalado de texto.
- TalkBack básico.
- semántica de controles.
- iconos sin texto.

### Criterios de aceptación

- [ ] Los botones importantes tienen etiquetas.
- [ ] Los iconos interactivos pueden identificarse.
- [ ] El texto puede escalar sin romper la navegación.
- [ ] La interfaz crítica es usable con TalkBack.

---

# P1 — Seguridad y privacidad

## Datos locales

Documentar exactamente qué información se almacena.

Ejemplos:

- instituciones.
- nombres de inversiones.
- cantidades invertidas.
- precios.
- movimientos.
- patrimonio.
- rendimiento.
- configuración fiscal.
- inflación estimada.

Determinar explícitamente:

```text
¿Los datos salen del dispositivo?
```

Para la versión local-first actual, la respuesta debería ser verificable desde el código.

Revisar Android Manifest buscando:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Si no existe funcionalidad de red, no agregarla innecesariamente.

---

## Backups

Definir política para:

```text
android:allowBackup
```

Decidir si los datos financieros deben participar en Auto Backup.

Si se permiten backups:

- documentarlo en Privacy Policy.
- revisar reglas de backup.

Si se deshabilitan:

- justificar la decisión.
- verificar comportamiento de restore.

### Criterios de aceptación

- [ ] Existe una política explícita de backup.
- [ ] La configuración del Manifest coincide con la política.
- [ ] Privacy Policy describe correctamente el comportamiento.

---

# P0 — Privacy Policy

Crear una política pública y estable.

Debe describir como mínimo:

## Información almacenada

- datos ingresados por el usuario.
- información financiera registrada manualmente.
- preferencias.

## Almacenamiento

Explicar si los datos:

```text
se almacenan exclusivamente en el dispositivo
```

cuando aplique.

## Transferencia

Indicar si la app:

- envía datos a servidores.
- utiliza analytics.
- utiliza crash reporting.
- comparte datos.
- utiliza advertising IDs.

## Eliminación

Explicar cómo eliminar datos.

Por ejemplo:

- borrar registros dentro de la app.
- limpiar almacenamiento.
- desinstalar aplicación.

## Backups

Indicar si Android puede respaldar la información.

## Contacto

Agregar email oficial del desarrollador.

## Cambios

Incluir política sobre cambios futuros.

### Criterios de aceptación

- [ ] Existe una URL HTTPS pública.
- [ ] La política corresponde al comportamiento real.
- [ ] El email de contacto funciona.
- [ ] El enlace se agrega en Google Play.

---

# P0 — Google Play Data Safety

Completar el formulario Data Safety utilizando únicamente el comportamiento real de la aplicación.

No marcar opciones basándose en lo que "podría hacer en el futuro".

Validar:

- collection.
- sharing.
- encryption.
- deletion.
- analytics.
- diagnostics.
- advertising.
- account information.
- financial information.

Si se añaden en el futuro:

- Firebase.
- Crashlytics.
- Analytics.
- Ads.
- backend.
- sincronización.

revisar nuevamente Data Safety antes de publicar esa actualización.

### Criterios de aceptación

- [ ] Data Safety coincide con el código actual.
- [ ] Se revisaron todas las dependencias de terceros.
- [ ] No existen SDKs que recopilen datos sin declarar.

---

# P0 — Financial Features Declaration

PULSO gestiona información relacionada con inversiones.

Completar:

```text
Play Console
→ App content
→ Financial features
```

Declarar únicamente las funcionalidades reales.

Diferenciar claramente entre:

```text
portfolio management / investment tracking
```

y:

```text
brokerage / execution / custody
```

si la aplicación no ejecuta operaciones financieras reales.

### Criterios de aceptación

- [ ] Financial Features está completado.
- [ ] Las funciones declaradas coinciden con la aplicación.
- [ ] La Store Listing no promete servicios financieros inexistentes.
- [ ] No se presenta la aplicación como asesor financiero si no lo es.

---

# P0 — Store Listing

Preparar:

## Identidad

- nombre definitivo.
- icono.
- package.
- categoría.

## Textos

- short description.
- full description.

No utilizar afirmaciones engañosas como:

```text
garantiza mejores rendimientos
```

o:

```text
predice tus inversiones
```

Las proyecciones deben describirse como estimaciones.

## Recursos gráficos

Preparar:

- App Icon.
- Feature Graphic.
- screenshots de teléfono.
- screenshots adicionales si se requieren.

Screenshots recomendados:

1. Dashboard.
2. Patrimonio.
3. Inversiones.
4. Detalle de inversión.
5. Registro de movimiento.
6. Distribución por institución.
7. Proyecciones.
8. Configuración.

### Criterios de aceptación

- [ ] No aparecen datos personales reales.
- [ ] No aparecen valores financieros reales del desarrollador.
- [ ] Los screenshots utilizan dataset demo.
- [ ] No aparece contenido de debug.
- [ ] El estilo visual es consistente.

---

# P0 — Content Rating

Completar el cuestionario de clasificación.

Responder basándose en funcionalidades reales.

### Criterios de aceptación

- [ ] Content Rating completado.
- [ ] La clasificación obtenida no tiene warnings pendientes.

---

# P0 — Target Audience

Definir público objetivo.

La aplicación está orientada a control financiero/inversiones, por lo que evitar configuraciones de audiencia infantil salvo que exista una razón explícita.

### Criterios de aceptación

- [ ] Target Audience configurado.
- [ ] No existen inconsistencias con políticas de familias.

---

# Testing Track Strategy

## Etapa 1 — Local Release

Generar:

```bash
./gradlew clean
./gradlew test
./gradlew lint
./gradlew bundleRelease
```

---

## Etapa 2 — Internal Testing

Subir `app-release.aab`.

Instalar exclusivamente desde Google Play.

No realizar únicamente sideload.

Validar:

```text
Play Store
→ Internal Testing
→ instalar
→ smoke test completo
```

### Smoke test mínimo

```text
Abrir app
Crear institución
Crear inversión
Registrar depósito
Registrar compra
Abrir dashboard
Editar inversión
Registrar venta
Cerrar app
Abrir nuevamente
Verificar persistencia
```

---

## Etapa 3 — Pre-launch Report

Esperar resultados automáticos de Play.

Revisar:

- crashes.
- ANRs.
- accessibility.
- security.
- compatibility.

Todo crash reproducible debe considerarse bloqueante.

---

## Etapa 4 — Closed Testing

Si la cuenta de Google Play requiere testing cerrado antes de Production:

- crear track Closed.
- agregar testers.
- cumplir duración y cantidad mínima requerida.
- recopilar feedback.
- corregir bloqueadores.

Documentar:

- fecha inicial.
- testers.
- versiones distribuidas.
- bugs encontrados.
- resolución.

---

## Etapa 5 — Production Candidate

Una build candidata debe ser inmutable.

Ejemplo:

```text
1.0.0
versionCode 1
```

Si se encuentra un bug:

```text
1.0.1
versionCode 2
```

No reemplazar silenciosamente builds.

---

# Checklist de Release Candidate

## Build

- [x] API 36 configurada.
- [x] `targetSdk = 36`.
- [x] Application ID definitivo.
- [x] `versionName = 1.0.0`.
- [x] `versionCode` válido.
- [x] Release signing configurado mediante propiedades/variables externas.
- [x] R8 habilitado.
- [x] Resource shrinking habilitado.
- [x] `bundleRelease` pasa con firma externa de prueba.

## Tests

- [x] Unit tests.
- [x] Instrumented tests en API 35.
- [x] E2E.
- [x] Migration tests.
- [x] Lifecycle tests.
- [x] Back/Cancel.
- [x] operaciones históricas.
- [x] clean install.
- [ ] upgrade.
- [x] stress dataset.
- [ ] API 26.
- [x] API 36.

## UX

- [x] Empty states.
- [x] Loading states.
- [x] Error states.
- [x] validación de formularios.
- [x] confirmación destructiva.
- [ ] accesibilidad.
- [x] navegación.

Evidencia: Loading/Empty/Error implementados y verificados en
`HARDENING_STATUS.md` (DoD del roadmap); validación con
`InvestmentFormValidatorTest` / `TransactionFormValidatorTest`; confirmación
destructiva con diálogo de borrado cubierto en `AppE2EFlowsTest`; navegación
por IDs con cobertura de rutas. Accesibilidad sin cobertura de tests
pendiente.

## Seguridad

- [x] Sin secrets en Git.
- [x] Sin keystore en Git.
- [x] Sin passwords hardcoded.
- [x] política de backups definida.
- [x] permisos revisados.
- [ ] dependencias revisadas.

## Google Play

- [ ] App creada en Play Console.
- [ ] Play App Signing.
- [ ] Data Safety.
- [ ] Financial Features.
- [ ] Privacy Policy.
- [ ] Target Audience.
- [ ] Content Rating.
- [ ] Store Listing.
- [ ] Screenshots.
- [ ] Feature Graphic.
- [ ] Icono.
- [ ] Support email.
- [ ] Países de distribución.
- [ ] Internal Testing.
- [ ] Pre-launch Report.
- [ ] Closed Testing si aplica.

---

# P2 — No bloqueantes para 1.0

No retrasar necesariamente la primera publicación por estas funcionalidades.

## Multi-moneda

Considerar para versiones posteriores:

```text
MXN
USD
EUR
etc.
```

Incluyendo:

- currency per asset.
- FX rates.
- base currency.
- historical FX.

---

## Backend

No obligatorio para Release 1.0 si la aplicación continúa siendo local-first.

Posibles fases posteriores:

- sync.
- backups propios.
- multi-device.
- cuentas.
- autenticación.

---

## Analytics

No agregar analytics únicamente "porque todas las apps lo tienen".

Si se agrega:

- justificar métricas necesarias.
- minimizar datos.
- actualizar Privacy Policy.
- actualizar Data Safety.

---

## Crash reporting

Puede evaluarse después de la versión inicial.

Si se utiliza Crashlytics o equivalente:

- revisar datos enviados.
- actualizar Data Safety.
- actualizar Privacy Policy.

---

## Publicidad

No introducir anuncios en esta fase salvo que forme parte explícita del alcance de Release 1.0.

Agregar Ads implica revisar:

- Consent.
- Advertising ID.
- Data Safety.
- Privacy Policy.
- UX.
- SDK dependencies.

---

# Reglas para el agente

## Regla 1 — No introducir regresiones

Cada modificación debe mantener:

```text
tests existentes = passing
```

No eliminar tests para hacer pasar la build.

---

## Regla 2 — Un cambio a la vez

Aplicar cambios en bloques pequeños.

Ejemplo:

```text
API 36
→ build
→ tests
→ commit
```

Después:

```text
applicationId
→ build
→ tests
→ commit
```

No mezclar 20 cambios estructurales en un único commit.

---

## Regla 3 — No cambiar arquitectura sin necesidad

La arquitectura actual debe preservarse salvo bug demostrado.

No reescribir:

- Room.
- Repository.
- Use Cases.
- ViewModels.
- Compose navigation.

únicamente por preferencias personales del agente.

---

## Regla 4 — No agregar dependencias innecesarias

Cada nueva dependencia debe tener un propósito concreto.

Preferir AndroidX / Jetpack cuando sea suficiente.

---

## Regla 5 — Datos financieros

No reemplazar lógica financiera sin tests.

Toda modificación de:

- rendimiento.
- costo.
- patrimonio.
- proyección.
- ISR.
- inflación.
- posiciones.

debe incluir pruebas.

---

## Regla 6 — Protección de datos

No agregar:

- telemetry.
- analytics.
- crash reporting.
- tracking.
- advertising.

sin documentarlo explícitamente.

---

## Regla 7 — No esconder warnings relevantes

Si aparece un warning que afecte:

- Android 16.
- Play policies.
- Room.
- security.
- release.
- signing.

debe documentarse.

---

# Formato esperado de trabajo del agente

Para cada tarea entregar:

```markdown
## Cambio realizado

Descripción.

## Archivos modificados

- file A
- file B

## Motivo

Explicación técnica.

## Validación

- command
- result

## Riesgos

Lista corta.

## Pendiente

Siguiente paso.
```

---

# Orden recomendado de ejecución

El agente debe ejecutar esta fase exactamente en este orden salvo bloqueo técnico:

```text
1. Crear baseline y ejecutar tests actuales
2. Migrar compileSdk / targetSdk a API 36
3. Resolver incompatibilidades
4. Ejecutar tests
5. Definir Application ID definitivo
6. Refactor package
7. Ejecutar tests
8. Definir version 1.0.0
9. Configurar Release
10. Configurar R8
11. Configurar signing
12. Generar bundleRelease
13. Probar Release
14. Resolver Lifecycle / Back / Cancel
15. Validar operaciones históricas
16. Ejecutar migration tests
17. Ejecutar dataset de estrés
18. Revisar accesibilidad
19. Revisar permisos y backups
20. Preparar Privacy Policy
21. Preparar Data Safety
22. Preparar Financial Features declaration
23. Preparar Store Listing
24. Preparar screenshots
25. Subir Internal Testing
26. Ejecutar smoke test desde Play
27. Revisar Pre-launch Report
28. Closed Testing si aplica
29. Congelar Release Candidate
30. Publicar Production
```

---

# Definition of Done — Fase 6

La Fase 6 se considera terminada cuando:

```text
Existe un AAB firmado de producción
+
targetSdk = 36
+
todos los tests pasan
+
la Release funciona instalada desde Google Play
+
Data Safety completado
+
Financial Features completado
+
Privacy Policy publicada
+
Store Listing terminada
+
Pre-launch Report sin bloqueadores
+
Closed Testing completado si aplica
```

Y finalmente:

```text
PULSO 1.0 puede promoverse a Production sin cambios adicionales de código.
```

---

# Resultado esperado

Al finalizar esta fase deben existir como mínimo:

```text
app-release.aab
Privacy Policy
Store Listing
Screenshots
Release Notes
Play Console configurado
Reporte de pruebas
Checklist de Release completado
```

---

# Fuera de alcance

No implementar durante esta fase salvo necesidad crítica:

- backend.
- cuentas de usuario.
- sincronización cloud.
- multi-device.
- multi-moneda avanzada.
- importación bancaria automática.
- conexión a brokers.
- ejecución real de órdenes.
- recomendaciones de inversión.
- machine learning.
- publicidad.
- suscripciones.

Estas funciones corresponden a versiones posteriores.

---

# Próxima fase sugerida

Después de publicar la versión 1.0:

## Fase 7 — Post-launch & Productization

Posibles objetivos:

- crash monitoring.
- feedback.
- métricas.
- export / import.
- backup controlado.
- multi-moneda.
- monetización.
- sync opcional.
- arquitectura multiusuario.
- preparación para versión no exclusivamente personal.
