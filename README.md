# Sistema Android — ClientApp + AdminApp

Dos aplicaciones Android independientes con Kotlin, Jetpack Compose, MVVM y Room Database.

---

## Estructura del proyecto

```
/ClientApp          → App cliente (detección de red real)
/AdminApp           → App administrador (CRUD Room Database)
```

---

## ClientApp

### Qué hace
- Detecta la red activa usando `ConnectivityManager` y `NetworkCapabilities` (APIs reales de Android)
- Muestra el SSID real de la red Wi-Fi si el sistema lo permite
- Si Android restringe el acceso (Android 10+ sin permiso de ubicación exacta), muestra el mensaje real del sistema
- Detecta si hay Internet real mediante `NET_CAPABILITY_VALIDATED`
- Detecta red móvil (celular)
- Sin datos hardcodeados — todo proviene de las APIs del sistema

### Permisos requeridos (AndroidManifest)
```xml
ACCESS_NETWORK_STATE
ACCESS_WIFI_STATE
ACCESS_FINE_LOCATION     ← necesario para leer SSID en Android 10+
ACCESS_COARSE_LOCATION
INTERNET
```

---

## AdminApp

### Qué hace
- CRUD completo sobre Room Database (SQLite local)
- Base de datos inicia **completamente vacía** — sin seed data
- Lista de registros con estado vacío real si no hay datos
- Formulario de creación y edición con validación real
- Confirmación de eliminación antes de borrar
- Navegación entre pantallas con Navigation Compose

---

## Cómo abrir en Android Studio

### Opción A — Importar directamente

1. Clonar o descargar este repositorio
2. Abrir **Android Studio** → `File → Open`
3. Seleccionar la carpeta `/ClientApp` o `/AdminApp` (una a la vez — son proyectos independientes)
4. Esperar a que Gradle sincronice las dependencias (requiere internet la primera vez)
5. Conectar dispositivo Android o iniciar emulador
6. Pulsar ▶ **Run**

### Opción B — Importar desde GitHub

```bash
# Subir este proyecto a GitHub (solo necesitas la carpeta del proyecto)
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/TU_USUARIO/android-system.git
git push -u origin main
```

Luego en Android Studio: `File → New → Project from Version Control → GitHub`

---

## Compilar APK de debug

### En Android Studio
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```
El APK se genera en:
```
ClientApp/app/build/outputs/apk/debug/app-debug.apk
AdminApp/app/build/outputs/apk/debug/app-debug.apk
```

### Desde línea de comandos (macOS/Linux)
```bash
cd ClientApp
chmod +x gradlew
./gradlew assembleDebug

cd ../AdminApp
./gradlew assembleDebug
```

### Desde línea de comandos (Windows)
```cmd
cd ClientApp
gradlew.bat assembleDebug

cd ..\AdminApp
gradlew.bat assembleDebug
```

---

## Compilar APK de release (firmado)

```bash
# 1. Generar keystore (solo una vez)
keytool -genkey -v -keystore release-key.jks \
    -alias mykey \
    -keyalg RSA -keysize 2048 \
    -validity 10000

# 2. Compilar release
./gradlew assembleRelease \
    -Pandroid.injected.signing.store.file=release-key.jks \
    -Pandroid.injected.signing.store.password=TU_PASS \
    -Pandroid.injected.signing.key.alias=mykey \
    -Pandroid.injected.signing.key.password=TU_PASS
```

---

## GitHub Actions — CI/CD automático

Crea el archivo `.github/workflows/android.yml` en la raíz del repositorio:

```yaml
name: Build Android APKs

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build-clientapp:
    name: Build ClientApp
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Build ClientApp debug APK
        working-directory: ClientApp
        run: ./gradlew assembleDebug

      - name: Upload ClientApp APK
        uses: actions/upload-artifact@v4
        with:
          name: clientapp-debug
          path: ClientApp/app/build/outputs/apk/debug/app-debug.apk

  build-adminapp:
    name: Build AdminApp
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Build AdminApp debug APK
        working-directory: AdminApp
        run: ./gradlew assembleDebug

      - name: Upload AdminApp APK
        uses: actions/upload-artifact@v4
        with:
          name: adminapp-debug
          path: AdminApp/app/build/outputs/apk/debug/app-debug.apk
```

Los APKs compilados aparecerán en la pestaña **Actions → Artifacts** de GitHub.

---

## Requisitos mínimos

| Herramienta | Versión mínima |
|------------|----------------|
| Android Studio | Ladybug (2024.2) o superior |
| JDK | 17 |
| Android SDK | API 26 (Android 8.0) |
| Gradle | 8.7 (descargado automáticamente) |
| AGP | 8.5.2 |

---

## Notas de comportamiento real

### ClientApp — SSID en Android 10+
Android requiere `ACCESS_FINE_LOCATION` para leer el SSID de la red Wi-Fi.
Si el usuario deniega el permiso, la app muestra:
> "No se puede verificar el hotspot por restricciones del sistema. Concede el permiso de Ubicación para ver el SSID."

Esto es comportamiento real del sistema, no un error de la app.

### AdminApp — Base de datos vacía
La app **no precarga ningún dato**. Al abrir por primera vez verás la pantalla de estado vacío real.
Esto es correcto según los requisitos del sistema.
