package com.hawkfranklin.aura.data.openrouter

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.hawkfranklin.aura.BuildConfig

const val OPENROUTER_PROVIDER = "openrouter"
const val OPENROUTER_CALLBACK_HOST = "hawkfranklin.eu"
const val OPENROUTER_CALLBACK = "https://$OPENROUTER_CALLBACK_HOST" + BuildConfig.OPENROUTER_CALLBACK_PATH
const val OPENROUTER_API = "https://openrouter.ai/api/v1/"
const val OPENROUTER_PRIVACY = "https://openrouter.ai/privacy"
const val ONLINE_DISCLOSURE_VERSION = 1

data class OpenRouterModel(val id: String, val name: String, val contextLength: Int = 0, val maxCompletionTokens: Int = 0) {
  companion object {
    val FREE = OpenRouterModel("openrouter/free", "Free Models Router")
  }
}

// Not data classes: secret-bearing objects must never acquire generated toString() methods.
class PendingOpenRouterAuth(
  val verifier: String,
  val state: String,
  val createdAt: Long,
  val manual: Boolean = false,
  // Old encrypted pending sign-ins for the previous domain are invalidated on upgrade.
  val callbackUrl: String = OPENROUTER_CALLBACK,
)
class OpenRouterSecrets(
  val key: String? = null,
  val pending: PendingOpenRouterAuth? = null,
  val disclosureVersion: Int = 0,
)

data class OpenRouterConnection(
  val connected: Boolean = false,
  val pending: Boolean = false,
  val exchanging: Boolean = false,
  val manual: Boolean = false,
  val message: String? = null,
)

data class OpenRouterTurn(
  val role: String,
  val content: String,
  val reasoning: JsonArray? = null,
)

data class OpenRouterEvent(
  val text: String = "",
  val reasoning: JsonArray? = null,
  val model: String? = null,
  val usage: JsonObject? = null,
  val generationId: String? = null,
  val finishReason: String? = null,
  val omittedTurns: Int? = null,
)

class OpenRouterException(val status: Int, message: String) : Exception(message)
