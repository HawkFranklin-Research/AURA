# AURA v1.0.7 device regression retest

**Date:** 2026-10-20
**Device:** Google Pixel 7 (Android 14), connected by Wi-Fi ADB
**Build:** `android/app/build/outputs/apk/debug/app-debug.apk`
**Installed package:** `com.hawkfranklin.aura.debug`, version `1.0.7-debug`, version code 25
**Install method:** `adb install -r` (update in place; no data clear or uninstall)
**Production package:** `com.hawkfranklin.aura` remained installed and untouched.

## Checks that passed

- Debug app installed, launched, and showed the AURA chat screen without an app crash.
- Existing on-device `AURA Chat 1B` was ready. A local prompt received a response, and the conversation restored from history.
- Chat history search matched title, model, and visible message text; case-insensitive matching worked, a no-result state appeared for an unmatched query, and clearing the query restored the list.
- Rename allowed a valid title, rejected a blank title by disabling Save, and capped an overlength entry at 80 characters. A renamed local chat kept its title after force-stop/relaunch. An existing online chat was renamed and its custom title appeared in history.
- Local Markdown export opened Android's document picker and saved successfully. The pulled `.md` file includes title, local mode, model, conversation text, and Unicode emoji. The exported copy is at `local-export.md`.
- OpenRouter connection remained active after an app force-stop/relaunch: the free-model catalog loaded without reauthentication.
- In a fresh online chat, Free Models Router answered and identified `poolside/laguna-s-2.1:free` as the provider model.
- The individual `cohere/north-mini-code:free` model also returned an answer.
- The picker displayed both local downloads and individual online free models; the online disclosure remained visible in chat.
- Both production and debug package names were present on the device after testing.

## Not fully exercised in this pass

- **Continue response / truncation:** no response was deliberately driven to the output cap, so truncation detection and Continue response remain unverified on-device.
- **Context budgeting:** full-history retention under a near-limit prompt and the oversized-prompt recovery behavior were not exercised.
- **Streaming controls:** this pass confirmed online responses but did not retest Stop during a live stream or Retry after a failed/stopped stream. The previous v1.0.6 device pass did exercise streaming, Stop, and Retry; that is supporting historical evidence, not a v1.0.7 result.
- **Online Markdown export:** only local Markdown export was completed in this pass.
- **Delete-one/delete-all, export surviving deletion, cancel picker, and unavailable export destination:** not exercised. Existing conversations were preserved; no delete-all action was attempted.
- **Model download/resume:** the installed Chat 1B model was available and inference worked; a new download or interrupted/resumed download was not tested.
- **Vision/audio, Labs, keyboard/accessibility while generating:** not exercised.
- **Play internal-track upgrade and production signing/App Links:** not exercised. The installed build was the debug APK, not a Play-distributed signed build.

## Result

No AURA app defect was observed in the exercised v1.0.7 paths. The tested local and online chat flows, search, rename, export, and persisted OpenRouter connection worked. This is a device smoke/regression pass, not a complete release sign-off; items above still need targeted verification. No application code was changed.
