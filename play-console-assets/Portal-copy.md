# AURA / Nimbo — Play Console Portal Copy

Written for direct copy-paste into Google Play Console. Items marked `NEEDS CONFIRMATION`
should be confirmed before final submission.

Official Google references:

- Create app setup: https://support.google.com/googleplay/android-developer/answer/9859152
- Prepare app for review: https://support.google.com/googleplay/android-developer/answer/9859455
- Preview asset requirements: https://support.google.com/googleplay/android-developer/answer/9866151
- Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
- Testing tracks: https://support.google.com/googleplay/android-developer/answer/9845334
- Upload key reset: https://support.google.com/googleplay/android-developer/answer/9842756

## Current Submission Stage

This is an **update to an already-published app** (not a first submission).

- App is live in production at versionCode 21 / 1.0.3.
- This update: versionCode **22** / versionName **1.0.4** ("Nimbo" rebuild + chat history).
- Signed AAB built and signed with the NEW upload key.
- One-time closed-testing / 12-tester requirement is already satisfied (app is in production).

Blocking item:

- Original upload key lost. New upload key generated (`AURA-UPL-2`). Must complete an
  **upload key reset** in App integrity before the new AAB is accepted.

## 1. App Identity (do NOT change)

App name (in-store):

```text
AURA
```

> Note: in-app UI brand is "Nimbo". The Play Store listing name can stay "AURA" or move to
> "Nimbo" / "Nimbo (formerly AURA)" — DECISION NEEDED. Package id must never change.

Package name:

```text
com.hawkfranklin.aura
```

Default language:

```text
English (United States) - en-US
```

## 2. Main Store Listing

App title (30 chars max):

```text
AURA: Private Local AI
```

Short description (80 chars max):

```text
Run open-source AI models locally. Private, offline, and ad-free.
```

Full description (4000 chars max):

```text
AURA (Android Utility for Runtime AI) runs open-source Small Language Models (SLMs)
locally on your Android device. No cloud, no account, no tracking on the default build.

Designed by HawkFranklin Research, AURA lets you chat, ask about images, and test
prompts entirely on-device. Once a model is downloaded, it works fully offline.

KEY FEATURES
- Pure local intelligence: models like Gemma 3, Qwen 2.5, and more.
- Privacy first: inference happens on your device; prompts never leave your phone.
- Offline capable: no internet needed after a model is downloaded.
- Chat history: your conversations are saved locally on the device.
- Ad-free: a pure tool, no ads, no distractions.
- Open source: inspect the code and verify the privacy claims.

SYSTEM REQUIREMENTS
- RAM: 8GB or higher strongly recommended (4-6GB devices may be slow or crash).
- Processor: modern Snapdragon 8 Gen 1+ / Tensor G2 or newer recommended.

Note: AURA is intended for research, learning, and personal productivity.
```

## 3. Graphics / Preview Assets

App icon:

```text
512 x 512 PNG. File: play-console-assets/app-icon-512/aura-icon-512.png
```

Feature graphic:

```text
1024 x 500 PNG/JPG (no alpha). TODO - not yet created.
Folder: play-console-assets/feature-graphic-1024x500/
```

Phone screenshots:

```text
At least 2 required. Current set in play-console-assets/screenshots-phone/:
01-welcome, 02-privacy-note, 03-home-onboarding, 04-model-picker,
05-chat, 06-ask-image, 07-labs.
```

## 4. Store Settings

App category:

```text
NEEDS CONFIRMATION. Suggested: Productivity or Tools.
```

Contact email:

```text
NEEDS CONFIRMATION (carry over the email used for the live 1.0.3 listing).
```

Website:

```text
NEEDS CONFIRMATION: https://hawkfranklin.in
```

## 5. Privacy Policy

Privacy policy URL (already used for live listing):

```text
https://hawkfranklin.in/products/aura-privacy.html
```

> A CAMERA permission requires a privacy policy URL. Confirm the page is still live.
> Draft copy is in play-console-assets/privacy-policy/PRIVACY_POLICY.md.

## 6. App Access / Sign-In Details

Does all or part of the app require login?

```text
No. AURA requires no account or sign-in. All features are available to reviewers
immediately after installing and downloading a model.
```

## 7. Ads Declaration

Does your app contain ads?

```text
No.
```

## 8. Data Safety

Does the app collect or share user data?

```text
NEEDS CONFIRMATION. The app does on-device inference only and does not send user
content to a server. HOWEVER, the release build includes Firebase Analytics, which
collects diagnostics/identifiers. Be consistent with the Advertising ID answer below.
Options:
  (a) Keep Firebase Analytics -> declare app activity / diagnostics collection + AD_ID = Yes.
  (b) Remove Firebase Analytics in a separate build -> declare "No data collected".
```

Is data encrypted in transit?

```text
Yes (for any analytics/model-download traffic).
```

Can users request data deletion?

```text
No account data is stored server-side. Chat history is stored locally and can be
cleared by the user on-device.
```

## 9. Content Rating

```text
Re-run only if prompted. Productivity/tools utility, no objectionable content.
```

## 10. Target Audience And Content

Target age:

```text
NEEDS CONFIRMATION. Suggested: 13+ / not designed for children.
```

## 11. Advertising ID

Does the app use Advertising ID?

```text
Yes - the release build includes Firebase Analytics, which pulls the
com.google.android.gms.permission.AD_ID permission. (Answer No only if Firebase
Analytics / the AD_ID permission is removed in a separate build.)
```

## 12. Permissions (release manifest)

```text
CAMERA                       - on-device image input (Ask Image).
RECORD_AUDIO                 - on-device voice input.
INTERNET / ACCESS_NETWORK_STATE - model downloads only.
FOREGROUND_SERVICE + FOREGROUND_SERVICE_DATA_SYNC - background model downloads.
POST_NOTIFICATIONS           - download progress notification.
WAKE_LOCK                    - keep download alive.
```

Foreground service declaration:

```text
Data sync -> Network processing (model downloads). Reuse the demo video provided
for the 1.0.3 submission unless the download flow changed.
```

## 13. Release Track + Versioning

```text
versionCode: 22
versionName: 1.0.4
Release name: 1.0.4 (Nimbo rebuild)
Track: Production (or Internal testing first, then promote).
AAB: android/app/build/outputs/bundle/release/app-release.aab
```

## 14. Signing / Upload Key Reset (DO THIS FIRST)

```text
The original upload key was lost. The new AAB is signed with a new key (AURA-UPL-2).
1. Play Console -> Test and release -> App integrity -> Upload key certificate
   -> Request upload key reset -> "I lost my upload key".
2. Upload ../upload_certificate.pem (repo root, gitignored).
3. Wait for Google approval (up to ~48h).
4. THEN upload the new AAB. Uploading before approval will be rejected for key mismatch.
```

## 15. Remaining Before This Update Ships

```text
1. Complete upload key reset and wait for approval.
2. Create feature graphic (1024 x 500).
3. Decide store name: keep "AURA" or move to "Nimbo".
4. Confirm data safety + Advertising ID answers match the Firebase decision.
5. Create new release on chosen track, upload AAB 22, add release notes, roll out.
```
