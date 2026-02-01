# Publish Work Plan (Signing + Release Bundle)

Planned actions (exact order):
1) Create Android upload keystore at `android/keystore/aura-upload.jks`.
2) Create `android/keystore.properties` with upload key alias + passwords + keystore path.
3) Update `android/app/build.gradle.kts` to load `keystore.properties`, define a `release` signingConfig, and use it for the release build.
4) Build a signed release bundle using `./gradlew clean bundleRelease`.
5) Report the output path of the signed `.aab`.

Notes:
- `android/keystore.properties` is gitignored; it will not be committed.
- Passwords will be stored only in `android/keystore.properties` locally.
