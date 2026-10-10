# PULSO — Guía para agente: recursos de Google Play Store

## Objetivo

Inspeccionar el repositorio Android de **PULSO: Patrimonio e inversiones** y preparar todos los recursos solicitados en la pantalla **Ficha de Play Store predeterminada** de Google Play Console (idioma `es-419`). Entregar archivos listos para cargar, textos revisados y un informe de verificación. No publicar ni subir recursos a Play Console automáticamente.

## Reglas obligatorias

1. Examina el repositorio antes de generar contenido: branding, ícono adaptativo, pantallas reales, navegación, funciones efectivamente implementadas, `applicationId`, variantes de compilación y soporte de dispositivos.
2. Reutiliza el isotipo y colores existentes. No hay archivos de fuente propios en el proyecto: la app usa monospace del sistema (`FontFamily.Monospace` en `ui/theme/Type.kt`); tómalo como referencia tipográfica. Identidad: estética tecnológica, sobria y editorial; carbón `#111110`, naranja `#FF641C`, fondos cálidos `#F4F3F1` y `#E4E3E1`. No inventes una nueva marca.
3. Nunca captures saldos, nombres, cuentas, movimientos o información personal reales. Crea datos de demostración claramente ficticios, coherentes entre pantallas, y aislados de producción. No uses capturas de datos financieros del desarrollador.
4. Las capturas deben representar pantallas y funciones **reales** de la aplicación. Se permiten marcos, titulares y composición promocional, pero no fabricar funcionalidades, ganancias garantizadas, integraciones bancarias ni resultados de inversión.
5. No alteres la lógica de negocio para obtener recursos. Cualquier fixture, script o configuración de captura debe ser reproducible y no quedar incluido en builds de producción. Nunca subas secretos, keystores o datos personales a Git.
6. Trabaja de manera autónoma cuando sea seguro. Si falta un emulador, herramienta, fuente o recurso, informa el bloqueo y entrega lo que sí puedas generar. No declares completado un archivo que no existe.
7. No publiques ni cambies la configuración de distribución, anuncios o monetización sin autorización.

## Entregables y especificaciones

| Recurso | Obligación mostrada | Especificación | Entrega |
|---|---|---|---|
| Nombre | Obligatorio | Máximo 30 caracteres | `listing/es-419.md` |
| Descripción breve | Obligatorio | Máximo 80 caracteres | `listing/es-419.md` |
| Descripción completa | Obligatorio | Máximo 4,000 caracteres | `listing/es-419.md` |
| Ícono de Play Store | Obligatorio | PNG o JPEG, exactamente 512 × 512 px, menos de 1 MB | `graphics/icon-512.png` |
| Gráfico de funciones | Obligatorio | PNG o JPEG, exactamente 1024 × 500 px, máximo 15 MB | `graphics/feature-1024x500.png` |
| Capturas de teléfono | Obligatorio | Entre 2 y 8, PNG/JPEG, menos de 8 MB cada una, 9:16 o 16:9, lados entre 320 y 3840 px | `screenshots/phone/` |
| Capturas tablet 7 pulgadas | Marcadas con asterisco en el formulario | Hasta 8, PNG/JPEG, menos de 8 MB, 9:16 o 16:9, lados entre 320 y 3840 px | `screenshots/tablet-7/` |
| Capturas tablet 10 pulgadas | Marcadas con asterisco en el formulario | Hasta 8, PNG/JPEG, menos de 8 MB, 9:16 o 16:9, lados entre 1080 y 7680 px | `screenshots/tablet-10/` |
| Capturas computadora | Sin asterisco en el formulario | Entre 4 y 8 si se incluyen, PNG/JPEG, menos de 8 MB, 9:16 o 16:9, lados entre 1080 y 7680 px | `screenshots/desktop/` |
| Video | Opcional | URL de YouTube público o no listado, sin restricción de edad y sin anuncios | Registrar estado en informe; no inventar URL |

**Importante:** Las secciones de tablet aparecen marcadas en la pantalla, pero el agente debe verificar su obligatoriedad efectiva según los dispositivos compatibles y los mensajes de validación de Play Console. No generar capturas de tablet simuladas a partir de capturas de teléfono ni afirmar que se probó un dispositivo no disponible. Android XR figura como sección sin especificaciones desplegadas: inspeccionar solo si el proyecto admite XR y documentar cualquier requisito adicional sin inventarlo.

