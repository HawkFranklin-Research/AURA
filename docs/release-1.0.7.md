# AURA v1.0.7 (25) release candidate

Production package: `com.hawkfranklin.aura`. Debug package:
`com.hawkfranklin.aura.debug`, version `1.0.7-debug` (25).

This candidate includes the previously tested optional OpenRouter integration
plus the following conversation improvements. Play Store submission and rollout
must follow the new device retest; prior v1.0.6-debug results do not validate this build.

## Changes

- Removed the stale ZDR footer and listing claim. Online chat remains limited to
  free targets with zero price ceilings. Provider retention policies vary.
- Updated both repository privacy copies and the deployed website source,
  including Android document exports and local context trimming.
- Preserve `length` finish reasons as `truncated`; expose a user-triggered Continue
  response action. Content-filtered responses have a separate visible status.
- Bound online requests using catalog context/completion limits, a conservative
  UTF-8 estimate, output reservation, and 512 tokens of headroom. Unknown/router
  context falls back to 8,192; app context is capped at 32,768 and output at 4,096.
  Older whole turns are omitted with a notice; saved history is unchanged. This
  estimate does not guarantee every upstream tokenizer/router accepts the request.
- Shared history drawer searches titles/models/visible messages on device,
  renames conversations, and exports Markdown using Android's document picker.
  Export omits attachments, credentials and hidden reasoning/usage metadata.
  Choosing cloud storage sends the export to that selected provider.
- Append `custom_title` at proto field 9. Atomic saves preserve user titles without
  changing legacy field numbers or moving chats between local/online modes.

## Device retest handoff

Install the newly built debug APK in place using `adb install -r`; preserve the
production Play app, downloaded models and all saved conversations. Do not
uninstall either app or clear data. Record the APK checksum and installed version.
Never put authorization codes, state values or keys in screenshots or logs.

1. Open an existing local chat, then an online chat. Search by title, model name
   and a word within a message. Check case-insensitive matches, no-result state,
   cleared search, and that searching does not initiate network requests.
2. Rename local and online chats; reopen them, send a message, force-stop/relaunch,
   and confirm both names persist. Check blank names and the 80-character limit.
3. Export each mode to Markdown through Android's picker. Verify visible text,
   Unicode, partial-response labels, attachment omission, and absence of private
   file paths/hidden metadata. Cancel the picker and test an unavailable destination;
   the app must remain usable. Verify exported copies survive chat deletion.
4. Ask for a sufficiently long answer to reach a provider output limit. Verify
   the persisted truncated label and Continue response action; continue, restart,
   and restore history. If the provider completes early, record this scenario as
   unverified instead of claiming it passed. Do not weaken free-only policy to test.
5. Build a long conversation and verify the older-context notice, full history
   retention, and a working follow-up. Try an oversized prompt: it should show a
   clear error and preserve its text in history instead of silently cutting it.
6. Regress online routing, streamed text, Stop, Retry, new chat, provider/model
   isolation, delete-one/delete-all, airplane mode, reconnect, and restart.
7. Regress local download/resume, chat streaming, stop/new chat, history restoration,
   local image/audio tasks and Labs. Check keyboard/layout, accessibility labels,
   and history actions while a generation is active.
8. For release validation, install through Play internal testing, upgrade from
   the published production build without data loss, and verify production App
   Links with the Play App Signing certificate. Debug verification is insufficient.

Record screenshots and results separately, with pass/fail/not-tested per scenario.
Do not perform production rollout during this test pass.

## Release gates

- Device retest above and a Play-signed internal-test upgrade.
- Verify the production App Links association on the Play-signed internal build.
  The production certificate was extracted from the installed Play app (installer
  `com.android.vending`) and verified with apksigner; the matching association
  is deployed separately. Upload-key signatures cannot substitute for it.
- Play Console Data Safety, privacy URL, store copy/screenshots, reviewer access,
  and AI-content safeguard/reporting review, matching actual app behavior.
- Upload the signed `android/app/build/outputs/bundle/release/app-release.aab`
  only after the release gates pass. Never upload the debug APK to production.

## Automated validation

- `assembleDebug`: successful; debug APK version `1.0.7-debug` (25).
- `testDebugUnitTest`: 27 tests, 0 failures/errors/skips.
- `lintDebug`: 0 errors, 179 warnings and 4 informational findings.
- Signed `bundleRelease`: successful using the existing upload keystore from the
  other local AURA checkout via an untracked Gradle initialization override.
  This checkout's ignored signing properties still point to missing `dummy.jks`;
  configure valid local signing properties before rebuilding without the override.
- `jarsigner -verify`: the release AAB signature verified.
- `bundletool validate`: passed. Release manifest confirms production package,
  version 1.0.7/code 25, EU production callback, and analytics deactivation.
- Build ran offline after the license plugin stalled on network POM retrieval.
  Offline OSS generation emitted missing-POM warnings; bundled license text and
  metadata are present. Review these dependency notices before store submission.
- The website privacy deployment succeeded and live Markdown returned HTTP 200
  with the revised provider-retention and export disclosures.
- No device installation, UI exercise, screenshot or live inference was performed
  by this implementation agent. All such validation belongs to the testing agent.

Debug APK SHA-256:
`2694ceab74b5c6c8120a6414002222487308e889847c81e885c6b1fcde772beb`

Release AAB SHA-256:
`dab03bd0e8af3c019b187e1814e08a830309f057e6ed5c86395d333f8c01a1c7`

Artifacts are local build outputs, excluded from Git; signing secrets and raw
device screenshots are not included in this release commit.

## Production certificate provenance

The phone was used only for read-only package metadata and copying its installed
production base APK for local certificate verification. This was not a device
functional test: no installation, app launch, consent, or screenshot was performed.
`pm list packages -i` identified the installer as `com.android.vending`;
`apksigner verify --print-certs` verified the APK and its app signer:

```
A0:91:7F:94:F0:B4:00:41:80:D1:08:A3:25:6E:8C:B1:12:82:4E:39:DB:92:52:A0:69:82:9D:CE:A6:B6:38:CF
```

The app signer (not Google's source-stamp certificate) is in the production
association artifact. Existing debug association remains for the testing agent.
If Play signing-key rotation applies to other Android versions, verify/add those
additional Play certificates from Play Console before rollout.
