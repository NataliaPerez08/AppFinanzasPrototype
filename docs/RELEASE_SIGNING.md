# Release Signing — PULSO

La firma release no almacena secretos en Git.

## Configuración local

Copiar `keystore.properties.example` como `keystore.properties` y completar:

```properties
storeFile=/absolute/path/to/pulso-release.keystore
storePassword=...
keyAlias=pulso-release
keyPassword=...
```

`keystore.properties`, los keystores y los certificados están excluidos por
`.gitignore`.

## CI

Usar variables de entorno equivalentes:

```text
PULSO_KEYSTORE_FILE
PULSO_KEYSTORE_PASSWORD
PULSO_KEY_ALIAS
PULSO_KEY_PASSWORD
```

El keystore debe existir fuera del repositorio y respaldarse mediante el gestor
de secretos del CI. No imprimir sus valores en logs.

## Verificación

Con credenciales configuradas:

```bash
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
```

Sin credenciales, `assembleRelease` debe fallar indicando que falta `storeFile`;
esto evita generar accidentalmente un artefacto release no firmado con la clave
de producción.
