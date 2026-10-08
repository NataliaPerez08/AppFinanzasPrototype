# PULSO --- Implementación de App Lock + corrección global de Window Insets

## Objetivo

Implementar dos mejoras de infraestructura en PULSO:

1.  Corregir globalmente el manejo de `WindowInsets`, edge-to-edge,
    status bar, navigation bar e IME.
2.  Implementar un sistema de bloqueo local mediante PIN de 4 dígitos +
    biometría.

Esta tarea debe ejecutarse sobre la arquitectura existente.

Antes de modificar código, inspeccionar el proyecto para identificar:

-   arquitectura;
-   navegación;
-   DI;
-   persistencia;
-   DataStore / SharedPreferences existentes;
-   Room;
-   lifecycle handling;
-   estructura de ViewModels;
-   `MainActivity`;
-   root composables;
-   `Scaffold`;
-   Bottom Navigation;
-   configuración actual de edge-to-edge;
-   versión mínima de Android;
-   dependencias disponibles.

No realizar refactors generales que no sean necesarios para esta tarea.

------------------------------------------------------------------------

# 1. Decisiones de producto

Las siguientes decisiones están cerradas y NO deben reinterpretarse.

## UI / Insets

Realizar auditoría global de edge-to-edge e insets.

No solucionar únicamente la pantalla de Inversiones.

## App Lock

El bloqueo es:

-   local-only;
-   opcional;
-   activable desde `Más → Seguridad`;
-   PIN exactamente de 4 dígitos;
-   compatible con biometría;
-   PIN siempre disponible como fallback;
-   sin backend;
-   sin cuenta de usuario;
-   sin recuperación remota.

## Auto-lock

Opciones:

-   Inmediatamente
-   30 segundos
-   1 minuto
-   5 minutos
-   15 minutos

## PIN olvidado

La única recuperación permitida es:

> Resetear PULSO eliminando todos los datos locales.

Nunca permitir resetear únicamente el PIN conservando los datos
financieros.

## Intentos fallidos

Aplicar espera progresiva.

No eliminar automáticamente los datos por intentos fallidos.

## Screenshots

NO bloquear capturas de pantalla.

No utilizar `FLAG_SECURE` como parte de esta implementación.

## Diseño

Mantener identidad PULSO:

-   neo-brutalista;
-   tecnológica;
-   sobria;
-   fondos claros existentes;
-   negro carbón;
-   naranja PULSO;
-   bordes finos;
-   retícula;
-   sin sombras decorativas;
-   sin gradientes;
-   sin Material Design genérico que rompa la identidad visual.

------------------------------------------------------------------------

# 2. FASE A --- Auditoría de Window Insets

Antes de modificar código buscar todos los usos de:

``` text
enableEdgeToEdge
WindowInsets
WindowInsetsCompat
safeDrawing
safeContent
systemBars
statusBars
navigationBars
ime
statusBarsPadding
navigationBarsPadding
systemBarsPadding
safeDrawingPadding
consumeWindowInsets
windowInsetsPadding
contentWindowInsets
```

También buscar:

-   padding inferior manual;
-   padding superior manual;
-   alturas hardcoded asociadas a barras del sistema;
-   `Scaffold` anidados;
-   `NavigationBar`;
-   Bottom Navigation custom;
-   uso de `innerPadding`.

Documentar brevemente qué estaba causando el problema antes de
modificarlo.

------------------------------------------------------------------------

# 3. Edge-to-edge

PULSO debe utilizar edge-to-edge correctamente.

En la Activity raíz utilizar la API moderna correspondiente, por
ejemplo:

``` kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()

    setContent {
        PulsoTheme {
            PulsoApp()
        }
    }
}
```

Adaptarlo a la implementación existente.

No duplicar configuración si ya existe.

No utilizar APIs deprecated si existe alternativa moderna compatible con
el proyecto.

------------------------------------------------------------------------

# 4. Responsabilidad de los Insets

Definir una única estrategia consistente.

Evitar casos como:

``` kotlin
Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing
) { innerPadding ->

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .navigationBarsPadding()
    )
}
```

