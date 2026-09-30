# Hardening — App Finanzas

> Estado de cumplimiento verificado en `HARDENING_STATUS.md` (P0 completado,
> P1/P2 pendientes).

## Objetivo

Validar integralmente los flujos financieros y garantizar consistencia entre:

```text
Room → DAO → Repository → Flow → ViewModel → UI → métricas derivadas
```

No realizar refactors grandes salvo que sean necesarios para evitar estados financieros inconsistentes.

## Estado inicial auditado

Los siguientes flujos ya fueron verificados en dispositivo físico:

- Dashboard vacío.
- Crear inversión.
- Depósito inicial.
- Dashboard con datos reales.
- Registrar compra.
- Actualización reactiva mediante Room/Flow.
- Patrimonio y distribuciones.
- Proyección y escenarios.
- Configuración persistente.
- Instituciones.
- Agregar institución.
- Navegación principal y secundaria.

No deben romperse durante Hardening.

## Problemas P0 detectados

### 1. Operaciones incompletas

Actualmente no existen flujos completos para:

- Editar inversión.
- Eliminar inversión.
- Editar movimiento.
- Eliminar movimiento.
- Recalcular una inversión después de modificar su historial.

### 2. Persistencia no atómica

`AddTransactionUseCase` guarda el movimiento y después actualiza la inversión en operaciones separadas:

```text
saveTransaction()
updateInvestment()
```

Si la segunda operación falla, Room puede quedar inconsistente.

Debe existir una operación transaccional que guarde el movimiento y el snapshot derivado de la inversión en una única transacción Room.

### 3. Modelo financiero insuficiente

El modelo actual mezcla en `Investment`:

- Posición.
- Efectivo.
- Valor actual.
- Capital aportado.

La compra actual puede recalcular `currentValue` como `quantity × price`, provocando pérdidas artificiales cuando existe un depósito inicial. El modelo no separa explícitamente:

- efectivo disponible;
- cantidad de unidades;
- costo promedio;
- ganancia realizada;
- valor de mercado;
- capital aportado.

## Modelo financiero propuesto

Ampliar el estado derivado de `Investment` con los campos estrictamente necesarios:

```text
cashBalance
quantity
averageCost
currentPrice
currentValue
investedCapital
realizedProfit
returnPercentage
```

El historial de `Transaction` será la fuente para reconstruir el estado. Los campos derivados se almacenarán como snapshot para lectura eficiente, pero nunca serán la única fuente de verdad.

### Reglas de ledger

#### Depósito

```text
cashBalance += amount
investedCapital += amount
```

No modifica `quantity` ni genera rendimiento.

#### Compra

```text
cost = quantity × price + commission
cashBalance -= cost
quantity += quantity
averageCost = costo promedio ponderado
```

El valor de mercado se calcula con `quantity × currentPrice` más el efectivo disponible cuando corresponda al modelo de la inversión.

#### Venta

```text
quantity vendida <= quantity disponible
proceeds = quantity × price - commission
realizedProfit += proceeds - costo de las unidades vendidas
cashBalance += proceeds
quantity -= quantity vendida
```

Nunca se permite una cantidad negativa.

#### Retiro

```text
amount <= cashBalance
cashBalance -= amount
```

Nunca se permite efectivo negativo.

#### Comisión

La comisión reduce efectivo disponible y no crea rendimiento positivo.

#### Dividendo e interés

Incrementan efectivo disponible. No modifican la cantidad de unidades.

### Invariantes

```text
Patrimonio total = suma del patrimonio de inversiones
Distribución por institución = patrimonio total
Distribución por tipo = patrimonio total
Cantidad actual = compras - ventas
Efectivo disponible >= 0
Cantidad actual >= 0
```

Una aportación no debe registrarse como rendimiento.

## Cambios de persistencia

### Repository

Añadir operaciones para:

- actualizar inversión;
- eliminar inversión;
- actualizar movimiento;
- eliminar movimiento;
- aplicar una operación y recalcular el snapshot en una transacción atómica.

### DAO

Añadir:

- `@Update` para movimientos;
- `@Delete` para movimientos e inversiones;
- consultas por inversión ordenadas por fecha e id;
- operaciones necesarias para recalcular el ledger.

### Foreign keys

Mantener `CASCADE` entre inversión y movimientos. Verificar mediante tests que no existan registros huérfanos.

### Migrations

Incrementar la versión de Room y crear migration explícita para los nuevos campos. No usar `fallbackToDestructiveMigration`.

Validar:

```text
DB versión N + datos
→ migration
→ DB versión N+1 + mismos datos
```

## Flujos P0

### Editar inversión

```text
Detalle → Editar → Guardar → Room → Flow → UI
```

Verificar:

- nombre;
- institución;
- tipo cuando proceda;
- cancelar;
- persistencia después de reiniciar;
- no duplicar movimientos;
- no alterar importes históricos.

### Eliminar inversión

Verificar:

- inversión sin movimientos;
- inversión con movimientos;
- cancelar;
- confirmar;
- actualización inmediata;
- eliminación en cascada;
- ausencia de huérfanos.

