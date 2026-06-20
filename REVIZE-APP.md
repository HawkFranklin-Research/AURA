# REVIZE App Plan

This plan covers only internal Android app UI/product edits for the AURA app. It does not cover the website, Play Store listing, hosted model files, model conversion, or package identity.

## Non-Negotiables

- Keep Android package/application id unchanged: `com.hawkfranklin.aura`.
- Keep all existing model download URLs, model files, model ids, model allowlist structure, and model runtime behavior unchanged.
- Do not rename source packages or directories from `com.hawkfranklin.aura`.
- Do not edit website files or external marketing pages from this plan.
- Do not make claims that conflict with the app implementation. In particular, avoid hard claims like "no tracking" unless analytics is removed or disabled in a separate approved task.
- Keep current launcher icon assets unless a later explicit icon task is approved.

## Product Direction

Working brand direction for in-app UI: **Nimbo**.

Positioning:

- Primary idea: **The AI that does not need the internet.**
- Secondary idea: **Your AI, in your pocket, on or offline.**
- Tone: warm, direct, non-jargon, usable by Indian and global audiences.
- Avoid "research lab" and "experimental" language in normal user-facing entry points.

Continuity:

- For source code, package id, repository naming, and model repo naming, keep AURA.
- For user-visible app copy, use `Nimbo`.
- If continuity is needed in settings/about text, use a mild bridge such as "Nimbo, formerly AURA" only in an About surface, not on the home hero.

## Files To Change

### 1. Core User-Facing Strings

File:

- `android/app/src/main/res/values/strings.xml`

Planned edits:

- Change visible app name strings:
  - `app_name`: `AURA` -> `Nimbo`
  - `app_name_first_part`: `AURA` -> `Nimbo`
  - `app_name_second_part`: `PRIVATE LOCAL AI` -> likely remove from primary use or change to `POCKET OFFLINE AI`
- Change intro copy:
  - `app_intro`: `Private, on-device AI optimized for mobile research.` -> `Your private AI. On or offline.`
- Change category labels:
  - `category_llm`: `LLM` -> `Chat & Tools`
  - `category_experimental`: `Experimental` -> `Labs`
- Change model-list headers:
  - `model_list_recommended_models_title`: `Recommended models` -> `Pick your AI`
  - `model_list_imported_models_title`: `Imported models` -> `Your imports`
- Change footer/link copy:
  - `litert_community_label`: `Model Library` -> `Browse AI models`
- Change first-run/TOS copy:
  - `settings_dialog_tos_title`: `Research notice` -> `Privacy note`
  - `tos_dialog_title`: `Welcome to AURA` -> `Welcome to Nimbo`
  - `tos_dialog_view_accept_button_label`: `Accept & Continue` -> `Get started`
- Change task/chat copy:
  - `chat_llm_agent_name`: `LLM` -> `Nimbo`
  - `chat_generic_agent_name`: `Model` -> `Nimbo`
  - `text_input_placeholder_llm_chat`: `Type prompt…` -> `Ask anything…`
  - `text_image_generation_text_field_placeholder`: `Type prompt…` -> `Ask anything…`
  - `benchmark`: `Run benchmark` -> `Test speed on my phone`
- Change memory warning:
  - `memory_warning_title`: `Memory Warning` -> `Heads up`
  - `memory_warning_content`: soften to: `This model is large and may run slowly or crash on your phone. Want to start with a lighter one instead?`
  - `memory_warning_proceed_anyway`: `Proceed anyway` -> `Download anyway`
- Add new strings for onboarding and empty-state UI:
  - `first_model_title`: `Download your first AI`
  - `first_model_body`: `Start with a fast model that works on most phones. Download it once, then chat anywhere.`
  - `first_model_button`: `Download starter model`
  - `offline_badge`: `Offline ready`
  - `starter_prompt_summarize`: `Summarize this`
  - `starter_prompt_explain`: `Explain simply`
  - `starter_prompt_draft`: `Draft a message`
  - `starter_prompt_ideas`: `Give me ideas`

### 2. First-Run Welcome Dialog

File:

- `android/app/src/main/java/com/hawkfranklin/aura/ui/common/tos/TosDialog.kt`

Planned edits:

- Replace the current "Research Notice" first page with a friendly welcome:
  - Title: `Welcome to Nimbo`
  - Body: `A complete AI that lives on your phone. Download a model once, then use it anywhere, even in airplane mode.`
- Replace the second page with a plain privacy/offline note:
  - Title: `Private by design`
  - Body: `Your chats run on this device after a model is downloaded. No account is needed to start.`
- Remove or soften wording that says outputs may be "unsafe" in the first-run framing. Keep any legal/responsibility text for a later About/Settings policy screen if needed.
- Replace hardcoded page text with `stringResource(...)` entries from `strings.xml` where practical.
- Keep existing local preference behavior through `TosViewModel`; do not change persistence schema.