**Promoción destacada:** Preparar preferentemente **4 a 6 capturas de teléfono**. La pantalla indica que, para participar en promoción, se requieren al menos cuatro capturas con un mínimo de 1080 px por lado. Seleccionar, si es viable, resolución **1440 × 2560 px** para 9:16.

## Fase 1 — Auditoría del proyecto

- Identifica pantallas reales y estado funcional: dashboard, cuentas/inversiones, patrimonio, rendimientos, proyecciones y configuración.
- Identifica recursos vectoriales, adaptive icon, logos, fuentes, assets de marca y su licencia.
- Revisa compatibilidad de tablets, Chromebook, orientación y tamaños de pantalla.
- Comprueba si hay una forma segura de iniciar la app con datos ficticios (debug flavor, fixtures locales, base de datos de prueba, etc.).
- Documenta qué afirmaciones publicitarias son demostrables en el código y cuáles no.
- Entrega una matriz `función → pantalla → evidencia → recurso promocional`.

## Fase 2 — Textos de la ficha (`es-419`)

Redacta tres textos finales, contando caracteres automáticamente:

- **Nombre:** `PULSO: Patrimonio e inversiones` tiene 31 caracteres y excede el límite de 30; proponer directamente una versión más corta que conserve PULSO.
- **Descripción breve:** explicar con precisión la utilidad principal de gestionar y visualizar patrimonio e inversiones, sin afirmar sincronización bancaria si no existe.
- **Descripción completa:** explicar funciones verificadas: registro y seguimiento de inversiones, visualización patrimonial, rendimiento, escenarios y proyecciones solo si están implementadas. Explicar que las estimaciones son orientativas y no garantizan resultados. Mencionar almacenamiento local únicamente si se confirma en el código.

Evitar testimonios inventados, rankings, superlativos no verificables, palabras clave repetitivas, emojis promocionales excesivos y promesas financieras. Validar contra las políticas de metadatos de Google Play.

## Fase 3 — Ícono de Play Store

- Localiza el isotipo oficial del proyecto; la única fuente vectorial es el drawable XML `ic_launcher_foreground.xml` (no existe SVG). Renderiza el PNG desde ese vector.
- Exporta un PNG RGB/RGBA de **512 × 512**, menos de 1 MB.
- Respeta la composición de marca y los márgenes de seguridad para el recorte visual de Google Play.
- Verifica legibilidad a 48, 96 y 192 px.
- No confundas el ícono de la ficha con los recursos `mipmap` del adaptive icon de Android: son entregables distintos.

## Fase 4 — Gráfico de funciones

Crear **1024 × 500 px**, PNG o JPEG, máximo 15 MB, con composición limpia y contraste accesible. Concepto sugerido: isotipo PULSO + una visualización coherente con la interfaz + mensaje breve `Tu patrimonio, bajo control`. Evita saturación, afirmaciones engañosas, premios, precios y referencias a Google Play no autorizadas. Si el gráfico incluye UI, usa una representación fiel de la aplicación.

## Fase 5 — Capturas reales

### Escenario ficticio

Preparar una cartera de demostración desde cero: el proyecto no tiene fixtures ni debug flavor, y `SeedData` solo siembra instituciones con nombres reales (GBM, BBVA, NU, CETES Directo, Mercado Pago). Usar importes, instituciones genéricas y fechas coherentes; no usar datos reales. Verificar que la suma de inversiones concilie con el patrimonio presentado y que los rendimientos y proyecciones no contradigan los valores de origen. Aislar el escenario en la base de datos del emulador o dispositivo de prueba, nunca en builds de producción.

### Secuencia recomendada para teléfono

1. `01-dashboard.png`: panorama general del patrimonio — «Tu patrimonio en un solo lugar».
2. `02-inversiones.png`: lista o detalle de inversiones — «Organiza tus inversiones».
3. `03-rendimientos.png`: métricas o evolución — «Entiende tus rendimientos».
4. `04-proyecciones.png`: proyecciones reales de la app — «Explora escenarios futuros».
5. `05-distribucion.png` (si existe): distribución por institución o activo.
6. `06-historial.png` (si existe): evolución o fotografías patrimoniales históricas.