cuando ambas capas estén aplicando el mismo inset.

Principio:

> Cada inset debe tener un propietario claro.

No solucionar problemas visuales acumulando `padding()` hasta que "se
vea bien".

------------------------------------------------------------------------

# 5. Root Scaffold

Revisar el `Scaffold` principal y determinar dónde deben consumirse:

-   status bars;
-   navigation bars;
-   display cutouts;
-   IME.

La solución debe funcionar globalmente.

La estructura conceptual puede ser similar a:

``` kotlin
Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
        PulsoBottomNavigation(...)
    }
) { innerPadding ->

    AppNavHost(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    )
}
```

Esto es una referencia conceptual.

NO copiar literalmente si la arquitectura existente requiere otra
distribución.

------------------------------------------------------------------------

# 6. Bottom Navigation

Corregir específicamente el problema visible actualmente.

La barra inferior de PULSO debe:

-   respetar la barra de navegación Android;
-   funcionar con navegación por gestos;
-   funcionar con navegación de 3 botones;
-   no quedar debajo de controles del sistema;
-   no producir un bloque vacío excesivo;
-   mantener altura visual compacta;
-   conservar borde superior;
-   conservar navegación existente;
-   conservar naranja para item activo;
-   mantener targets táctiles accesibles.

Revisar especialmente si actualmente existe:

``` text
Scaffold inset
+
BottomNavigation inset
+
navigationBarsPadding
+
padding manual
```

provocando acumulación.

Eliminar cualquier duplicación encontrada.

------------------------------------------------------------------------

# 7. Status Bar

Revisar todas las pantallas principales:

-   Inicio
-   Inversiones
-   Patrimonio
-   Proyección
-   Más

Y pantallas secundarias:

-   crear inversión;
-   editar inversión;
-   instituciones;
-   configuración;
-   seguridad;
-   formularios;
-   diálogos;
-   bottom sheets.

Ningún título o contenido interactivo debe quedar debajo de la status
bar.

------------------------------------------------------------------------

# 8. IME / teclado

Verificar formularios con teclado abierto.

El teclado no debe:

-   ocultar campos activos;
-   ocultar CTA importantes;
-   crear padding doble;
-   desplazar incorrectamente Bottom Navigation;
-   provocar saltos visuales absurdos.

Utilizar las APIs de IME/insets correspondientes cuando sea necesario.

------------------------------------------------------------------------

# 9. FASE B --- App Lock

Crear un módulo/capa de seguridad local claramente separado del dominio
financiero.

La arquitectura exacta debe adaptarse al proyecto.

Una estructura razonable sería:

``` text
security/
├── AppLockManager.kt
├── LockState.kt
├── LockPolicy.kt
├── PinRepository.kt
├── PinHasher.kt
├── BiometricAuthenticator.kt
└── model/
```

UI:

``` text
ui/security/
├── UnlockScreen.kt
├── UnlockViewModel.kt
├── SetupPinScreen.kt
├── SetupPinViewModel.kt
├── ConfirmPinScreen.kt
├── SecuritySettingsScreen.kt
└── ForgotPinScreen.kt
```

No crear abstracciones innecesarias solamente para reproducir esta
estructura.

------------------------------------------------------------------------

# 10. Estados de App Lock

Crear un modelo explícito.

Por ejemplo:

``` kotlin
sealed interface LockState {
    data object Disabled : LockState
    data object Locked : LockState
    data object Unlocked : LockState
}
```

Puede existir además un estado inicial/loading si la persistencia es
asíncrona:

``` kotlin
data object Loading : LockState
```

Esto es especialmente importante para evitar mostrar información
financiera antes de resolver el estado del bloqueo.

------------------------------------------------------------------------

# 11. Requisito crítico: NO FLASH

Al iniciar PULSO:

``` text
START
  │
  ▼
Resolver configuración de seguridad
  │
  ├── App Lock OFF → App
  │
  └── App Lock ON → Locked → UnlockScreen
```

PROHIBIDO:

``` text
START
 ↓
Dashboard
 ↓
leer configuración
 ↓
UnlockScreen
```

