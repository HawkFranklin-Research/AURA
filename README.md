# Aura
**Abliterated Unfiltered RAW Artificial Intelligence**

Aura is a native Android app based on Google AI Edge Gallery (Kotlin + Jetpack Compose). It runs on-device models via LiteRT (Google AI Edge) and supports local model download and offline inference.

## Highlights
- **On‑device inference:** No cloud API keys required for local models.
- **Model manager:** Download and manage LiteRT models.
- **Chat + tools:** Multi‑turn chat and demo tasks (can be trimmed later).
- **Offline‑ready:** Runs without network once models are downloaded.

## Requirements
- **Android 12+** (minSdk 31)
- **Android Studio** (or Gradle CLI)
- **JDK 11**

## Configure Hugging Face OAuth (required for model downloads)
Create a Hugging Face OAuth app and set these values:

1) `android/app/src/main/java/com/google/ai/edge/gallery/common/ProjectConfig.kt`
- `clientId`
- `redirectUri`

2) `android/app/build.gradle.kts`
- `manifestPlaceholders["appAuthRedirectScheme"]`

These must match the redirect URI you configured in Hugging Face.

## Build (Android Studio)
- Open the `android/` folder in Android Studio.
- Sync Gradle.
- Build → Build APK(s).

## Build (CLI)
```bash
cd android
./gradlew assembleDebug
```
Output:
- `android/app/build/outputs/apk/debug/app-debug.apk`

## Notes
- The current Android app in `android/` is the Google AI Edge Gallery base. You can trim demos and re‑skin later.
- The previous Capacitor Android project was archived to `android-capacitor-old.tar.gz`.