### 3. Home Screen Copy And Layout

File:

- `android/app/src/main/java/com/hawkfranklin/aura/ui/home/HomeScreen.kt`

Planned edits:

- Change `AppTitle()` from hardcoded:
  - `AURA\nLocal AI Research Lab`
  - to `Nimbo`
- Add a short supporting line under the title:
  - `The AI that doesn't need the internet.`
- Keep `IntroText()` but rewrite it around the new `app_intro` and link text.
- Rename visible task labels indirectly by changing task definitions in task module files listed below.
- Add a "download your first AI" onboarding card on home when no recommended chat model is downloaded.
  - It should appear after TOS acceptance and model allowlist loading.
  - It should recommend the smallest compatible chat model already in the allowlist, not add or modify models.
  - It should call the same existing download flow used by model cards, rather than creating a separate downloader.
  - It should route users toward Chat after download completes if that can be done without risky lifecycle changes.
- Keep category pager behavior.
- Keep current settings button and model import removal state.
- Keep Hugging Face model library link behavior, but use `Browse AI models` copy.

### 4. Task Names And Descriptions

Files:

- `android/app/src/main/java/com/hawkfranklin/aura/ui/llmchat/LlmChatTaskModule.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/llmsingleturn/LlmSingleTurnTaskModule.kt`

Planned edits:

- In `LlmChatTask`:
  - Keep label `Chat`.
  - Change description from `Chat with on-device models` to `Talk, ask, draft, summarize`.
- In `LlmAskImageTask`:
  - Change label from `Ask Image` to `Look`.
  - Change description from `Ask questions about images with on-device models` to `Ask about any photo`.
  - Keep task id `llm_ask_image`.
  - Keep model compatibility logic unchanged.
- In `LlmSingleTurnTask`:
  - Change label from `Prompt Lab` to `Labs`.
  - Change description from `Single-turn prompts with on-device models` to `Test prompts & settings`.
  - Keep task id `llm_prompt_lab`.

### 5. Model List And Model Manager Copy

Files:

- `android/app/src/main/java/com/hawkfranklin/aura/ui/modelmanager/ModelList.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/common/modelitem/ModelItem.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/common/modelitem/DownloadModelPanel.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/common/MemoryWarning.kt`

Planned edits:

- Change section title through `strings.xml`: `Recommended models` -> `Pick your AI`.
- Keep raw model names as they are for now. Do not edit `android/app/src/main/assets/model_allowlist.json` as part of this rebrand plan.
- Add or surface a short plain-English descriptor near each model where feasible without changing model metadata:
  - Example display layer only: `Fast & light. Great for most phones.`
  - Do not overwrite model descriptions in the allowlist.
- Update warning dialog copy and button labels through `strings.xml`.
- If `MemoryWarning.kt` currently only supports `Proceed anyway` / `Cancel`, update it to present:
  - Primary: `Use a lighter model`
  - Secondary: `Download anyway`
  - This may require callback naming cleanup in the component, but should keep download behavior intact.

### 6. Chat, Prompt, And Message Experience

Files:

- `android/app/src/main/java/com/hawkfranklin/aura/ui/common/chat/ChatPanel.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/common/chat/MessageInputText.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/common/chat/MessageBodyText.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/common/chat/MessageBodyLoading.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/common/chat/MessageBodyBenchmarkLlm.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/common/chat/BenchmarkConfigDialog.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/common/ModelPageAppBar.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/common/ModelPickerChip.kt`

Planned edits:

- Change assistant sender labels from `Model`/`LLM` to `Nimbo` through strings.
- Change input placeholder to `Ask anything...`.
- Keep voice dictation strings as-is:
  - `Hold to talk`
  - `Listening...`
  - `Release to send`
  - `Slide up to cancel`
- Rename benchmark action to `Test speed on my phone`.
- Add starter prompt chips on empty chat screens:
  - `Summarize this`
  - `Explain simply`
  - `Draft a message`
  - `Give me ideas`
- For Ask Image, use image-specific starter chips if practical:
  - `What is in this photo?`
  - `Read the text`
  - `Explain this image`
- Add a subtle offline-ready indicator near the model picker/header:
  - Text: `Offline ready`
  - Green dot or check, using existing Compose primitives.
  - Only show it after a model is downloaded/selected.
- Keep actual streaming/model execution logic unchanged.

### 7. Palette And Theme Refresh

Files:

- `android/app/src/main/java/com/hawkfranklin/aura/ui/theme/Color.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/theme/Theme.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/theme/ThemeSettings.kt`
- `android/app/src/main/java/com/hawkfranklin/aura/ui/home/SettingsDialog.kt`
- `android/app/src/main/proto/settings.proto`

