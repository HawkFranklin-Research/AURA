# OpenRouter integration

This update adds optional online **text** chat without putting online models in
the download manager or giving them fake downloaded/initialized states. Existing
local LiteRT generation, downloads, image/audio tasks and Labs remain separate.
No local conversation is automatically converted or uploaded to an online chat.

## User flow

The Chat model chip opens a combined picker: On this device and Online ·
OpenRouter. Local rows use the existing ModelPicker and download UI. Online rows
include `openrouter/free` plus current catalog entries ending `:free` whose pricing
and text input/output capabilities pass the filter. Catalog metadata is cached
for one hour; Refresh updates it. Cache failure does not remove saved history.

Connect OpenRouter shows the online disclosure. Continue opens a browser Custom
Tab with S256 PKCE, random state and the AURA key label. OpenRouter returns a code
to the EU HTTPS callback. If Android leaves that callback in the browser, the
website offers a package-targeted Return to AURA button; native PKCE/state checks
still apply. Only the app exchanges it for the user's API key. The code
is consumed before transmission; a failed exchange requires a new authorization.
AURA never handles the OpenRouter account password. Sign-in secrets expire after
10 minutes. The encrypted pending state survives process recreation.

If the browser cannot return automatically, cancel pending sign-in and choose
"Use a browser authorization code instead". This starts a new PKCE headless flow.
The user pastes the **authorization code**, not an API key or callback URL. It is
exchanged using the matching verifier and is not saved as chat history.

Online settings are available in Chat and the app settings dialog. Disconnect
removes local credentials; remote revocation is separate in OpenRouter settings.

## Data path and boundaries

- `data/chat/ChatTarget.kt`: distinct local/online identities.
- `data/openrouter/`: fixed-origin transport, PKCE, encrypted store, catalog,
  request policy and SSE parser/provider.
- `ui/openrouter/`: connection disclosure, combined picker, isolated online
  ViewModel and screen. It uses the existing protobuf history store and drawer.
- Local selection stays in ModelManagerViewModel; online generation does not
  call local engine initialization, error recovery or cleanup.
- Online sessions use `provider_id = openrouter` and stable `model_id`.
  Missing provider in v1.0.6 sessions means local. Existing fields are not renumbered.
- Each response stores actual model, generation ID, response status, opaque
  reasoning details and usage when returned. Reasoning is not shown as answer text.
- Context contains only user turns and completed or length-limited assistant turns from that online
  session. Failed/interrupted assistant responses are not replayed. Retry removes
  the interrupted placeholder and does not duplicate the last user message.
- Stop cancels collection and the underlying network call. Partial output is
  saved as interrupted. Cancellation does not guarantee upstream processing stops.
- Local history restoration resets its LiteRT conversation before transcript replay.
- Online saves occur on IO, at request start, periodically during streaming and
  on completion/interruption. All saved history remains local to this app.

The only generation endpoint is HTTPS `/api/v1/chat/completions` with `stream=true`.
Every request requires zero prompt, completion, request and image price limits
in `provider.max_price`. There are no paid model fallbacks or paid plugins.
Specific free targets are validated against the catalog. A failure never
relaxes pricing limits or selects paid providers.

The transport rejects redirects and automatic connection-failure POST retries,
never logs bodies/keys and presents fixed error messages rather than echoed
upstream error text. The SSE parser handles event framing, comments, usage frames,
reasoning fragments and mid-stream errors; premature EOF is failure.

## Website deployment and production association

See [EU callback handoff](openrouter-eu-callback-handoff.md) for the complete
current deployment instructions and testing-agent checklist.

1. Host `docs/openrouter/website/callback.html` at both the production URL
   `https://hawkfranklin.eu/aura/openrouter/callback` and the debug URL
   `https://hawkfranklin.eu/aura/openrouter/debug/callback`. Serve its first-party
   `callback.js` at `https://hawkfranklin.eu/aura/openrouter/callback.js`.
2. Publish `https://hawkfranklin.eu/.well-known/assetlinks.json` using the template
   under `docs/openrouter/website/.well-known/assetlinks.json.example`.
   Replace its placeholder with the **Play App Signing SHA-256 certificate** from
   Play Console, not the upload certificate. Do not publish the placeholder.
   A separate `assetlinks.debug.json` is supplied for temporary testing of this
   debug package and certificate. Merge entries with the existing association
   file when required, and remove debug authorization after testing.
3. Serve both URLs directly with HTTPS 200; the association file must use JSON.
4. Configure CDN/proxy/server logs to omit callback query values. Send
   `Cache-Control: no-store`, `Referrer-Policy: no-referrer` and a restrictive CSP.
   The page must have no analytics, third-party assets or code/key exchange.
5. Publish the updated AURA privacy page at the existing public policy URL.
6. Install a Play-signed internal testing build and verify App Links on actual
   phones. Browser redirect behavior must be tested, not inferred from the manifest.

The EU callback pages and debug App Links association have been deployed. The
production package association has now been added using the verified signer of
the installed Play app; see release-1.0.7.md. A Play-signed retest is still required. Automatic
return can depend on browser and Android link settings. AURA exposes Open by default
settings and the deployed website offers an explicit return button. The manual
code option can be used without a hosted callback. Debug builds now permanently
use `com.hawkfranklin.aura.debug`, with a separate callback path, to preserve the
installed Play app and its data.

## Verification commands

```bash
cd android
./gradlew :app:assembleDebug :app:testDebugUnitTest
./gradlew :app:lintDebug
adb shell pm verify-app-links --re-verify com.hawkfranklin.aura
adb shell pm get-app-links com.hawkfranklin.aura
```

Do not put a real authorization code in shell history or issue trackers. Use
synthetic values for invalid-callback testing. Actual account consent must be
completed by the account owner.

## Sources and provenance

PKCE behavior was adapted from the scoped reference
`mlca-revized/src/mlca/llm/openrouter_auth.py` and its onboarding entry point;
no Python source or credentials were copied. Android storage and callbacks are new.

- https://openrouter.ai/docs/guides/overview/auth/oauth
- https://openrouter.ai/docs/guides/routing/routers/free-router
- https://openrouter.ai/docs/guides/routing/provider-selection
- https://openrouter.ai/docs/api_reference/streaming
- https://openrouter.ai/docs/guides/best-practices/reasoning-tokens
- https://developer.android.com/training/app-links/about

## v1.0.7 conversation improvements

The shared local/online history drawer searches titles, model names and visible
message text on the phone, renames chats, and exports visible text to Markdown
using Android's document picker. Rename metadata is stored in an appended proto
field and preserved atomically across automatic saves. Attachments and internal
reasoning/usage metadata are excluded from exports. A user-selected cloud document
provider receives exported text under its own policies.

Online requests reserve output and 512 tokens of headroom. The app uses the
catalog's model context/completion limits where available, caps its working
context at 32,768, and falls back to 8,192 for unknown models and Free Models Router.
Output is at most 4,096 tokens (2,048 for the router), or a smaller catalog limit.
UTF-8 bytes plus per-message overhead conservatively estimate input, including
reasoning metadata; this is not an exact tokenizer or a guarantee about every
router-selected provider. Oldest whole turns are omitted with a visible notice;
full history stays local. An oversized latest prompt is rejected without cutting it.
A `length` finish reason persists as `truncated` with a Continue response button.
Continue is a new, user-triggered request; the model can still repeat or diverge.
`content_filter` is displayed separately. Usage frames do not erase terminal status.
