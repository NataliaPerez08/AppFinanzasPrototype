# App Finanzas — Roadmap de Integración UI

## Objetivo

Integrar el nuevo prototipo visual de **App Finanzas** en el proyecto Android existente, manteniendo la arquitectura y funcionalidad desarrolladas durante las fases 1–5.

El prototipo visual es una referencia de **UI/UX**.

No debe utilizarse como una nueva arquitectura ni como una nueva fuente de datos.

> El objetivo es evolucionar la aplicación existente, no reconstruirla desde cero.

---

# Arquitectura objetivo

```text
┌─────────────────────┐
│   Jetpack Compose   │
│         UI          │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│      ViewModel      │
│      UiState        │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│      Use Cases      │
│       Domain        │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│     Repository      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Room / Data Sources │
└─────────────────────┘
```

La UI nunca debe acceder directamente a Room ni implementar cálculos financieros.

---

# Flujo principal

```text
Instituciones
      │
      ▼
Agregar inversión
      │
      ▼
Registrar movimiento
      │
      ▼
     Room
      │
      ▼
 Repository
      │
      ▼
  Use Cases
      │
      ▼
  ViewModels
      │
      ├──────────────┐
      ▼              ▼
 Dashboard      Inversiones
      │              │
      └──────┬───────┘
             ▼
        Patrimonio
             │
             ▼
        Proyección
```

---

# FASE 1 — Fundación UI

## Objetivo

Integrar el nuevo sistema visual sin modificar innecesariamente la arquitectura existente.

## 1.1 Auditoría del proyecto

Revisar:

- estructura de módulos
- paquetes
- Gradle
- dependencias
- navegación
- entidades Room
- DAOs
- repositories
- use cases
- ViewModels
- modelos de dominio
- tests existentes

Documentar qué componentes actuales pueden reutilizarse.

No modificar arquitectura durante esta auditoría salvo que exista un problema concreto.

---

## 1.2 Design System

Crear un sistema visual centralizado.

### Colores

```text
Background       #E4E3E1
Surface          #F4F3F1
Divider          #C6C5C2
Text             #111110
Accent           #FF641C
```

El naranja se utilizará únicamente para:

- selección
- navegación activa
- CTA
- métricas destacadas
- estados relevantes
- datos financieros importantes

Evitar:

- gradientes
- sombras
- elevaciones innecesarias
- tarjetas excesivamente redondeadas

---

## 1.3 Tipografía

Definir dos estilos principales.

### Display

Utilizado para:

- títulos
- patrimonio
- KPIs
- cifras principales

Estética:

- tecnológica
- geométrica
- modular
- extended

### Mono

Utilizado para:

- precios
- porcentajes
- fechas
- labels
- filtros
- navegación
- métricas

---

## 1.4 Componentes base

Crear componentes reutilizables:

```text
FinanceTopBar
FinanceBottomNavigation
FinancePanel
MetricCard
PrimaryMetric
SectionHeader
SegmentedFilter
AssetRow
InstitutionRow
FinanceButton
FinanceTextField
MoneyMetric
PercentageMetric
AllocationBar
PortfolioChart
ProjectionChart
```

Los componentes no deben conocer Room ni repositories.

---

## 1.5 Navegación

Navegación inferior:

```text
Inicio
Inversiones
Patrimonio
Proyección
Más
```

Rutas secundarias:

```text
InvestmentDetail/{investmentId}

AddInvestment

AddTransaction/{investmentId}

Institutions

Settings
```

Pasar IDs entre pantallas.

Nunca pasar entidades completas mediante navegación.

---

## Resultado esperado Fase 1

La aplicación:

- compila
- conserva funcionalidad existente
- tiene el nuevo Design System
- tiene navegación base
- dispone de componentes reutilizables
- puede representar las pantallas del prototipo
- mantiene los tests existentes

---

# FASE 2 — Dashboard e Inversiones

## Objetivo

Conectar las principales pantallas del prototipo con información real del dominio.

---

## 2.1 Dashboard

Implementar:

```text
DashboardScreen
DashboardViewModel
DashboardUiState
```

Mostrar:

- patrimonio actual
- variación diaria
- capital aportado
- ganancias
- rendimiento
- evolución histórica
- distribución del portafolio

El Composable no realiza cálculos financieros.

---

## 2.2 Estados

Implementar:

```text
Loading
Content
Empty
Error
```

El estado vacío debe ser una experiencia diseñada explícitamente.

Ejemplo:

```text
NO HAY INVERSIONES

Registra tu primera inversión
para comenzar a analizar
tu patrimonio.

[ + AGREGAR INVERSIÓN ]
```

---

## 2.3 Inversiones

Implementar:

```text
InvestmentsScreen
InvestmentsViewModel
InvestmentsUiState
```