No debe existir ni siquiera brevemente un frame donde aparezca
patrimonio, inversiones, saldos, instituciones, rendimiento o
proyecciones.

Mientras el estado de seguridad se resuelve, mostrar una superficie
neutra/splash compatible con PULSO.

------------------------------------------------------------------------

# 12. Activación del PIN

El App Lock es opcional.

Agregar en `Más → Seguridad`:

``` text
Bloqueo de PULSO        [ OFF ]
Biometría               [ -- ]
Bloqueo automático         >
```

Al activar:

``` text
Bloqueo PULSO
      ↓
Crear PIN
      ↓
Confirmar PIN
      ↓
¿Coinciden?
 ├── NO → Error
 └── SÍ → Persistir credencial → App Lock ON
```

No considerar App Lock activo hasta que la configuración se haya
persistido correctamente.

------------------------------------------------------------------------

# 13. PIN

El PIN debe tener exactamente 4 dígitos y aceptar únicamente `0-9`.

No almacenar el PIN original. No registrarlo ni enviarlo a analytics,
crash reporting, SavedState, logs, Room, intents o bundles.

Evitar mantener el valor más tiempo del necesario.

------------------------------------------------------------------------

# 14. Almacenamiento del PIN

PROHIBIDO almacenar `pin = "1234"`.

PROHIBIDO depender únicamente de `SHA256(pin)`.

Un PIN de cuatro dígitos tiene únicamente 10,000 combinaciones.

Utilizar:

``` text
PIN + random salt + KDF → derived credential
```

Usar una KDF apropiada disponible en Android/JVM. PBKDF2 es una opción
aceptable si es compatible con el proyecto.

Persistir como mínimo:

``` text
algorithmVersion
salt
iterations/workFactor
derivedCredential
```

Los parámetros deben estar versionados para permitir futuras
migraciones.

------------------------------------------------------------------------

# 15. Verificación

Conceptualmente:

``` text
enteredPin + storedSalt + storedParameters
        ↓
       KDF
        ↓
candidateCredential
        ↓
constant-time comparison
        ↓
 true / false
```

Evitar comparaciones inseguras cuando sea razonablemente posible.

------------------------------------------------------------------------

# 16. Android Keystore

Utilizar Android Keystore cuando aporte protección real.

El agente debe inspeccionar primero cómo se persisten actualmente los
datos sensibles.

Mantener separación entre:

``` text
PinHasher
PinRepository
AppLockManager
BiometricAuthenticator
```

------------------------------------------------------------------------

# 17. Unlock Screen

Crear una pantalla específica siguiendo la identidad visual de PULSO.

Conceptualmente:

``` text
┌──────────────────────────────┐
│            PULSO             │
│                              │
│       DESBLOQUEAR PULSO      │
│                              │
│          ● ● ○ ○             │
│                              │
│       1     2     3          │
│       4     5     6          │
│       7     8     9          │
│       BIO   0     ←          │
│                              │
│       OLVIDÉ MI PIN          │
└──────────────────────────────┘
```

El mockup es conceptual. El diseño final debe reutilizar componentes y
tokens existentes.

------------------------------------------------------------------------

# 18. Keypad

Preferir keypad propio en lugar de abrir el teclado del sistema.

Requisitos:

-   botones grandes;
-   targets táctiles accesibles;
-   feedback visual;
-   botón borrar;
-   máximo 4 dígitos;
-   indicadores en lugar del PIN real.

Cuando se introduzcan los 4 dígitos puede iniciarse automáticamente la
validación. Evitar doble submit.

------------------------------------------------------------------------

# 19. PIN incorrecto

Mostrar feedback coherente con PULSO, por ejemplo `PIN INCORRECTO`.

No revelar información adicional sobre la credencial.

Limpiar los cuatro dígitos después del fallo.

------------------------------------------------------------------------

# 20. Protección contra fuerza bruta

Implementar espera progresiva, encapsulada en `LockPolicy`.

Propuesta inicial:

``` text
1-4 fallos: sin espera
5 fallos: 30 segundos
6 fallos: 1 minuto
7 fallos: 5 minutos
8+ fallos: 15 minutos
```

El cooldown debe sobrevivir razonablemente a recomposición, navegación,
background/foreground y reinicio del proceso cuando corresponda.

No debe poder evitarse simplemente cerrando y abriendo la pantalla.

PROHIBIDO borrar automáticamente datos financieros debido a intentos
fallidos.

------------------------------------------------------------------------

# 21. Biometría

Implementar biometría en esta fase mediante APIs oficiales:

``` text
AndroidX Biometric
BiometricPrompt
```

Nunca implementar autenticación biométrica propia.

------------------------------------------------------------------------

# 22. Relación PIN / biometría

PIN es el fallback.

Biometría NO revela el PIN, recupera el PIN, sustituye permanentemente
el PIN, modifica el PIN ni permite cambiar configuración de seguridad
sin las validaciones requeridas.

Flujo:

``` text
UnlockScreen
   ├── PIN → validar
   └── Biometría → BiometricPrompt → success → unlock
```

------------------------------------------------------------------------

# 23. Configuración biométrica

En `Más → Seguridad` mostrar:

``` text
Bloqueo de PULSO          ON
Biometría                 ON/OFF
Bloqueo automático        1 min
Cambiar PIN                  >
Olvidé mi PIN                >
```

La opción biométrica debe aparecer habilitable únicamente cuando App
Lock esté activo y el dispositivo soporte autenticación compatible.

Manejar correctamente hardware no disponible, biometría no configurada,
lockout, cancelación y error del sistema.

Siempre permitir PIN.

------------------------------------------------------------------------

# 24. Auto-lock

Opciones exactas:

``` text
Inmediatamente
30 segundos
1 minuto
5 minutos
15 minutos
```

Persistir la elección.

El valor inicial recomendado es `1 minuto`.

------------------------------------------------------------------------

# 25. Lifecycle

Detectar cuándo PULSO abandona foreground.

Al volver:

``` text
elapsed >= selectedTimeout → Locked
elapsed < selectedTimeout → mantener Unlocked
```

Para `Inmediatamente`, bloquear al abandonar foreground según la
semántica definida por lifecycle.

Evitar falsos bloqueos provocados por `BiometricPrompt`, dialogs del
sistema, permission dialogs o cambios internos de Activity.

------------------------------------------------------------------------

# 26. Inicio en frío

Si App Lock está habilitado, un cold start debe comenzar bloqueado.

``` text
process restart + App Lock enabled = Locked
```

------------------------------------------------------------------------

# 27. Cambiar PIN

Flujo:

``` text
Cambiar PIN
     ↓
PIN actual
     ↓
validar
     ↓
Nuevo PIN
     ↓
Confirmar PIN
     ↓
persistir
```

Solicitar siempre el PIN actual.

------------------------------------------------------------------------

# 28. Desactivar App Lock

Flujo:

``` text
Bloqueo PULSO ON
       ↓
Usuario desactiva
       ↓
Solicitar PIN actual
       ↓
Validar
       ↓
Eliminar configuración/credencial
       ↓
Bloqueo OFF
```

No permitir desactivarlo simplemente pulsando el switch.

------------------------------------------------------------------------

# 29. PIN olvidado

Decisión de producto:

> Olvidar el PIN implica resetear completamente los datos locales de
> PULSO.

NO existe recuperación del PIN.

NO existe email, SMS, backend, pregunta secreta, PIN maestro, código
oculto o bypass biométrico para establecer un PIN nuevo conservando
datos.

------------------------------------------------------------------------

# 30. Flujo de reset

Al seleccionar `OLVIDÉ MI PIN`, mostrar advertencia explícita y doble
confirmación destructiva.

El usuario debe entender que se eliminarán inversiones, movimientos,
instituciones, configuración y demás datos locales.

No utilizar dark patterns.

------------------------------------------------------------------------

# 31. Reset transaccional

El reset debe eliminar TODOS los datos locales relevantes.

Auditar:

-   Room databases;
-   DataStore;
-   SharedPreferences;
-   archivos internos;
-   cachés con información financiera;
-   preferencias de seguridad;
-   credenciales derivadas;
-   configuración biométrica;
-   configuración App Lock;
-   instituciones personalizadas;
-   inversiones;
-   movimientos;
-   proyecciones/configuración;
-   cualquier otra persistencia del usuario.

No dejar PULSO en un estado parcialmente borrado.

Después:

``` text
clean local state
     ↓
App Lock OFF
     ↓
onboarding / initial empty state
```

según el flujo inicial existente.

------------------------------------------------------------------------

# 32. Screenshots

Las capturas de pantalla permanecen permitidas.

No introducir `FLAG_SECURE` ni mecanismos equivalentes.

------------------------------------------------------------------------

# 33. Navegación

App Lock debe funcionar como un gate del contenido financiero.

No debe ser simplemente otra destination que pueda saltarse mediante
back navigation o deep link.

Conceptualmente:

``` kotlin
when (lockState) {
    Loading -> SecureStartupSurface()
    Disabled, Unlocked -> PulsoAppContent()
    Locked -> UnlockScreen()
}
```

------------------------------------------------------------------------

# 34. Back button

Desde `UnlockScreen`, Back NO debe mostrar contenido protegido.

Puede cerrar/minimizar la aplicación según comportamiento Android
apropiado.

------------------------------------------------------------------------

# 35. Deep links / intents

Si PULSO tiene entry points externos, App Lock debe permanecer por
encima del destino solicitado.

No permitir bypass mediante intents.

------------------------------------------------------------------------

# 36. Seguridad de datos en memoria/UI

Cuando PULSO pase a `Locked`:

-   ocultar inmediatamente contenido financiero;
-   evitar mantener una captura visual sensible detrás del lock screen
    cuando sea razonable;
-   limpiar estado temporal del PIN;
-   cancelar validaciones pendientes cuando corresponda.

------------------------------------------------------------------------

# 37. Accessibility

El keypad debe ser accesible.

Los indicadores deben comunicar, por ejemplo,
`2 de 4 dígitos introducidos`, pero nunca leer el PIN parcial.

------------------------------------------------------------------------

# 38. Tests --- Insets

Probar navegación por gestos y navegación de 3 botones, incluyendo
portrait, pantallas principales, formularios, teclado abierto y teclado
cerrado.

Verificar:

-   no overlap;
-   no padding duplicado;
-   Bottom Navigation compacta;
-   status bar correcta;
-   IME correcto.

------------------------------------------------------------------------

# 39. Tests --- PIN hashing

Cubrir:

``` text
same PIN + same salt → same credential
same PIN + different salt → different credential
correct PIN → true
wrong PIN → false
```

Verificar además parámetros/versionado del KDF.

------------------------------------------------------------------------

# 40. Tests --- AppLockManager

Cubrir como mínimo:

``` text
App Lock OFF → app accesible
App Lock ON + cold start → Locked
correct PIN → Unlocked
incorrect PIN → Locked
biometric success → Unlocked
biometric failure → Locked
```

------------------------------------------------------------------------

# 41. Tests --- Auto-lock

Probar cada opción:

``` text
Immediate
30 sec
1 min
5 min
15 min
```

Casos límite:

``` text
elapsed < timeout → unlocked
elapsed >= timeout → locked
```

Evitar tests que realmente esperen minutos. Inyectar clock/time provider
para tests deterministas.

------------------------------------------------------------------------

# 42. Tests --- Intentos fallidos

Probar:

``` text
1-4 → sin cooldown
5 → cooldown
cooldown activo → no permitir nuevos intentos
cooldown terminado → permitir intento
```

Verificar progresión posterior.

------------------------------------------------------------------------

# 43. Tests --- Reset

Crear datos de prueba y ejecutar:

``` text
Forgot PIN
→ confirm
→ confirm destructive action
→ reset
```

Verificar:

``` text
Room = clean
preferences = clean
security state = clean
App Lock = disabled
financial data = absent
```

------------------------------------------------------------------------

# 44. UI Tests críticos

Cubrir cuando sea viable:

### Cold start protegido

``` text
Launch → UnlockScreen → NO financial flash
```

### PIN correcto

``` text
Launch → PIN → Dashboard
```

### PIN incorrecto

``` text
Launch → wrong PIN → error → remains locked
```

### Biometría

Utilizar abstracción/mock apropiado para tests cuando `BiometricPrompt`
real no sea automatizable.

### Background

``` text
Dashboard → background → timeout → foreground → UnlockScreen
```

------------------------------------------------------------------------

# 45. Security review

Antes de terminar buscar en el código cualquier posibilidad de
exposición de:

``` text
PIN
derived credential
salt
biometric state
```

Confirmar que el PIN no termina en Logcat, exception messages,
analytics, Room, SavedStateHandle, Bundle, crash reports o debug output.

------------------------------------------------------------------------

# 46. Backup

Revisar la configuración actual de Android Backup.

Verificar específicamente cómo interactúan:

``` text
Room
DataStore
security configuration
Android Keystore
Auto Backup
device restore
```

No cambiar silenciosamente la política global de backup.

Si existe un problema o una decisión de producto pendiente, documentarlo
claramente en el reporte final.

------------------------------------------------------------------------

# 47. Dependencias

Agregar únicamente dependencias necesarias.

Para biometría preferir `androidx.biometric`.

Usar una versión estable compatible con el proyecto.

No actualizar Compose, Kotlin, AGP o librerías no relacionadas salvo
necesidad real.

------------------------------------------------------------------------

# 48. Definition of Done

## Insets

-   [ ] Edge-to-edge está configurado correctamente.
-   [ ] Se auditó el manejo global de WindowInsets.
-   [ ] Se eliminó padding duplicado.
-   [ ] Bottom Navigation funciona con gestos.
-   [ ] Bottom Navigation funciona con 3 botones.
-   [ ] No existe el espacio inferior excesivo observado originalmente.
-   [ ] Status bar no cubre contenido.
-   [ ] IME funciona correctamente.
-   [ ] Pantallas principales fueron verificadas.

## PIN

-   [ ] App Lock es opcional.
-   [ ] Puede activarse desde Seguridad.
-   [ ] PIN tiene exactamente 4 dígitos.
-   [ ] PIN no se almacena directamente.
-   [ ] Existe salt aleatorio.
-   [ ] Se utiliza KDF.
-   [ ] Existe comparación segura.
-   [ ] Cold start bloquea cuando corresponde.
-   [ ] No existe flash de información financiera.
-   [ ] PIN correcto desbloquea.
-   [ ] PIN incorrecto no desbloquea.
-   [ ] Back no permite bypass.

## Biometría

-   [ ] AndroidX Biometric integrado.
-   [ ] Puede activarse/desactivarse.
-   [ ] Success desbloquea.
-   [ ] Failure no desbloquea.
-   [ ] Cancelación funciona correctamente.
-   [ ] PIN siempre está disponible como fallback.
-   [ ] Dispositivos sin biometría funcionan correctamente.

## Auto-lock

-   [ ] Inmediato funciona.
-   [ ] 30 segundos funciona.
-   [ ] 1 minuto funciona.
-   [ ] 5 minutos funciona.
-   [ ] 15 minutos funciona.
-   [ ] Estado sobrevive correctamente al lifecycle.
-   [ ] Process restart inicia bloqueado.

## Intentos

-   [ ] Existe política progresiva.
-   [ ] Cooldown no puede evitarse simplemente navegando.
-   [ ] No se borran datos por intentos fallidos.

## Reset

-   [ ] Forgot PIN informa que no existe recuperación.
-   [ ] Requiere confirmación destructiva.
-   [ ] Requiere segunda confirmación.
-   [ ] Elimina todos los datos locales.
-   [ ] Elimina configuración de seguridad.
-   [ ] Regresa a estado inicial limpio.
-   [ ] No deja datos parcialmente eliminados.

## General