### Editar y eliminar movimientos

Verificar depósitos, compras, ventas y retiros. Después de cada modificación deben actualizarse automáticamente:

- detalle;
- Dashboard;
- patrimonio;
- distribuciones;
- rendimiento;
- proyección.

### Ventas

Casos obligatorios:

- venta parcial: 100 → 60 unidades;
- venta total: 100 → 0 unidades;
- venta superior a la posición: rechazo sin modificar Room.

### Retiros

Casos obligatorios:

- retiro parcial;
- retiro total;
- retiro superior al disponible;
- cero;
- negativo.

## Validaciones

Rechazar antes de persistir:

- campos vacíos;
- cantidad cero o negativa;
- precio inválido;
- porcentaje inválido;
- venta superior a la posición;
- retiro superior al efectivo;
- valores extremadamente grandes;
- fechas futuras si la regla del producto las prohíbe.

Los errores deben conservar los datos válidos ya introducidos.

## Precisión monetaria

Auditar el uso actual de `Double` en dominio, Room y cálculos.

La implementación debe cubrir:

- `$0`;
- `$0.01`;
- centavos;
- valores grandes;
- operaciones acumuladas;
- rendimiento negativo;
- rendimiento cero;
- porcentajes decimales.

La estrategia monetaria no debe cambiar globalmente sin justificarlo. Si se usa `BigDecimal` para cálculos, debe evitarse volver a `Double` durante cálculos intermedios.

## Fechas

Probar:

- movimientos de hoy;
- históricos;
- múltiples movimientos el mismo día;
- movimientos insertados fuera de orden;
- fechas futuras según la regla vigente.

El resultado debe depender de la fecha real e id estable, no del orden accidental de inserción.

## Persistencia y ciclo de vida

Probar:

1. crear datos;
2. cerrar completamente la aplicación;
3. abrirla de nuevo;
4. verificar instituciones, inversiones, movimientos, configuración y proyecciones.

También probar recreación de Activity durante Dashboard, detalle y formularios. No deben duplicarse operaciones ni perderse datos parciales no guardados.

## Estados parciales

Verificar:

- sin instituciones;
- institución sin inversiones;
- inversión sin movimientos;
- depósito sin compra;
- posición cerrada;
- patrimonio insuficiente para proyectar.

No mostrar `NaN`, `Infinity`, porcentajes absurdos, gráficos rotos ni valores inventados.

## Volumen

Generar un escenario con:

- 20 instituciones;
- 100 inversiones;
- 1,000 movimientos.

Observar tiempo de apertura, consultas Room, recomposición, scroll y cálculos agregados. No optimizar sin evidencia de un problema.

## Pruebas

### Unit tests

- ledger;
- ventas y retiros;
- invariantes;
- precisión;
- proyecciones;
- validaciones;
- fechas.

### Repository/DAO tests

- inserts;
- updates;
- deletes;
- cascades;
- Flows;
- transacciones atómicas;
- migrations.

### ViewModel tests

Verificar `acción → estado esperado` y propagación reactiva.

### UI/device tests

Reservar para los flujos críticos:

- crear inversión;
- registrar compra;
- venta parcial/total;
- retiro;
- editar/eliminar;
- persistencia después de reinicio.

## Prioridades

### P0

- Ledger consistente.
- Venta.
- Retiro.
- Editar/eliminar movimientos.
- Editar/eliminar inversión.
- Operaciones atómicas Room.
- Precisión monetaria.
- Validación de operaciones imposibles.
- Persistencia real.

### P1

- E2E completo.
- Múltiples inversiones.
- Fechas.
- Estados parciales.
- Migrations.
- Back/cancelación.
- Recreación de Activity.

### P2

- Volumen.
- Rendimiento.
- Casos extremos adicionales.
- Optimización basada en mediciones.

## Regla de implementación

Antes de cada cambio:

1. inspeccionar la implementación actual;
2. identificar entidades, DAO, Repository, Use Case, ViewModel y pantalla;
3. determinar la fuente de verdad;
4. reproducir el problema con un test;
5. aplicar el cambio mínimo;
6. ejecutar tests unitarios, instrumentados y regresión del dispositivo.

No reemplazar código funcional por preferencia arquitectónica.

## Definition of Done

- Flujos P0 funcionales.
- Flujos existentes sin regresiones.
- Operaciones inválidas rechazadas antes de persistir.
- Sin posiciones negativas.
- Sin registros huérfanos.
- Dashboard y detalle con métricas coincidentes.
- Cambios Room reflejados por Flow.
- Datos persistentes después de cerrar la app.
- Fórmulas financieras probadas.
- Tests existentes y nuevos en verde.
- `./gradlew test` y tareas de build/lint disponibles ejecutadas.

## Entregable final

El informe final debe incluir:

- Implementado.
- Bugs encontrados y corregidos.
- Tests agregados.
- Cambios de arquitectura y justificación.
- Pendientes.
- Riesgos de datos, migrations, precisión, cálculos y concurrencia.
- Comandos ejecutados y resultados.
