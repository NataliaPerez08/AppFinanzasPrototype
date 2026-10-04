# PULSO — Android Adaptive Icon

Paquete listo para copiar a un proyecto Android.

## Incluye
- `values/colors.xml`: colores oficiales.
- `drawable/ic_launcher_background.xml`: fondo `#E4E3E1`.
- `drawable/ic_launcher_foreground.xml`: isotipo PULSO carbón + naranja.
- `drawable/ic_launcher_monochrome.xml`: máscara monocromática para themed icons (Android 13+).
- `mipmap-anydpi-v26/ic_launcher.xml`: Adaptive Icon.
- `mipmap-anydpi-v26/ic_launcher_round.xml`: alias compatible con launchers que solicitan icono redondo.
- `source-svg/`: SVG maestros del isotipo.

## Integración
Copia el contenido de `app/src/main/res/` sobre el directorio equivalente de tu aplicación.

En `AndroidManifest.xml`, la aplicación debe apuntar a:

```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    ... >
```

## Decisiones de diseño
El foreground usa un lienzo Android de 108 × 108 y coloca el isotipo dentro de una región central de 72 × 72. Esto deja margen para las máscaras del launcher (círculo, squircle, rounded square, etc.) y para el efecto de movimiento/parallax de Adaptive Icons.

El fondo se mantiene como capa independiente. No agregues esquinas redondeadas al foreground o background: Android aplica la máscara del launcher.

`monochrome` es intencionalmente blanco; el launcher utiliza la silueta como máscara y aplica el color del tema del usuario.

## Compatibilidad pre-Android 8
Los Adaptive Icons se usan desde API 26. Si el proyecto soporta API 25 o inferior, conserva/genera también los PNG legacy en `mipmap-mdpi`, `mipmap-hdpi`, `mipmap-xhdpi`, `mipmap-xxhdpi` y `mipmap-xxxhdpi`. Este paquete no sobrescribe esos archivos para evitar destruir recursos legacy existentes.
