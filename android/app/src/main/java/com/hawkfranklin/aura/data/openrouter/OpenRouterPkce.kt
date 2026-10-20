package com.hawkfranklin.aura.data.openrouter

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/** Pure JVM component so callback and replay protections can be tested without Android. */
object OpenRouterPkce {
  const val EXPIRY_MS = 10 * 60 * 1000L
  private fun random(bytes: Int): String = Base64.getUrlEncoder().withoutPadding()
    .encodeToString(ByteArray(bytes).also { SecureRandom().nextBytes(it) })
  fun create(now: Long) = PendingOpenRouterAuth(random(48), random(32), now)
  fun challenge(verifier: String): String = Base64.getUrlEncoder().withoutPadding()
    .encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(StandardCharsets.US_ASCII)))
  fun url(pending: PendingOpenRouterAuth, applicationId: String? = null): String {
    require(pending.callbackUrl == OPENROUTER_CALLBACK) { "Callback configuration changed. Start sign-in again." }
    require(applicationId == null || applicationId in setOf("com.hawkfranklin.aura", "com.hawkfranklin.aura.debug"))
    val callback = if (applicationId == null) OPENROUTER_CALLBACK else
      "$OPENROUTER_CALLBACK?app=${URLEncoder.encode(applicationId, "UTF-8")}"
    val params = linkedMapOf(
      "callback_url" to callback,
      "code_challenge" to challenge(pending.verifier),
      "code_challenge_method" to "S256",
      "state" to pending.state,
      "key_label" to "AURA",
    )
    if (pending.manual) { params.remove("callback_url"); params.remove("state") }
    return "https://openrouter.ai/auth?" + params.entries.joinToString("&") {
      "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
    }
  }
  fun validate(callback: String, pending: PendingOpenRouterAuth, now: Long, applicationId: String? = null): String {
    require(pending.callbackUrl == OPENROUTER_CALLBACK) { "Callback configuration changed. Start sign-in again." }
    require(now >= pending.createdAt && now - pending.createdAt < EXPIRY_MS) { "Sign-in expired. Start again." }
    val uri = URI(callback)
    val expected = URI(OPENROUTER_CALLBACK)
    val httpsCallback = uri.scheme == expected.scheme && uri.rawAuthority == expected.rawAuthority && uri.rawPath == expected.rawPath
    val appCallback = applicationId in setOf("com.hawkfranklin.aura", "com.hawkfranklin.aura.debug") &&
      uri.scheme == applicationId && uri.rawAuthority == "openrouter" && uri.rawPath == "/callback"
    require((httpsCallback || appCallback) && uri.rawFragment == null) { "Invalid sign-in callback." }
    val params = (uri.rawQuery ?: "").split('&').map {
      val pair = it.split('=', limit = 2)
      URLDecoder.decode(pair[0], "UTF-8") to URLDecoder.decode(pair.getOrElse(1) { "" }, "UTF-8")
    }.groupBy({ it.first }, { it.second })
    require(params["state"] == listOf(pending.state)) { "Invalid sign-in state. Start again." }
    require(params["error"] == null) { "Authorization was declined. Start again." }
    val codes = params["code"]
    require(codes?.size == 1) { "Missing authorization code. Start again." }
    val code = codes.single()
    require(code.isNotBlank() && code.length <= 4096 && code.none { it.isWhitespace() }) { "Invalid authorization code." }
    return code
  }
}
