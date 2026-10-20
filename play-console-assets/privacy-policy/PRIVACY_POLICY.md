# Privacy Policy — AURA

Effective date: October 20, 2026 (for the update adding optional OpenRouter chat).

AURA is developed by HawkFranklin Research. It offers downloaded, on-device AI
models and optional online text chat using the user's own OpenRouter connection.
These modes have different data flows.

## Downloaded models and local chat

Downloaded models perform inference on your device. AURA does not upload local
chat conversations to OpenRouter. No OpenRouter account is required to download
public models or use local chat. Model downloads and optional Hugging Face
access requests are sent directly to the model host. That host receives normal
network information such as your IP address and requested file.

## Optional OpenRouter chat

Online chat is enabled only after you choose to connect your OpenRouter account.
Authorization takes place in your browser on OpenRouter's website. AURA receives
an API key for your account, not your OpenRouter password.

The browser authorization callback is hosted on HawkFranklin's website using
GitHub Pages. The hosting provider receives the callback request, which can
include a short-lived authorization code and state. The API key is exchanged
only by AURA on your phone and is not sent to that website. The callback page
removes query values from the visible browser history and does not deliberately
log them or include third-party scripts. Hosting-provider request processing
still applies; GitHub states that it logs visitor IP addresses for security.
See [GitHub Pages data collection](https://docs.github.com/en/pages/getting-started-with-github-pages/what-is-github-pages#data-collection)
and the [GitHub Privacy Statement](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement).

When you send an online message, AURA sends that message and the relevant
user/assistant context from the current online conversation directly over HTTPS
to OpenRouter, which routes it to an inference provider. Reasoning details returned
by a model may be saved locally and returned as conversation context. Other saved
conversations are not automatically uploaded. This release supports online text
chat; existing local image/audio tasks remain local.

AURA requests only free models with strict zero price limits ($0 for prompt,
completion, request, and images). If no free endpoint is available, the request
fails; AURA does not select paid models or fallbacks. Free models are hosted by
independent third-party inference providers whose data collection, retention,
and training policies vary. AURA cannot independently audit or guarantee a
provider's conduct.

OpenRouter describes prompt/response storage as opt-in through account controls.
If you enable its content logging or product-use options, those settings apply.
OpenRouter retains request metadata, such as token counts and latency. Its privacy
policy describes account, network, security, legal and other processing. Underlying
providers have their own terms and policies. Review these before connecting:

- [OpenRouter privacy policy](https://openrouter.ai/privacy)
- [OpenRouter data collection](https://openrouter.ai/docs/guides/privacy/data-collection)
- [Provider policies](https://openrouter.ai/providers)
- [OpenRouter terms](https://openrouter.ai/terms)

HawkFranklin Research operates AURA independently. We do not operate OpenRouter
or the inference providers, control their policies, or claim their endorsement.
AURA does not relay chat traffic through a HawkFranklin chat server.

## Local storage, deletion, and access

Chat history, including partial responses, is stored in AURA's app storage.
OpenRouter credentials and pending sign-in secrets are encrypted with an
Android Keystore key and stored in a directory excluded from Android backup.
Current backup rules also exclude chat history, attachments and Hugging Face
credential storage from cloud backup and device transfer. Earlier app versions
may have used different backup rules; this update does not erase earlier backups.

Chat search runs on saved conversations on your device. You can rename chats and
export their visible text as Markdown to a location you select through Android's
file picker. Exports do not include image/audio attachments, credentials, or hidden
reasoning metadata. If you choose a cloud storage provider, that provider receives
the exported file under its own policies. Exported copies are outside AURA's
private storage and are not removed when you delete a chat or uninstall AURA.

For long online chats, AURA may omit older turns from a request to leave room for
the answer. The app indicates when this happens; the full local history remains.

Deleting a chat removes its local saved copy. It does not submit a deletion
request to OpenRouter, its providers, or a previous backup service. Disconnecting
OpenRouter removes AURA's locally stored connection; it does not revoke the remote
API key or close your OpenRouter account. Manage or revoke access in OpenRouter's
account settings. Clearing app data or uninstalling removes app-owned local data,
subject to Android's behavior and any earlier backups.

## Permissions

- Internet: model downloads, optional authentication/catalog requests and online chat.
- Camera/photos: local image input, when you choose that feature.
- Microphone: local audio input and device speech recognition features.
- Notifications/foreground service: model download progress and completion.

Device dictation uses Android's configured speech recognition service. Its
processing, network use and policies depend on the service installed on your
device; AURA does not guarantee every dictation service is offline. Online text
chat does not automatically upload camera images or microphone recordings.

## Analytics and advertising

AURA is ad-free. Firebase Analytics code is present as a dependency, but analytics
collection is explicitly deactivated in this update's application manifest. The
OpenRouter integration does not send chat content or credentials to analytics.
OpenRouter and model hosts process network requests under their own policies.

## Children and changes

AURA is not directed to children under 13. Online providers may impose additional
age and account requirements. We will update this policy when data handling
changes, including the effective date.

## Contact

Contact HawkFranklin Research through https://hawkfranklin.eu for AURA privacy
questions. Contact OpenRouter or the applicable provider for their account/data
requests; AURA does not administer those services.
