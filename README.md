# AURA
**Abliterated Uncensored RAW Artificial Intelligence**

AURA is a native Android app for on‑device AI. It runs LiteRT‑LM models locally, supports offline use, and is designed for research‑grade experimentation with fewer restrictions than typical consumer assistants.

## Why AURA
AURA is meant for serious, privacy‑first experimentation where cloud AI isn’t practical or desired. Example use cases:
- **Red‑team prompt testing** to see how models behave under edge or adversarial inputs.
- **Offline field work** where network access is limited or risky.
- **Sensitive data exploration** (notes, transcripts, logs) that you don’t want to upload.
- **Research & evaluation** of model behavior across different on‑device sizes and quantizations.
- **Creative brainstorming** when you want less constrained generation.

> Note: AURA is not fully uncensored today. The intent is to allow **mid‑way** constraints now, and expand options later.

## What it does
- **On‑device inference** (LiteRT‑LM / Google AI Edge).
- **Model hub + downloads** for local models.
- **Chat, Ask Image, Prompt Lab** as core tasks.
- **Offline‑ready** once models are downloaded.

## Privacy & Responsibility
AURA keeps inference on device by default. You are responsible for what you run and the outputs you generate.

## Status
- **Android 12+** (minSdk 31)
- **Models download from Hugging Face** (may require a token for gated models)

## Developer build (short)
```bash
cd android
./gradlew assembleDebug
```
Output:
- `android/app/build/outputs/apk/debug/app-debug.apk`

## Repo notes
- The app is based on Google AI Edge Gallery and has been trimmed and re‑branded for AURA.
- The old Capacitor build is archived as `android-capacitor-old.tar.gz`.
