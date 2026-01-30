# Conversion Plan — Port Google AI Edge Gallery (Android) into Aura repo

## Goal
Create a new native Android app based on Google AI Edge Gallery (Kotlin/Compose), copied into this repo, then trim features and re-skin. Keep the same model stack used by Google’s app (LiteRT / AI Edge). No UI changes done now — this document is the plan only.

## What we are comparing
- **Current Aura app:** React + Vite + Capacitor (web UI + Android shell)
- **Google sample:** Native Android **Kotlin + Jetpack Compose** app in `other-samples/gallery-1.0.9/Android/src`

These are different stacks. A direct “merge” is not realistic. The practical path is to **replace the Android project** with the Google sample and then customize it.

---

## Essential files & folders in Google app
Root: `other-samples/gallery-1.0.9/Android/src`

**Build system**
- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle/` (wrapper config)
- `gradlew`, `gradlew.bat`
- `gradle.properties`
- `app/build.gradle.kts` (Android app config)

**App source**
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/google/ai/edge/gallery/` (all Kotlin code)
- `app/src/main/res/` (icons, strings, themes, drawables)

**Config hooks (required to run)**
- `app/src/main/java/com/google/ai/edge/gallery/common/ProjectConfig.kt`
  - `clientId` (Hugging Face OAuth)
  - `redirectUri`
- `app/build.gradle.kts`
  - `manifestPlaceholders["appAuthRedirectScheme"]`

**Model allowlists**
- `other-samples/gallery-1.0.9/model_allowlist.json`
- `other-samples/gallery-1.0.9/model_allowlists/` (versioned)

---

## Proposed conversion approach (copy-first)
### Phase 1 — Create a new Android-native app base
1. **Create a new branch** (e.g., `native-rebuild`).
2. **Copy the Google Android project into Aura**:
   - Suggested target path: `android-native/` (keeps current Capacitor `android/` intact).
   - Copy from: `other-samples/gallery-1.0.9/Android/src/*` → `android-native/`
3. **Open Android Studio on `android-native/`** to verify it builds (after config below).

### Phase 2 — Make it runnable (no UI changes yet)
1. Create Hugging Face OAuth app.
2. Set values in:
   - `android-native/app/src/main/java/.../ProjectConfig.kt` (clientId, redirectUri)
   - `android-native/app/build.gradle.kts` (appAuthRedirectScheme)
3. Build debug APK using `./gradlew assembleDebug` in `android-native/`.

### Phase 3 — Identify what to keep vs remove
**Keep (core):**
- Model download + management flow
- Core LLM chat + inference + LiteRT stack
- Settings, model chooser, benchmarking (if useful)

**Remove (likely):**
- Tiny Garden mini‑game
- Mobile Actions demo
- Unneeded task cards / demos
- Extra demo prompts and showcase tiles

### Phase 4 — Rebrand & reskin (later)
- Update colors, fonts, app name, icons in `res/` and Compose themes
- Replace product strings, About text, and UI labels

---

## Mapping: Aura vs Google app (high-level)
| Area | Aura (current) | Google app (target) | Action |
|---|---|---|---|
| UI | React web | Compose native | Replace UI with Compose app |
| AI backend | Google GenAI cloud | LiteRT on-device | Keep Google LiteRT |
| Models | Gemini / Veo / TTS | LiteRT / HF downloads | Keep LiteRT models |
| Packaging | Capacitor Android shell | Full Android app | Switch to native app |

---

## Risks / Notes
- **Licensing:** Google app is Apache 2.0; check compliance when copying.
- **Min SDK:** Google app uses **minSdk=31** (Android 12+). If you need older devices, it will require work.
- **HF OAuth required:** Model download flow depends on OAuth.
- **Large codebase:** It’s substantial; trimming should be planned carefully.

---

## Next Steps (if you approve)
1) Create a new branch `native-rebuild`.
2) Copy Google Android project into `android-native/`.
3) Write a short checklist of which screens/features to keep.
4) Do a build once Hugging Face credentials are added.

---

## Open Questions for you
- Should we **replace** the current Capacitor app, or keep it alongside (e.g., `android/` + `android-native/`)?
- Which features from Google app do you want to keep/remove first?
- Do you want to keep the HF model download flow or use local/hosted models only?
