# AURA Pixel 7 device test report

**Date:** 2026-10-20
**Device:** Google Pixel 7 (`panther`), Android 14
**Connection:** ADB over Wi-Fi at `192.168.29.232:34841`
**Build tested:** locally built debug APK, installed as `com.hawkfranklin.aura.debug`

## OpenRouter authorization screen

The OpenRouter browser page displayed an **Authorization Request** for AURA. The screen stated:

- “An app requests access to your account”
- “You will be sent to `https://hawkfranklin.in`”
- The callback URL to recognize before proceeding was `https://hawkfranklin.in/aura/openrouter/callback`
- The optional key label was prefilled as `AURA`.
- The page warned that authorizing would let an external app create an API key and use account credits to make requests. The screen showed no expiration and no credit limit selected.

At capture time, the page still showed the **Authorize** button. This capture therefore documents the consent screen and intended redirect; it does not prove that this particular authorization was submitted or that AURA received a key. The screenshot contains no authorization code or `state` query value.

**Screenshot:** [OpenRouter authorization request (account avatars masked)](../device-shots/openrouter-pixel7-2026-10-20/10-openrouter-authorize/openrouter-authorization-redacted.png)
**Workspace path:** `/home/prime/Documents/github/andriod-apps/AURA/device-shots/openrouter-pixel7-2026-10-20/10-openrouter-authorize/openrouter-authorization-redacted.png`

## Device checks completed

- Connected to the Pixel 7 over ADB and confirmed the device was authorized.
- The normal in-place debug install failed because the installed Play Store app and debug APK use different signing certificates. The Play Store app was left installed with its data intact.
- Built a temporary side-by-side debug package and installed/launched `com.hawkfranklin.aura.debug`. The temporary Gradle change used to give the debug variant a package suffix was restored exactly afterward.
- Viewed the revised first-run onboarding disclosure.
- Viewed the combined on-device/OpenRouter model picker.
- Viewed the OpenRouter privacy and connection disclosure.
- Launched the OpenRouter browser authorization page.
- Checked the callback route registered by Android and the installed debug package’s App Links status.
- Checked the Android crash buffer. No AURA process crash was found. Two `uiautomator dump` attempts failed inside Android’s UI automation service; these were inspection-tool failures, not AURA crashes.
- Ran the debug unit-test suite with task reruns: **18 tests passed, 0 failures, 0 errors**.
- `git diff --check` passed.

Other captured screens are in [the Pixel 7 screenshot folder](../device-shots/openrouter-pixel7-2026-10-20/).

## Findings

