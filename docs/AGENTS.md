# AGENTS.md — PULSO

## Objetivo

Integrar el prototipo visual neo-brutalista de PULSO (Patrimonio e inversiones) en el proyecto Android existente, respetando la arquitectura y funcionalidad construidas durante las fases 1–5.

El prototipo `AppFinanzasPrototype` es una **referencia de UI/UX**, no una nueva arquitectura ni una nueva fuente de datos.

No reconstruir la aplicación desde cero.

## Principio fundamental

Separar estrictamente:

UI → ViewModel → Use Cases → Repository → Data Sources

La UI nunca debe contener lógica financiera, acceso directo a Room, llamadas HTTP ni cálculos de negocio.

Los valores actualmente hardcodeados en el prototipo deben desaparecer progresivamente y ser sustituidos por estados provenientes de ViewModels.

---

# Stack

- Kotlin
- Jetpack Compose
- Material 3 cuando sea útil como infraestructura
- Navigation Compose
- ViewModel
- StateFlow
- Coroutines
- Room
- Repository Pattern
- Use Cases / Domain Layer
- Arquitectura existente del proyecto

No introducir frameworks adicionales salvo que exista una justificación clara.

---

# Sistema visual

Mantener el lenguaje visual del prototipo.

## Colores

Background principal:

`#E4E3E1`

Background secundario:

`#F4F3F1`

Divisores:

`#C6C5C2`

Texto:

`#111110`

Accent:

`#FF641C`

El naranja se reserva para:

- CTA
- selección
- navegación activa
- métricas destacadas
- estados relevantes
- series principales de gráficas

No usar gradientes.

Evitar sombras.

Evitar tarjetas flotantes.

Evitar redondeos excesivos.

Priorizar paneles planos, líneas, retículas y separación tipográfica.

---

# Tipografía

El diseño utiliza dos familias conceptuales.

## Display

Para:

- títulos
- patrimonio
- KPIs principales
- cifras destacadas

Debe sentirse:

- geométrica
- tecnológica
- extended
- modular

## Mono

Para:

- precios
- porcentajes
- fechas
- labels
- filtros
- tablas
- navegación
- información financiera

Si las fuentes exactas del prototipo no están disponibles, utilizar fuentes compatibles sin añadir dependencias innecesarias.

---

# Componentes

No duplicar UI entre pantallas.

Crear componentes reutilizables.

Como mínimo:

`FinanceTopBar`

`FinanceBottomNavigation`

`FinancePanel`

`MetricCard`

`PrimaryMetric`

`SectionHeader`

`SegmentedFilter`

`AssetRow`

`InstitutionRow`

`FinanceButton`

`FinanceTextField`

`PercentageMetric`

`MoneyMetric`

`PortfolioChart`

`AllocationBar`

`ProjectionChart`

Los componentes deben ser independientes de la fuente de datos.

---

# Formato monetario

Nunca concatenar manualmente símbolos y cantidades.

Centralizar:

`MoneyFormatter`

Debe soportar inicialmente:

- MXN
- USD

Preparar la arquitectura para soportar otras monedas posteriormente.

Ejemplo:

`1_245_320.45`

→

`$1,245,320.45 MXN`

---

# Porcentajes

Centralizar formato en:

`PercentageFormatter`

Debe distinguir entre:

- rendimiento positivo
- rendimiento negativo
- cero

La lógica de presentación no debe modificar el cálculo financiero original.

---

# Navegación

Navegación inferior principal:

Inicio

Inversiones

Patrimonio

Proyección

Más

Rutas adicionales:

InvestmentDetail/{investmentId}

AddInvestment

AddTransaction/{investmentId}

Institutions

Settings

Nunca pasar objetos completos mediante navegación.

Pasar IDs.

---

# Pantallas objetivo

## Dashboard

Debe consumir datos reales del dominio.

Mostrar:

- patrimonio actual
- variación diaria
- capital aportado
- ganancias
- rendimiento
- evolución histórica
- distribución del portafolio

