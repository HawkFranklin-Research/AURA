# Google Play Publishing Notes (AURA)

This document summarizes what we learned and the concrete steps for publishing AURA on Google Play, based on our current setup. It is written so any future app can follow the same checklist.

## 1) Release artifact requirements
- **Play Console requires a signed release AAB** (debug-signed bundles are rejected).
- Output (after release build):
  - `android/app/build/outputs/bundle/release/app-release.aab`

## 2) Signing (upload key + Play App Signing)
- Use **Play App Signing** (recommended). Do **not** change signing key after first upload.
- Create upload keystore and configure release signing via `android/keystore.properties`.
- Ensure `release` build uses the release signing config (not debug).
- **Do not commit** `android/keystore.properties` (already gitignored).

## 3) Versioning
- Play Console rejects reused **versionCode**.
- **Increment** `versionCode` for every upload (ex: 20 → 21).
- Update in: `android/app/build.gradle.kts`.

## 4) Required privacy URL
- Because the app uses **CAMERA**, a privacy policy URL is required.
- Current URL used in app + Play Console:
  - `https://hawkfranklin.in/products/aura-privacy.html`

## 5) Data Safety declaration
- If all inference is on-device and no data is transmitted off-device, declare:
  - **No data collected**
  - **No data shared**
- Be consistent with Firebase/SDK usage (see AD ID section).

## 6) Advertising ID (AD_ID) declaration (blocker)
- If any SDK pulls `com.google.android.gms.permission.AD_ID` (often Firebase Analytics), you must answer **Yes**.
- If you want to answer **No**, remove the permission by:
  - Removing Firebase Analytics, OR
  - Removing the permission explicitly in manifest with `tools:node="remove"`.

## 7) Foreground service declaration (Android 14+)
- App uses **FOREGROUND_SERVICE_DATA_SYNC** for **model downloads**.
- In Play Console:
  - Select **Data sync → Network processing**
  - Provide a short **demo video** link (YouTube unlisted is fine).
- Required video content:
  1) Start model download
  2) Show foreground notification with progress
  3) Move app to background and show download continues

## 8) Warnings in Play Console
- **No deobfuscation file**: expected if `isMinifyEnabled = false` (warning only).
- **No native debug symbols**: warning only.
- These are not blockers for closed testing, but recommended for production.

## 9) Closed testing requirement (new personal accounts)
- If account created after **Nov 13, 2023**:
  - Must run **Closed Test** with **≥12 testers**
  - Testers must be opted in for **14 continuous days**
  - Only then you can apply for Production access

## 10) Store listing assets
Required:
- **App icon**: 512×512 PNG/JPG
- **Feature graphic**: 1024×500 PNG/JPG
- **Phone screenshots**: 2–8 (16:9 or 9:16, min 1080px on each side for promo eligibility)

### Generated assets (current paths)
- Feature graphic (AURA):
  - ` /home/vatsal1/Documents/g2/hawkfranklin.github.io/products/assets/generated/aura-feature-1024x500.png `
- App icon (AURA):
  - ` /home/vatsal1/Documents/g2/hawkfranklin.github.io/assets/generated/aura-app-icon-512x512.png `

### How to resize icons (generic)
Use Python + PIL to generate a clean 512×512 icon with a white background:
```
python - <<'PY'
from PIL import Image
from pathlib import Path

src = Path("/path/to/logo.png")
dst = Path("/path/to/output-icon-512x512.png")

img = Image.open(src)
if img.mode != "RGBA":
    img = img.convert("RGBA")
img.thumbnail((512, 512), Image.LANCZOS)
bg = Image.new("RGBA", (512, 512), (255, 255, 255, 255))
x = (512 - img.width) // 2
y = (512 - img.height) // 2
bg.paste(img, (x, y), img)
bg.convert("RGB").save(dst, format="PNG")
print("saved", dst)
PY
```

### How to generate feature graphics (generic)
Option A: Create a small HTML layout and screenshot it at 1024×500:
```
/usr/bin/google-chrome --headless --disable-gpu --hide-scrollbars \
  --window-size=1024,500 --force-device-scale-factor=1 \
  --screenshot=/path/to/feature-1024x500.png \
  file:///path/to/feature.html
```

Option B: Use a design tool (Figma, Sketch, Canva) at 1024×500 and export PNG/JPG.

### How to generate developer header (generic)
The Play Console developer profile header must be 4096×2304. You can use the same HTML + headless Chrome approach:
```
/usr/bin/google-chrome --headless --disable-gpu --hide-scrollbars \
  --window-size=4096,2304 --force-device-scale-factor=1 \
  --screenshot=/path/to/dev-header-4096x2304.png \
  file:///path/to/dev-header.html
```
Then convert to JPG if needed:
```
python - <<'PY'
from PIL import Image
Image.open("/path/to/dev-header-4096x2304.png").convert("RGB") \
  .save("/path/to/dev-header-4096x2304.jpg", quality=90, optimize=True, progressive=True)
PY
```

## 11) Suggested store listing text (short version)
- **App name**: AURA
- **Short description**: "Private, local AI on your phone — offline, ad‑free, open source."
- **Full description**: Use the longer draft in `PLAYSTORE_SUBMISSION.txt` or expand as needed.

## 12) Release naming guidance
Use clear internal names like:
- `v1.0.3-closed-1`
- `1.0.3 (first closed test)`

## 13) Build commands (signed release bundle)
From `android/`:
```
./gradlew clean bundleRelease
```

## 14) Troubleshooting
- If Play Console says “debug signed”: release build is still using debug keystore.
- If bundle rejected for versionCode: increment versionCode and rebuild.
- If AD_ID declaration conflict: remove the permission or answer Yes.
