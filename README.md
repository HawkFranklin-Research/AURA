# Aura
**Abliterated Unfiltered RAW Artificial Intelligence**

Aura is a sophisticated personal research assistant developed by HawkFranklin Research. It is designed to be helpful, concise, and professional, capable of analyzing data, generating creative content, and assisting with complex tasks.

## Features
- **Voice Interaction:** Speak naturally to Aura and hear responses.
- **Multimodal capabilities:** Understands text and images.
- **Live Mode:** Real-time conversational interface.
- **Media Studio:** Tools for generating content.
- **Privacy Focused:** Your API key is stored locally on your device.

## Getting Started

1.  **Clone the repository**
2.  **Install dependencies:** `npm install`
3.  **Run development server:** `npm run dev`
4.  **Build for Android:** `npx cap sync android` then open in Android Studio.

## API Key
Aura requires a Google Gemini API key. You can enter this key in the App Settings. It is stored locally.

## Building the App

### Debug APK
Useful for testing on your own device.
1.  **Build web assets:** `npm run build`
2.  **Sync with Android:** `npx cap sync android`
3.  **Compile APK:**
    ```bash
    cd android && ./gradlew assembleDebug
    ```
    *Output:* `android/app/build/outputs/apk/debug/app-debug.apk`

### Release APK
Signed version for distribution (F-Droid, etc.).
1.  **Generate Keystore (First time only):**
    ```bash
    keytool -genkey -v -keystore android/keystore/release-key.keystore -alias aura-key -keyalg RSA -keysize 2048 -validity 10000
    ```
2.  **Configure Signing:** Ensure `android/keystore.properties` points to your keystore.
3.  **Compile Signed APK:**
    ```bash
    cd android && ./gradlew assembleRelease
    ```
    *Output:* `android/app/build/outputs/apk/release/app-release.apk`
