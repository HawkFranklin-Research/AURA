# AURA / Nimbo — Play Console Assets

This folder is the single staging area for Google Play Console submission materials
for the AURA app (user-facing brand: **Nimbo**, package `com.hawkfranklin.aura`).

## Folder Map

- `app-icon-512/`
  - Final Play Store app icon, 512 x 512 PNG. Current: `aura-icon-512.png`
    (generated from `android/app/src/main/res/drawable/logo_new_black.png`).
- `feature-graphic-1024x500/`
  - Final feature graphic, 1024 x 500 PNG/JPG (no alpha). **TODO — not yet created.**
- `screenshots-phone/`
  - Current Nimbo phone screenshots for the store listing.
- `screenshots-tablet/`
  - Optional tablet screenshots (add only if tablet is advertised).
- `privacy-policy/`
  - Privacy policy copy + published-URL notes.
- `release-notes/`
  - Release notes per track.
- `review-access/`
  - Reviewer access notes. (AURA needs no login — see file.)
- `Portal-copy.md`
  - Copy-paste answers for the Play Console forms.

## Current Stage (2026-06-27)

- App is **already live on Google Play** at versionCode 21 / 1.0.3 (production).
- This is the **first update** since launch: versionCode **22** / versionName **1.0.4**
  (the "Nimbo" rebuild + chat history).
- Signed release AAB built at `android/app/build/outputs/bundle/release/app-release.aab`.
- **Blocker:** original upload key was lost; a new upload key was generated and an
  **upload key reset** must be approved by Google before the new AAB is accepted.
  See `../SIGNING_KEY_INFO.md` (gitignored) for key details.

## Still TODO before upload

- Feature graphic (1024 x 500).
- Confirm store listing copy reflects "Nimbo" branding (see `Portal-copy.md`).
- Confirm data safety answers still match (Firebase Analytics → Advertising ID = Yes).
- Refresh screenshots if UI changed since capture.