Android reports `hawkfranklin.in: 1024` for the debug package’s domain verification state. The callback intent resolves to both AURA’s debug callback activity and Chrome, so Android has not established the verified App Link needed to ensure the HTTPS callback opens AURA. The Android documentation says a domain must report `verified`; values of 1024 or higher are verifier-specific errors. See [Verify Android App Links](https://developer.android.com/training/app-links/verify-applinks).

The repo’s [assetlinks example](openrouter/website/.well-known/assetlinks.json.example) contains a placeholder fingerprint and is not a deployed association file. The debug package fingerprint reported by Android was:

`5A:F1:58:10:3C:EC:47:4B:4B:55:9C:BC:C0:3D:BE:7A:04:A4:22:3B:96:B1:E5:33:F8:70:09:56:4B`

A release association must include the Play App Signing certificate fingerprint for the production package. A separate debug association is needed to test automatic callback handoff on this debug package.

## Not verified in this run

- The authorization approval and return from OpenRouter into AURA. The captured screen still had **Authorize** available; no authorization code, `state`, or API key was captured.
- Whether the credentials exchange succeeds, whether AURA stores the key, or whether the model catalog loads after authorization.
- An online streamed chat, Stop/Retry behavior, or online session persistence.
- Local inference. No local model was installed in the side-by-side debug package; downloading the 584 MB local chat model was not done.
- Callback behavior after the production website serves a valid `assetlinks.json` and Android verifies the matching signing certificate.

## Relevant screenshot paths

- First-run privacy onboarding: `device-shots/openrouter-pixel7-2026-10-20/02-onboarding/screen.png`
- Initial chat screen: `device-shots/openrouter-pixel7-2026-10-20/03-chat-initial/screen.png`
- Combined model picker: `device-shots/openrouter-pixel7-2026-10-20/04-model-picker/screen.png`
- OpenRouter connection disclosure: `device-shots/openrouter-pixel7-2026-10-20/07-connect-dialog/screen.png`
- OpenRouter authorization request: `device-shots/openrouter-pixel7-2026-10-20/10-openrouter-authorize/openrouter-authorization-redacted.png`

## Follow-up implementation handoff (not a new test run)

The original observations and screenshot above are preserved as historical evidence.
The owner subsequently requested migration of all active app/website references to
`hawkfranklin.eu`. This follow-up edits source and builds an APK only; no device,
authorization, chat, unit-test or lint execution is included in this follow-up.

New callback configuration:

- Production: `https://hawkfranklin.eu/aura/openrouter/callback`
- Side-by-side debug: `https://hawkfranklin.eu/aura/openrouter/debug/callback`
- Debug application ID: `com.hawkfranklin.aura.debug`; label: **AURA Debug**.
- Separate callback paths keep a production app from claiming the debug HTTPS return.
- A non-secret `app` query hint selects the correct package for the website's
  explicit Return to AURA button. Code/state remain subject to native PKCE checks.
- Old pending sign-ins are invalidated because their callback binding changed;
  cancel any already-open old browser consent and start a new authorization.

The full local debug signing SHA-256 was read from the signing certificate
(public certificate metadata, not a device test):

`5A:F1:58:10:3C:EC:47:4B:4B:55:9C:BC:C0:3D:BE:7A:04:A4:22:3B:96:B1:E5:33:F8:70:09:56:31:24:56:4B`

The originally reported fingerprint above omits `31:24:56` and is not a complete
32-byte SHA-256 fingerprint. The debug association artifact uses the full value.
Do not substitute this debug certificate for the production Play signing certificate.

Website deployment is still required. See
[EU callback handoff](openrouter-eu-callback-handoff.md). The code edits do not
establish that the EU domain is verified, the website was deployed, consent
completed, or a key exchange/chat succeeded. Those checks belong to the next agent.

## Follow-up device test after website deployment

**Date:** 2026-10-20
**Build:** `android/app/build/outputs/apk/debug/AURA-eu-callback-debug.apk`
**Package:** `com.hawkfranklin.aura.debug` (**AURA Debug**)
**Device:** Pixel 7, Android 14. Production Play Store package was not uninstalled or modified.

### Deployment and App Links

Fetched each deployed URL over HTTPS; all returned HTTP 200: production callback, debug callback, `callback.js`, `/.well-known/assetlinks.json`, and the AURA privacy page. The published Digital Asset Links file contains the debug package and its full SHA-256 certificate fingerprint. After `pm verify-app-links --re-verify`, Android reported `hawkfranklin.eu: verified` for `com.hawkfranklin.aura.debug`.

### Authorization

Closed the old pending browser tab, canceled its pending sign-in in AURA, and began a fresh login. The OpenRouter consent page showed the EU return host and exact debug callback `https://hawkfranklin.eu/aura/openrouter/debug/callback`, with key label `AURA`. It also stated that authorization creates an API key usable to make requests with account credits; the page showed no expiration and no credit limit. With the owner's explicit approval, tapped **Authorize**. Android automatically opened the HTTPS callback in AURA Debug; the app displayed **OpenRouter connected**. The short-lived code and API key were not copied into the report or logs.

A force-stop/relaunch preserved the connection state and online catalog; the manage-connection dialog offered **Reconnect in browser**. Online chat history entries remained on-device after restart and one failed chat was restored successfully. **New chat** returned to an empty online conversation.

### Online model checks and limitations

The model picker loaded the current OpenRouter free catalog, including Free Models Router and individual free entries such as Cohere North Mini Code. A request through each selected model failed with AURA's message: “No free endpoint is available with AURA's privacy restrictions.” The UI recorded “No answer received” / “Failed · partial response.” This indicates no currently eligible endpoint for the app's zero-data-retention requirement during this run; it does not establish a callback or authorization defect.

Because both requests failed before producing a usable stream, streaming output and **Stop** could not be verified. **Retry last request** was activated and produced the same no-eligible-endpoint message. The manual browser authorization-code fallback, Android Open by default guidance, and the website's explicit **Return to AURA Debug** button were not exercised; automatic verified App Link return succeeded, so no second API key was created. These remain unverified.

### Screenshots

Screens from this follow-up are saved in `device-shots/openrouter-pixel7-2026-10-20/22-fresh-auth/`, including `consent.png`, `automatic-return.png`, `model-picker.png`, `streaming.png`, `individual-chat.png`, `restart.png`, `history.png`, `history-restored.png`, and `new-chat.png`. The consent capture shows OpenRouter's account avatar and should be treated as private; it contains no callback code or API key.

No application source or build configuration was changed during this device test. No deployment files were changed.

## Retest with relaxed OpenRouter provider routing

**Date:** 2026-10-20
**APK installed in place:** `android/app/build/outputs/apk/debug/app-debug.apk`
**Package/version:** `com.hawkfranklin.aura.debug`, `1.0.6-debug` (version code 24)
**Device:** Pixel 7, Android 14. `adb install -r` succeeded. Both the production package and debug package remain installed.

The installed request policy in `OpenRouterRequestPolicy.kt` retains a zero `max_price` for prompt, completion, request, and image costs, and no longer sends the previous `zdr` or `data_collection` restrictions. No paid-price request was made.

The Free Models Router now returned a response, identifying `nvidia/nemotron-3-ultra-550b-a55b:free` as the selected model. An individual `cohere/north-mini-code:free` request streamed a long response. **Stop** canceled generation while retaining the partial text, and **Retry last request** started a new stream. **New chat** opened an empty conversation; the stopped chat was listed in local history and restored. After a force-stop/relaunch, the installed app remained usable with its online connection and the previously saved history. The test also confirms `adb install -r` preserved the existing debug app data.

The build and device test showed no AURA crash or UI error. The prompt was entered with underscores due to the Android test input method; this did not prevent the router response or stream controls from working. These checks are functional smoke tests on one device and provider availability may vary.

Screenshots for this pass are in `device-shots/openrouter-pixel7-2026-10-20/23-relaxed-routing/`, including `router-result.png`, `individual-streaming-progress.png`, `stopped.png`, `retry-in-progress.png`, `new-chat.png`, `history.png`, and `history-restored.png`.