No calcular patrimonio dentro del Composable.

---

## Inversiones

Mostrar inversiones reales.

Permitir filtros por:

- tipo
- institución
- moneda

Cada elemento debe navegar mediante:

`investmentId`

---

## Detalle de inversión

Mostrar:

- instrumento
- institución
- valor actual
- capital invertido
- ganancia
- rendimiento
- historial
- movimientos

La pantalla recibe únicamente el ID.

---

## Nueva inversión

Debe usar el modelo de dominio existente.

Tipos iniciales:

- acción
- ETF / fondo
- FIBRA
- CETES
- SOFIPO
- otro

Los campos pueden variar según el tipo.

No crear modelos paralelos exclusivamente para la UI si ya existe una representación adecuada en dominio.

---

## Registrar movimiento

Tipos:

- compra
- venta
- dividendo
- interés
- depósito
- retiro
- comisión

Validar los datos antes de persistir.

Después de guardar:

actualizar inversión → actualizar patrimonio → actualizar métricas.

---

## Patrimonio

Consumir las métricas agregadas del dominio.

Mostrar:

- patrimonio
- capital aportado
- ganancias
- distribución por tipo
- distribución por institución
- distribución por moneda

---

## Proyección

La UI no implementa fórmulas.

Debe consumir el motor de proyección existente.

Inputs:

- rendimiento esperado
- inflación estimada
- ISR estimado

Outputs:

- valor nominal
- valor después de ISR
- valor real
- escenarios

Mantener diferenciados:

rendimiento nominal ≠ rendimiento después de impuestos ≠ rendimiento real.

---

## Configuración

Permitir administrar:

- instituciones
- moneda base
- inflación estimada
- ISR estimado
- preferencias

---

# Estado UI

Cada pantalla debe exponer un único `UiState`.

Ejemplo conceptual:

DashboardUiState

con:

- isLoading
- portfolioValue
- dailyChange
- investedCapital
- profit
- performance
- allocation
- history
- error

Evitar múltiples estados independientes difíciles de sincronizar.

---

# Estados obligatorios

Cada pantalla basada en datos debe contemplar:

Loading

Content

Empty

Error

No asumir que siempre existen inversiones.

El estado vacío es parte del producto, no un caso excepcional.

---

# Datos demo

Los datos hardcodeados del prototipo solo pueden utilizarse en:

- @Preview
- FakeRepository
- fixtures de tests

Nunca como fuente de datos de producción.

---

# Testing

La integración no debe romper las pruebas existentes.

Agregar tests para:

- ViewModels
- mappers UI
- formatters
- navegación crítica
- formularios
- estados vacíos

Las fórmulas financieras deben probarse fuera de Compose.

---

# Restricciones

NO:

- reescribir las capas data/domain existentes sin necesidad
- mover lógica financiera a Composables
- crear una segunda base de datos
- duplicar entidades Room
- duplicar repositories
- introducir navegación paralela
- mantener cifras financieras hardcodeadas
- cambiar fórmulas para hacer coincidir el mockup

El mockup debe adaptarse a los datos reales.

Los datos reales nunca deben adaptarse artificialmente al mockup.

---

# Estrategia de integración

Trabajar incrementalmente.

Cada cambio debe:

1. compilar
2. mantener tests existentes
3. introducir una parte pequeña de UI
4. conectar esa UI con arquitectura existente
5. eliminar datos mock correspondientes
6. añadir pruebas cuando corresponda

Evitar un único commit que sustituya toda la aplicación.

---

# Definition of Done

Una pantalla está integrada cuando:

- reproduce razonablemente el diseño del prototipo
- obtiene información mediante ViewModel
- ViewModel utiliza dominio/use cases
- no contiene datos financieros hardcodeados
- soporta Loading
- soporta Empty
- soporta Error
- navegación funciona
- TalkBack dispone de descripciones donde corresponde
- números y monedas usan formatters centralizados
- tests relevantes pasan
- proyecto compila

Si alguna de estas condiciones no se cumple, la pantalla no está terminada.
