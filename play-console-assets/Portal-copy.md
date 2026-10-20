# AURA — next release listing draft

Baseline: published v1.0.6 (24), commit 65099cb. The online-chat update has not
been published. The candidate is v1.0.7 (25); complete the retest before rollout.

Package: `com.hawkfranklin.aura` (unchanged).

## Listing copy

Title: **AURA: Local & Online AI**

Short description (under 80 characters):

> Chat offline with downloaded AI, or use optional free online models.

Full description:

> AURA brings AI chat to your Android device with two choices.
>
> Download a supported model once and run it on your phone, including when you
> are offline. Local chat does not require an OpenRouter account. Existing local
> image/audio features and Prompt Lab let you explore compatible on-device models.
>
> Or connect your own OpenRouter account to use free online text models without
> downloading a model. Choose Free Models Router or an available individual free
> chat model. Responses stream into your chat, and saved conversations stay in
> AURA's local history.
>
> Online messages and current online conversation context are transmitted to
> OpenRouter and an inference provider. AURA requests only free endpoints.
> Provider data retention policies, availability and rate limits vary; third-party
> policies and account settings apply. AURA cannot independently guarantee those
> services' data practices. Read the online privacy explanation before connecting.
>
> AURA is ad-free and developed independently by HawkFranklin Research. We do not
> operate OpenRouter or its model providers. Downloaded models remain usable if
> you disconnect OpenRouter or lose internet access.
>
> Android 12 or later. Local model memory and storage requirements vary by model.

## Reviewer access

Downloaded public models and local chat do not require sign-in. Optional online
chat requires the reviewer's own OpenRouter account and browser authorization.
No shared developer credential is embedded. The privacy explanation is displayed
before connecting. Free endpoint availability can vary; local chat remains an
independent review path. The browser authorization-code fallback is available if
App Link return is unavailable. Complete the website association before release.

## Privacy, Data Safety and release review

Public privacy URL: https://hawkfranklin.eu/products/aura-privacy.html

Publish the updated policy before submission. Reconcile Data Safety with actual
online message/context transmission, provider metadata processing and SDK behavior;
do not reuse previous local-only answers. Firebase Analytics collection is
explicitly deactivated in this update's manifest. Verify the merged release
manifest. Local deletion does not delete third-party data, and disconnect does not
revoke the remote API key. Do not claim that all provider data retention is zero.

Update screenshots to show On this device / Online, connection disclosure,
streaming and provider-labelled history. Check AI-content safeguards and reporting
against current Play policy. See `docs/openrouter-privacy-and-release.md` for the
remaining release/device checks. This draft does not establish that they passed.