-   [ ] Screenshots continúan permitidas.
-   [ ] No se introdujo backend.
-   [ ] No se introdujeron cuentas.
-   [ ] No se rompieron flujos financieros.
-   [ ] Tests existentes pasan.
-   [ ] Tests nuevos pasan.
-   [ ] Debug build pasa.
-   [ ] Release build pasa.

------------------------------------------------------------------------

# 49. Restricciones

NO:

-   crear backend;
-   crear login;
-   crear cuentas;
-   almacenar PIN plaintext;
-   almacenar PIN reversible;
-   utilizar SHA-256 simple como mecanismo de PIN;
-   implementar biometría propia;
-   permitir recuperación secreta del PIN;
-   permitir reset del PIN conservando datos;
-   borrar datos automáticamente por intentos fallidos;
-   bloquear screenshots;
-   parchear cada pantalla con padding arbitrario;
-   introducir APIs deprecated innecesariamente;
-   rediseñar PULSO;
-   cambiar lógica financiera;
-   actualizar dependencias no relacionadas sin necesidad.

------------------------------------------------------------------------

# 50. Orden de ejecución

1.  Inspeccionar arquitectura actual.
2.  Ejecutar baseline de tests/build.
3.  Auditar WindowInsets.
4.  Identificar causa del problema visual actual.
5.  Corregir estrategia edge-to-edge global.
6.  Corregir Bottom Navigation.
7.  Validar status bar.
8.  Validar IME.
9.  Ejecutar regresión visual.
10. Diseñar capa App Lock adaptada a arquitectura existente.
11. Implementar persistencia segura del PIN.
12. Implementar KDF.
13. Implementar `AppLockManager`.
14. Implementar Setup PIN.
15. Implementar Confirm PIN.
16. Implementar UnlockScreen.
17. Integrar gate de seguridad.
18. Verificar NO-FLASH.
19. Implementar auto-lock.
20. Implementar cooldown progresivo.
21. Integrar AndroidX Biometric.
22. Implementar Security Settings.
23. Implementar cambio de PIN.
24. Implementar desactivación segura.
25. Implementar Forgot PIN.
26. Implementar reset total local.
27. Añadir tests unitarios.
28. Añadir tests UI/instrumentation cuando corresponda.
29. Ejecutar suite completa.
30. Ejecutar debug build.
31. Ejecutar release build.
32. Realizar revisión final de seguridad.
33. Entregar reporte.

------------------------------------------------------------------------

# 51. Reporte obligatorio del agente

Al terminar crear:

`SECURITY_AND_INSETS_IMPLEMENTATION_REPORT.md`

Debe contener:

## Root cause

Explicar cuál era la causa real del problema de espacio/insets.

## Cambios realizados

Listar archivos modificados y responsabilidad de cada cambio.

## App Lock

Documentar arquitectura final.

## Seguridad

Documentar:

-   KDF elegida;
-   parámetros;
-   salt;
-   persistencia;
-   uso de Android Keystore;
-   comparación de credenciales;
-   cooldown;
-   comportamiento biométrico.

NO incluir PIN reales, secretos o claves.

## Reset

Documentar exactamente qué fuentes de datos se eliminan.

## Tests

Incluir tests ejecutados, pasados y fallidos.

## Builds

Incluir resultado de debug y release.

## Deuda técnica

Listar cualquier problema encontrado que no pertenezca al scope actual.

## Decisiones pendientes

Si durante la implementación aparece una decisión que pueda provocar
pérdida de datos, cambiar la política de backup, modificar
compatibilidad Android, debilitar seguridad o cambiar la UX definida,
DETENER esa parte de la implementación y documentar la decisión
necesaria en lugar de asumirla.

------------------------------------------------------------------------

# Resultado esperado

Al finalizar, PULSO debe conservar su filosofía actual:

> Una aplicación financiera local, rápida y sin cuentas, pero cuyos
> datos no quedan expuestos inmediatamente a cualquiera que tenga acceso
> al teléfono.

El sistema de seguridad debe añadir una barrera local razonable sin
fingir que un PIN de cuatro dígitos convierte el dispositivo en Fort
Knox.

La corrección de edge-to-edge debe ser estructural y no una colección de
paddings mágicos.