Solo incluir capturas 5 y 6 si las pantallas existen y están suficientemente terminadas. Sustituir cualquier pantalla inexistente por otra funcionalidad real. Capturar con emulador o dispositivo mediante herramientas Android (por ejemplo `adb exec-out screencap -p`), ocultar notificaciones personales y asegurar que la barra de estado, navegación, textos y gráficos no se superpongan. Mantener una composición visual consistente, sin recortar cifras importantes.

### Tablets y computadora

- Comprobar si la app admite cada categoría y si Play Console exige recursos.
- Si aplica, ejecutar en emuladores o dispositivos con tamaños adecuados, capturar la **interfaz real** y exportar con los límites indicados.
- No estirar capturas de teléfono para aparentar un layout de tablet o escritorio.
- Si no se puede probar, dejar el directorio sin entregables finales y documentar `PENDIENTE` con causa exacta.

## Fase 6 — Validación automática

Crear un script local reproducible (Python con Pillow u otra dependencia ya disponible) que:

1. Inspeccione todos los archivos de salida y verifique formato real, resolución, peso y relación de aspecto.
2. Verifique mínimos y máximos de cantidad de capturas por categoría aplicable.
3. Cuente caracteres Unicode de nombre y descripciones.
4. Detecte nombres duplicados, imágenes vacías o ilegibles y archivos faltantes.
5. Genere `validation-report.md` con tabla de resultados `PASS / FAIL / PENDIENTE`.
6. No marque como `PASS` la calidad editorial, cumplimiento de políticas ni fidelidad funcional sin revisión manual; incluya un checklist separado para esos puntos.

No hacer upscale artificial para cumplir dimensiones. Si falla una validación, corregir el archivo y repetirla.

## Fase 7 — Estructura de entrega

Crear una carpeta de exportación fuera del árbol versionado, o añadir `play-store-assets/` a `.gitignore` antes de generarla (hoy no está ignorada):

```text
play-store-assets/
├── listing/
│   └── es-419.md
├── graphics/
│   ├── icon-512.png
│   └── feature-1024x500.png
├── screenshots/
│   ├── phone/
│   ├── tablet-7/
│   ├── tablet-10/
│   └── desktop/
├── scripts/
│   └── validate_assets.py
├── asset-inventory.md
├── validation-report.md
└── README.md
```

`README.md` debe indicar cómo reproducir los recursos, qué se debe subir a cada campo de Play Console, el orden de las capturas, el estado de cada categoría y cualquier dependencia. Conservar archivos editables de origen cuando existan (SVG, diseños, scripts), sin incluir material sensible.

## Criterios de aceptación

- Todos los campos obligatorios de texto están redactados y dentro de límites.
- El ícono y el gráfico destacado existen y cumplen las especificaciones.
- Existen al menos dos capturas válidas de teléfono; objetivo recomendado: cuatro o más.
- Cada captura refleja una pantalla auténtica y datos exclusivamente ficticios.
- Se documentó la necesidad o no de recursos de tablet, escritorio y XR; los exigidos para la distribución seleccionada se entregaron o quedaron explícitamente bloqueados.
- Se ejecutó el validador y se guardó su resultado.
- Se revisó `git status` para asegurar que no se incorporaron secretos ni fixtures financieros personales.
- No se alteró el comportamiento de producción ni se publicó nada automáticamente.

## Informe final del agente

Responder con:

1. Ruta absoluta del paquete generado.
2. Tabla de archivos generados y resultado de validación.
3. Nombre y descripciones finales, con sus conteos de caracteres.
4. Funciones reales utilizadas como evidencia en cada captura.
5. Recursos pendientes, bloqueos y pasos manuales en Play Console.
6. Confirmación de que no se utilizaron datos reales ni se realizó una publicación.

**Orden de ejecución:** auditar → preparar datos demo seguros → redactar textos → generar gráficos → capturar pantallas reales → validar → entregar informe. No dar por terminada la tarea mientras falten recursos obligatorios sin un bloqueo documentado.