Mostrar inversiones agrupadas o filtradas.

Filtros:

```text
Todas
Renta variable
Renta fija
SOFIPO
Otros
```

Preparar filtros adicionales por:

- institución
- moneda

---

## 2.4 Detalle de inversión

Implementar:

```text
InvestmentDetailScreen
InvestmentDetailViewModel
InvestmentDetailUiState
```

Mostrar:

- instrumento
- institución
- valor actual
- capital invertido
- ganancia
- rendimiento
- historial
- movimientos

La pantalla recibe:

```text
investmentId
```

y obtiene el resto desde el dominio.

---

## 2.5 Formatters

Centralizar:

```text
MoneyFormatter
PercentageFormatter
DateFormatter
```

Nunca concatenar manualmente:

```text
"$" + amount
```

Preparar soporte inicial para:

```text
MXN
USD
```

y posibilidad futura de más monedas.

---

## Resultado esperado Fase 2

Flujo funcional:

```text
Dashboard
    │
    ▼
Inversiones
    │
    ▼
Detalle
```

Toda la información proviene del dominio.

Los valores hardcodeados del prototipo quedan únicamente en previews y tests.

---

# FASE 3 — Captura y movimientos

## Objetivo

Permitir modificar realmente el patrimonio desde la nueva interfaz.

---

## 3.1 Nueva inversión

Implementar:

```text
AddInvestmentScreen
AddInvestmentViewModel
AddInvestmentUiState
```

Tipos iniciales:

```text
Acción
ETF / Fondo
FIBRA
CETES
SOFIPO
Otro
```

Los campos deben poder variar según el tipo de instrumento.

---

## 3.2 Registrar movimiento

Implementar:

```text
AddTransactionScreen
AddTransactionViewModel
AddTransactionUiState
```

Tipos:

```text
Compra
Venta
Dividendo
Interés
Depósito
Retiro
Comisión
```

---

## 3.3 Persistencia

Flujo:

```text
Formulario
    │
    ▼
ViewModel
    │
    ▼
Use Case
    │
    ▼
Repository
    │
    ▼
Room
```

Después de registrar un movimiento:

```text
Room
 │
 ├──► inversión
 │
 ├──► Dashboard
 │
 ├──► patrimonio
 │
 └──► métricas
```

deben reaccionar automáticamente.

Preferir:

```text
Flow
StateFlow
```

sobre actualizaciones manuales entre pantallas.

---

## 3.4 Validaciones

Validar:

- campos obligatorios
- cantidades
- precios
- fechas
- moneda
- institución
- tipo de movimiento

La validación de dominio no debe depender de Compose.

---

## Resultado esperado Fase 3

Debe funcionar el flujo:

```text
Agregar inversión
       │
       ▼
Registrar compra
       │
       ▼
      Room
       │
       ▼
Dashboard actualizado
```

En este punto el prototipo deja de ser únicamente visual.

---

# FASE 4 — Patrimonio y Analytics

## Objetivo

Transformar las inversiones y movimientos en información útil sobre el patrimonio.

---

## 4.1 Pantalla Patrimonio

Implementar:

```text
PortfolioScreen
PortfolioViewModel
PortfolioUiState
```

Mostrar:

- patrimonio actual
- capital aportado
- ganancias
- rendimiento

---

## 4.2 Distribución

Calcular distribución por:

### Instrumento

```text
Renta variable
Renta fija
SOFIPO
Otros
```

### Institución

Ejemplo:

```text
GBM
CETES Directo
Nu
Klar
Otros
```

### Moneda

```text
MXN
USD
Otros
```

---

## 4.3 Visualizaciones

Implementar:

```text
AllocationBar
PortfolioChart
AssetMatrix
PerformanceChart
```

Mantener estética:

- geométrica
- monocromática
- compacta
- técnica

Usar naranja únicamente para información destacada.

---

## 4.4 Separar aportaciones de rendimiento

Distinguir:

```text
Patrimonio actual
Capital aportado
Ganancia / pérdida
Rendimiento
```

Una aportación no debe contabilizarse como rendimiento.

---

## Resultado esperado Fase 4

El usuario puede entender:

```text
¿Cuánto tengo?

¿Cuánto aporté?

¿Cuánto he ganado?

¿Dónde está mi dinero?

¿Cómo está distribuido?

¿Cómo ha evolucionado?
```

---

# FASE 5 — Proyección y Configuración

## Objetivo

Completar la experiencia financiera inicial conectando el patrimonio con el motor de proyección.

---

## 5.1 Proyección

Implementar:

```text
ProjectionScreen
ProjectionViewModel
ProjectionUiState
```

Inputs:

```text
Rendimiento esperado
Inflación estimada
ISR estimado
Horizonte
```

Inicialmente:

```text
Horizonte = 1 año
```

---