Planned edits:

- Make the light-first Nimbo palette the primary brand palette:
  - Background: `#FAF8F5`
  - Surface/cards: `#FFFFFF`
  - Primary accent: `#0E7C66`
  - Secondary accent: `#FF6B4A`
  - Text: `#1A1A1A`
  - Muted text: `#6B7280`
  - User bubble: `#0E7C66`
  - AI bubble: `#F0EDE8`
- Add matching dark palette:
  - Background: `#0D0D0F`
  - Surface: `#1A1A1E`
  - Primary accent: `#2DD4A7`
  - Secondary: `#FF7A5C`
  - Text: `#F5F5F4`
  - Muted text: `#9CA3AF`
- Keep the existing purple AURA theme as an optional theme, but rename it in UI to `Cosmic`.
- Do not remove theme persistence. If `settings.proto` already has stable enum values for `THEME_AURA`, do not renumber existing enum values.
- If a new `THEME_NIMBO` enum is needed, append it only. Prefer reusing current light/dark schemes first to avoid a DataStore migration.
- Update settings copy in `SettingsDialog.kt` so theme names read naturally:
  - `System`
  - `Light`
  - `Dark`
  - `Cosmic`

### 8. Launcher Label, But Not Package Identity

Files:

- `android/app/src/main/res/values/strings.xml`
- `android/app/src/main/AndroidManifest.xml`

Planned edits:

- If the launcher label uses `@string/app_name`, changing `app_name` to `Nimbo` is enough.
- Confirm manifest package/application id remains `com.hawkfranklin.aura`.
- Do not change:
  - `namespace`
  - `applicationId`
  - Kotlin package declarations
  - Play Store package id

### 9. Settings And About Copy

Files:

- `android/app/src/main/java/com/hawkfranklin/aura/ui/home/SettingsDialog.kt`
- `android/app/src/main/res/values/strings.xml`

Planned edits:

- Update visible About/title copy to `Nimbo`.
- If continuity is useful, add one small line:
  - `Previously AURA. Built by HawkFranklin Research.`
- Do not add website or Play Store copy here.
- Do not imply no analytics/tracking unless Firebase Analytics is removed in a separate task.

## Files Explicitly Not To Change For This App-Only Pass

- `android/app/build.gradle.kts`
  - Keep package/application identity and dependencies unchanged unless a build fix is separately required.
- `android/app/src/main/assets/model_allowlist.json`
  - Keep current model entries, ids, file names, sizes, URLs, and task mappings unchanged.
- `model_allowlist.json`
- `model_allowlists/*`
- `PLAYSTORE_SUBMISSION.txt`
- `PLAY_CONSOLE_GUIDE.md`
- Any website repository or `aura2.html`.
- Any files under `/home/prime/Documents/github/AURA-models`.
- Launcher icon assets:
  - `android/app/src/main/res/mipmap-*`
  - `android/app/src/main/res/drawable/logo*.png`
  - These stay unchanged unless a separate icon-refresh task is approved.

## Suggested Implementation Order

1. Copy-only pass:
   - Update `strings.xml`.
   - Replace hardcoded copy in `TosDialog.kt`, `HomeScreen.kt`, and task modules.
   - Build and screenshot.

2. Theme pass:
   - Update `Color.kt` and `Theme.kt`.
   - Verify light, dark, and Cosmic themes.
   - Build and screenshot.

3. First-model onboarding pass:
   - Add the home onboarding card in `HomeScreen.kt`.
   - Reuse existing download flow.
   - Verify first-run behavior on a clean app install.

4. Chat polish pass:
   - Add starter prompt chips.
   - Add offline-ready indicator.
   - Rename benchmark UI.
   - Verify Chat, Look, and Labs task screens.

5. Final validation:
   - `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ANDROID_HOME=/home/prime/android-sdk ./gradlew clean assembleDebug`
   - Install with mobile MCP.
   - Capture screenshots for:
     - First-run welcome
     - Home
     - First-model onboarding card
     - Chat empty state with starter chips
     - Model picker/list
     - Look task
     - Labs task
     - Settings theme selector

## Open Questions Before Implementation

- Should the launcher-visible app name become `Nimbo` immediately, or should it be `Nimbo by AURA` for one transitional release?
- Should `Firebase Analytics` remain enabled? If yes, avoid "no tracking" copy. If no, remove it in a separate privacy/build task.
- Should the default theme be light-first Nimbo, system theme, or dark Nimbo?
- Should the old purple AURA theme remain named `AURA`, or should it be renamed `Cosmic` in UI while preserving enum/storage names internally?
- Should the first-model onboarding auto-start the download, or require an explicit tap?
- Should "Use a lighter model" in the memory warning navigate/select the smallest model automatically, or simply dismiss and point back to the model list?
