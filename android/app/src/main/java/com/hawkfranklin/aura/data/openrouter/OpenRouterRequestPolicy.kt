package com.hawkfranklin.aura.data.openrouter

import com.google.gson.JsonArray
import com.google.gson.JsonObject

/** The sole chat request builder; UI selection cannot bypass this policy. */
object OpenRouterRequestPolicy {
  fun isFreeId(id: String) = id == "openrouter/free" ||
    (id.endsWith(":free") && id.matches(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.:-]+")))
  fun request(model: OpenRouterModel, messages: List<OpenRouterTurn>): JsonObject {
    require(isFreeId(model.id)) { "Only free OpenRouter models are supported." }
    require(messages.isNotEmpty() && messages.all { it.role in setOf("user", "assistant") })
    val context = OnlineContextBudget.prepare(model, messages)
    return JsonObject().apply {
      addProperty("model", model.id)
      addProperty("stream", true)
      addProperty("max_tokens", context.outputTokens)
      // Do not deliberately request extended reasoning. Preserve details returned by the model.
      // Enforce zero pricing so no paid models or fallbacks are ever billed.
      add("provider", JsonObject().apply {
        add("max_price", JsonObject().apply {
          addProperty("prompt", 0); addProperty("completion", 0)
          addProperty("request", 0); addProperty("image", 0)
        })
      })
      add("messages", JsonArray().apply {
        context.messages.forEach { turn -> add(JsonObject().apply {
          addProperty("role", turn.role); addProperty("content", turn.content)
          if (turn.role == "assistant" && turn.reasoning != null) add("reasoning_details", turn.reasoning.deepCopy())
        }) }
      })
    }
  }
}
