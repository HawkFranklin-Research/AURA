# EU callback fix — build and testing-agent handoff

The owner requested source edits and a debug APK, explicitly reserving all tests
for another agent. This change includes no device interaction, browser consent,
unit-test, lint, live endpoint check or website deployment.

## What changed

- Active URLs now use `hawkfranklin.eu`: callback, Android host filter, privacy/help
  links, OpenRouter app attribution, policy contact links and deployment documents.
- Production keeps `/aura/openrouter/callback`. Debug uses
  `/aura/openrouter/debug/callback` so the production app cannot consume its return.
- Debug now permanently uses `com.hawkfranklin.aura.debug` and label **AURA Debug**.
  It can update the prior side-by-side test installation with the same debug key,
  while leaving the Play installation and its data alone.
- AURA displays the return website before sign-in, reads Android's link-handling
  state, and offers a button to the app's Open by default settings when needed.
- The callback web page offers an explicit package-targeted Return to AURA button
  when HTTPS stays in Chrome. It forwards only the one-time code/state to the
  correct package's callback activity. Only the app exchanges the code for a key.
- The app still enforces the exact callback shape, pending state, PKCE and expiry.
  The old domain is not accepted. Old pending sign-ins are invalidated on upgrade;
  established keys are retained. The manual authorization-code option is retained.
- Historical `.in` observations and screenshot links in `device-testing-report.md`
  remain unchanged; its addendum describes the current configuration.

The earlier suggestion to use a phone-local callback was superseded by the owner's
EU-domain instruction. This build uses the EU HTTPS callback and explicit website
return fallback, not a localhost listener.

## Website files to deploy

These are deployment artifacts, not proof of a deployed site:

1. Serve `openrouter/website/callback.html` at BOTH:
   - `https://hawkfranklin.eu/aura/openrouter/callback`
   - `https://hawkfranklin.eu/aura/openrouter/debug/callback`
2. Serve `openrouter/website/callback.js` at:
   - `https://hawkfranklin.eu/aura/openrouter/callback.js`
3. Publish the association JSON at:
   - `https://hawkfranklin.eu/.well-known/assetlinks.json`

For this specific debug build, use the entry in
`openrouter/website/.well-known/assetlinks.debug.json`. It contains the debug
package and the full public signing fingerprint read from the local certificate.
If the site already has entries, merge; do not overwrite existing associations.
For production, add the production package with its actual **Play App Signing**
certificate using `assetlinks.json.example` as the template. Its placeholder must
never be deployed. The debug association is temporary testing access; remove it
when no longer required and do not distribute the debug private key.

Serve both pages and JSON with HTTPS 200, without redirecting to another host or
adding a trailing slash to callback paths. Exclude callback query values from all
proxy/CDN/server logs. Send `Cache-Control: no-store` and
`Referrer-Policy: no-referrer` on callback responses. Add an HTTP CSP with
`default-src 'none'; script-src 'self'; base-uri 'none'; form-action 'none'; frame-ancestors 'none'`.
The callback JS must remain first-party, with no analytics or third-party assets.
It strips query values from browser history and never exchanges or logs them.
Publish the updated privacy policy at
`https://hawkfranklin.eu/products/aura-privacy.html` and update the Play Console URL.

## Next agent's checks (not executed here)

- Install the new APK over `com.hawkfranklin.aura.debug`; preserve the Play app.
- Cancel the old browser authorization and begin a fresh flow. The consent page
  should show **hawkfranklin.eu** and the debug callback, with `app` identifying
  the debug package. Do not capture code/state/keys in screenshots or reports.
- After website deployment, re-verify Android App Links for the debug package and
  confirm the EU host is verified. A verifier error remains a deployment problem.
- Exercise automatic HTTPS handoff and the website Return to AURA button separately.
- Exercise Open by default settings, denied consent, expired/stale/duplicate
  callbacks, wrong-package return links and process recreation.
- Confirm the key exchange, secure persistence, free catalog and streamed chat.
- Run the existing unit tests and lint; they were not rerun for this follow-up.

Changing a domain in an APK cannot publish or verify the website. No successful
authorization or runtime fix is claimed until the deployment and device checks pass.

## Build artifact

Build-only command completed successfully:

```bash
./android/gradlew -p android --offline :app:assembleDebug
```

APK: `android/app/build/outputs/apk/debug/AURA-eu-callback-debug.apk`
(the standard `app-debug.apk` is the same build).
Application ID: `com.hawkfranklin.aura.debug`.
Version name: `1.0.6-debug`; version code: `24`.
No installation, tests, lint, consent, domain verification, live API call or
website deployment was performed in this follow-up.


## Website source located and prepared

The EU website source is the separate repository
`/home/prime/Documents/github/web-pages/hfreu.github.io`
(`HawkFranklin-Research/hfreu.github.io`, main branch). Callback pages, script,
debug association and the AURA privacy page have now been prepared in its `public`
directory and its Astro build completed. This does not publish them.

The site's GitHub Pages workflow deploys on pushes to main. Publishing this
prepared change was the remaining step; the completed deployment is recorded below. Application
code and device tests were not touched during this website preparation.

GitHub Pages can append a slash to directory routes. The web fallback script now
accepts that same-origin path form; native callback validation remains exact.
GitHub controls its edge logs and headers; source files cannot claim to disable
those logs or set response-level no-store headers. The policy now discloses GitHub
Pages callback hosting. No new live authorization result is claimed.


## GitHub Pages deployment completed — October 20, 2026

At the owner's request, the website changes were preserved, `git pull --ff-only
origin main` completed (already current), and the changes were restored without
conflict. The Astro build passed. Eight website files were committed and pushed
to `HawkFranklin-Research/hfreu.github.io` main:

- Commit: `80ab791461e70c81f326b81d8866172d8d7417d8`
- Commit URL: https://github.com/HawkFranklin-Research/hfreu.github.io/commit/80ab791461e70c81f326b81d8866172d8d7417d8
- Successful Pages workflow: https://github.com/HawkFranklin-Research/hfreu.github.io/actions/runs/39363549934

Publication checks returned HTTP 200 for both callbacks, `callback.js`,
`/.well-known/assetlinks.json`, and `/products/aura-privacy.html`. The association
JSON is served directly as `application/json` and contains the debug package.
GitHub Pages redirects directory callback URLs to the same-origin trailing-slash
form; the fallback script accepts that form. No authorization parameters were
used in these publication checks.

Only the debug association has been published. Production automatic App Links
still require the actual production Play signing certificate entry.

No Android install, device operation, consent approval, key exchange, online chat,
unit-test or lint execution was performed during deployment. The testing agent
should reverify EU App Links for the debug package, cancel any old consent tab,
and start a fresh authorization before proceeding with the device checklist.
