package com.hawkfranklin.aura.data.openrouter

import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test
import java.net.URLEncoder

class OpenRouterSecurityTest {
  private val pending = PendingOpenRouterAuth("test-verifier", "expected-state", 1000)
  private fun callback(query: String = "state=expected-state&code=one-time-code") = "$OPENROUTER_CALLBACK?$query"
  private fun rejected(block: () -> Unit) {
    try { block(); fail("Expected rejection") } catch (_: IllegalArgumentException) { }
  }
  @Test fun `PKCE matches RFC 7636 S256 vector`() {
    assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
      OpenRouterPkce.challenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"))
    val first = OpenRouterPkce.create(1000)
    val second = OpenRouterPkce.create(1000)
    assertNotEquals(first.state, second.state)
    assertNotEquals(first.verifier, second.verifier)
    assertTrue(first.verifier.length in 43..128)
    assertFalse(OpenRouterPkce.url(first).contains(first.verifier))
  }
  @Test fun `valid exact callback returns code`() {
    assertEquals("one-time-code", OpenRouterPkce.validate(callback(), pending, 1001))
  }
  @Test fun `reject spoofed callback and duplicate parameters`() {
    listOf(
      callback().replace("hawkfranklin.eu", "evil.example"),
      callback().replace("https:", "http:"),
      callback().replace("hawkfranklin.eu", "hawkfranklin.eu:443"),
      callback().replace("/callback?", "/callback/extra?"),
      callback() + "#fragment",
      callback("state=wrong&code=secret"),
      callback("code=secret"),
      callback() + "&state=expected-state",
      callback() + "&code=duplicate",
      callback("state=expected-state&code=bad%20code"),
      callback("state=expected-state&error=access_denied&code=secret"),
    ).forEach { value -> rejected { OpenRouterPkce.validate(value, pending, 1001) } }
  }
  @Test fun `expired and future sign in rejected`() {
    rejected { OpenRouterPkce.validate(callback(), pending, 999) }
    rejected { OpenRouterPkce.validate(callback(), pending, 1000 + OpenRouterPkce.EXPIRY_MS) }
  }
  @Test fun `headless recovery omits callback without exposing verifier`() {
    val url = OpenRouterPkce.url(PendingOpenRouterAuth("secret", "state", 1, manual = true))
    assertFalse(url.contains("callback_url")); assertFalse(url.contains("state=")); assertFalse(url.contains("secret"))
    assertTrue(url.contains("code_challenge_method=S256"))
  }
  @Test fun `all online requests enforce free routing with zero price limits`() {
    for (id in listOf("openrouter/free", "vendor/chat:free")) {
      val body = OpenRouterRequestPolicy.request(OpenRouterModel(id, "name"), listOf(OpenRouterTurn("user", "hello")))
      val policy = body.getAsJsonObject("provider")
      assertFalse(policy.has("zdr"))
      assertFalse(policy.has("data_collection"))
      assertTrue(policy.getAsJsonObject("max_price").entrySet().all { it.value.asInt == 0 })
      assertFalse(body.has("models")); assertFalse(body.has("plugins")); assertFalse(body.has("tools"))
      assertTrue(body["stream"].asBoolean)
    }
    for (id in listOf("openrouter/auto", "vendor/paid", "vendor/chat:free/paid", "https://evil/free", "model:free")) {
      rejected { OpenRouterRequestPolicy.request(OpenRouterModel(id, "name"), listOf(OpenRouterTurn("user", "secret"))) }
    }
  }
  @Test fun `catalog excludes paid unknown priced and non text models`() {
    fun model(id: String, price: String = "0", output: String = "text", extra: String = "") =
      """{"id":"$id","name":"$id","pricing":{"prompt":"$price","completion":"0"$extra},"architecture":{"input_modalities":["text"],"output_modalities":["$output"]}}"""
    val json = """{"data":[${listOf(
      model("good/chat:free"), model("paid/chat"), model("bad/price:free", "0.001"),
      model("bad/embedding:free", output = "embeddings"), model("bad/unknown:free", "-1"),
      model("bad/request:free", extra = ",\"request\":\"1\""), model("bad/malformed:free", "unknown")
    ).joinToString(",")}]}"""
    assertEquals(listOf("openrouter/free", "good/chat:free"),
      OpenRouterCatalogRepository.parse(JsonParser.parseString(json).asJsonObject).map { it.id })
  }
}