## 5.2 Resultados

Mostrar claramente:

```text
Valor actual

Valor nominal proyectado

Valor después de ISR

Valor real después de inflación
```

No mezclar:

```text
rendimiento nominal

rendimiento después de impuestos

rendimiento real
```

---

## 5.3 Escenarios

Preparar:

```text
Conservador
Base
Optimista
```

Los escenarios son parámetros del motor financiero.

La UI únicamente representa resultados.

---

## 5.4 Configuración

Implementar:

```text
SettingsScreen
```

Permitir configurar:

- moneda base
- inflación estimada
- ISR estimado
- preferencias generales

---

## 5.5 Instituciones

Implementar administración de instituciones.

Ejemplos:

```text
GBM
CETES Directo
Nu
Klar
Mercado Pago
Otra
```

No limitar el modelo a estas instituciones.

El usuario debe poder crear instituciones personalizadas.

Esto es importante para permitir que la aplicación evolucione posteriormente de una herramienta personal a un producto general.

---

# Resultado esperado Fase 5

Flujo completo:

```text
          INSTITUCIONES
                │
                ▼
       AGREGAR INVERSIÓN
                │
                ▼
      REGISTRAR MOVIMIENTO
                │
                ▼
              ROOM
                │
                ▼
            DOMINIO
                │
        ┌───────┼────────┐
        ▼       ▼        ▼
   DASHBOARD ACTIVOS PATRIMONIO
                         │
                         ▼
                     PROYECCIÓN
```

---

# Hardening Fases 1–5

Después de integrar las cinco fases realizar una pasada específica de calidad.

## Tests

Agregar o actualizar tests para:

- ViewModels
- Use Cases
- repositories
- mappers
- formatters
- cálculos financieros
- formularios
- estados vacíos
- navegación crítica

---

## Accesibilidad

Revisar:

- TalkBack
- content descriptions
- contraste
- tamaños mínimos táctiles
- escalado de texto
- estados seleccionados

---

## Rendimiento

Revisar:

- recomposiciones innecesarias
- listas grandes
- consultas Room
- Flows duplicados
- cálculos ejecutados desde UI

---

## Limpieza

Eliminar:

- datos demo fuera de previews/tests
- componentes antiguos sin uso
- navegación duplicada
- modelos temporales
- TODOs de integración
- código muerto

---

# Definition of Done global

Las fases 1–5 se consideran integradas cuando:

- [ ] El proyecto compila.
- [ ] Los tests existentes pasan.
- [ ] La nueva UI está integrada.
- [ ] Dashboard utiliza datos reales.
- [ ] Inversiones utiliza datos reales.
- [ ] Detalle utiliza datos reales.
- [ ] Se pueden crear inversiones.
- [ ] Se pueden registrar movimientos.
- [ ] Los cambios actualizan automáticamente el patrimonio.
- [ ] Patrimonio muestra agregaciones reales.
- [ ] Proyección utiliza el motor financiero.
- [ ] Inflación e ISR están separados del rendimiento nominal.
- [ ] Instituciones son configurables.
- [ ] No existen cifras financieras hardcodeadas en producción.
- [ ] Los Composables no contienen lógica financiera.
- [ ] Room no es accedido directamente desde UI.
- [ ] Loading, Empty y Error están implementados.
- [ ] Formato monetario está centralizado.
- [ ] Formato porcentual está centralizado.
- [ ] Navegación utiliza IDs.
- [ ] El sistema visual es consistente.
- [ ] La aplicación mantiene una base preparada para crecer más allá del uso personal.

---

# Orden recomendado para el agente

No implementar las cinco fases simultáneamente.

Ejecutar:

```text
FASE 1
  ↓
Compilar + Tests
  ↓
FASE 2
  ↓
Compilar + Tests
  ↓
FASE 3
  ↓
Compilar + Tests
  ↓
FASE 4
  ↓
Compilar + Tests
  ↓
FASE 5
  ↓
Compilar + Tests
  ↓
Hardening
```

Cada fase debe terminar con el proyecto en un estado ejecutable.

Nunca dejar la rama principal en un estado donde la siguiente fase sea necesaria para que la anterior compile.

---

# Siguiente etapa

Una vez completadas las fases 1–5, continuar con las fases posteriores del roadmap general de App Finanzas.

La arquitectura debe quedar preparada para incorporar progresivamente:

- métricas financieras avanzadas
- historial de patrimonio
- rendimiento diario
- benchmarks
- actualización de precios
- automatización de valuaciones
- múltiples monedas
- importación de movimientos
- fuentes externas
- sincronización
- múltiples portafolios
- múltiples usuarios
- monetización

La prioridad durante fases 1–5 es construir una base sólida.

Las capacidades avanzadas deben apoyarse sobre esta arquitectura y no introducirse prematuramente.
