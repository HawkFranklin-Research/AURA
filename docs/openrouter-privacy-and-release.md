# OpenRouter privacy and release checks

The current candidate is v1.0.7 (25). Historical validation notes below describe
earlier snapshots; see release-1.0.7.md for current build results and retest gates.
A Play Console submission has not been performed.

## Privacy behavior

- Online mode is optional and disclosed before browser sign-in. Existing local
  users are not forced to create an account.
- Text messages and current online context reach OpenRouter and an inference
  provider. HawkFranklin does not proxy chat traffic.
- Every online generation enforces strict zero-price limits ($0 for prompt,
  completion, request, and images) and rejects fallback to paid models.
- Inference is performed by third-party providers whose individual logging,
  caching, and retention policies apply. OpenRouter account-level content
  logging/product-use settings also apply; request metadata is retained by OpenRouter.
- The connection sheet links to OpenRouter privacy, collection and provider pages.
- Local credential deletion is not remote key revocation. Local chat deletion is
  not a third-party data deletion request.
- Keys/pending PKCE use AES-GCM and Android Keystore in noBackupFilesDir.
  Chat history/attachments and HF credential files are excluded from backup and
  device transfer by the current rules. Earlier backups are not erased.
- Firebase Analytics collection is deactivated in the application manifest.
  Existing local prompt-history/token-prefix debug logging has been removed.
- No automatic image/audio uploads or tools/search plugins are added.

## Before production

- Deploy/verify HTTPS callback, association file and published privacy text.
- Inspect merged **release** manifest for analytics deactivation, backup rules and
  exact callback route. Verify no SDK/configuration re-enables content logging.
- Complete owner-driven browser authorization on cold/warm app launches, cancelled
  consent, duplicate callbacks, expired codes, process death and disconnected keys.
- Exercise a real free online response, Stop, retry, airplane-mode failure and
  handling when no free endpoint is available.
- Upgrade from v1.0.6 with local histories and downloaded models. Verify download,
  restart/reopen, local streaming, same-model session restoration, image/audio and Labs.
- Verify chat history stays isolated between providers and sessions, including
  delete-one/delete-all and stop-then-new-chat behavior.
- Review content safeguards and in-app reporting under Google Play AI-content
  policy. AURA remains responsible for its app experience; OpenRouter is not a
  substitute for this review. If a reporting endpoint is needed, define its
  destination, submission consent and data retention before enabling it.
- Reconcile Play Console Data Safety answers with online transmission and the
  merged release SDK configuration. Do not reuse the old local-only answers.
- Update listing/screenshots and reviewer instructions. Explain that online
  connection is optional and requires the reviewer's own OpenRouter account.
- Assign a new version code/name, build the signed bundle, and use internal testing
  before a production rollout. The candidate uses versionCode 25.

## Limits of verification in this workspace

JVM tests cover PKCE vectors/callback rejection, free/privacy policy enforcement,
catalog filtering, SSE framing/error handling and reasoning preservation. They do
not establish Android Keystore, browser App Links, Play upgrade, UI layout or live
provider availability. Record device results separately; do not label static/unit
checks as live authentication or inference validation.

## Workspace validation — October 20, 2026

Completed command:

```bash
./android/gradlew -p android --offline :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Result: build successful; 18 JVM tests passed; lint reported 0 errors and 179
warnings (including existing dependency/deprecation findings). `git diff --check`
passed and both Markdown privacy-policy copies match. A public, unauthenticated
models-catalog request confirmed the response schema; no account key or inference
request was used. No Android device was connected, so device/browser/Keystore and
live chat checks remain unverified. No website deployment, version bump, signed
release, commit, push or store submission was performed.

## EU-domain follow-up

The earlier workspace validation above predates the EU callback changes. The
owner reserved all follow-up tests for another agent. See
[the EU callback handoff](openrouter-eu-callback-handoff.md) and the addendum in
[the device report](device-testing-report.md). This follow-up performs a debug
build only; it does not rerun or claim updated unit, lint, device or live results.
