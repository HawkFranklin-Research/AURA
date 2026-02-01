# AURA
**Android Utility for Runtime AI**

AURA is a native Android app for running open-source Small Language Models (SLMs) locally on your phone. It is built for private, on-device research and internal use. Once models are downloaded, inference runs fully offline and the app stays ad-free.

Developed by **HawkFranklin Research**.

## Highlights
- **Pure local inference** using LiteRT-LM (no cloud required for chat).
- **Model hub + downloads** from Hugging Face.
- **Core tasks**: Chat, Ask Image, and Prompt Lab.
- **Open source & ad-free** by design.

## Models (examples)
AURA supports open-source SLMs such as:
- **Gemma 3**
- **Qwen 2.5**
- **DeepSeek R1 Distill**
- **Phi-4 Mini**

(Exact models can change over time based on the allowlist.)

## System Requirements
- **Android 12+** (minSdk 31)
- **RAM**: 8GB+ strongly recommended (4–6GB may be unstable)
- **Processor**: Modern Snapdragon 8 Gen 1+ / Tensor G2 or newer recommended

## Privacy & Offline Use
All inference runs on-device. Your prompts and data do not leave your phone. Internet is only needed to download models or access gated repositories, and after download the app works fully offline.

## Roadmap
- Chat with PDF & documents (RAG)
- Real-time multi-lingual translation
- Voice-to-action workflows
- Optional hovering assistant icon

## Developer Build
```bash
cd android
./gradlew assembleDebug
```
Output:
- `android/app/build/outputs/apk/debug/app-debug.apk`

## Play Store Prep (first-time)
If this is your first Play Store submission, you will need:
- App signing (keystore) and Play App Signing enrollment
- Store listing copy (title, short/full descriptions)
- Feature graphic, screenshots, and icon assets
- Privacy policy URL (even for no-data apps)
- Data Safety form answers
- Content rating questionnaire
- Target API compliance and permission justifications
- Internal/closed testing track setup before production

If you share the codebase, we can help generate the store listing text, privacy policy skeleton, and Data Safety answers needed for submission.
