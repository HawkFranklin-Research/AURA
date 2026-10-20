package com.hawkfranklin.aura.data.openrouter

import com.hawkfranklin.aura.data.chat.ChatProvider
import com.hawkfranklin.aura.data.chat.ChatTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenRouterChatProvider @Inject constructor(
  private val http: OpenRouterHttp,
  private val auth: OpenRouterAuthRepository,
  private val catalog: OpenRouterCatalogRepository,
) : ChatProvider {
  override fun stream(target: ChatTarget, messages: List<OpenRouterTurn>): Flow<OpenRouterEvent> = channelFlow {
    var model = (target as? ChatTarget.Online)?.model ?: error("OpenRouter requires an online target")
    // Verify specific models against the catalog; do not trust a stale history entry.
    if (model.id != OpenRouterModel.FREE.id) {
      model = catalog.load().firstOrNull { it.id == model.id }
        ?: throw OpenRouterException(0, "This free model is no longer available. Choose another model.")
    }
    val budget = OnlineContextBudget.prepare(model, messages)
    send(OpenRouterEvent(omittedTurns = budget.omittedTurns))
    val call = http.call("chat/completions", auth.key(), OpenRouterRequestPolicy.request(model, messages))
    val worker = launch(Dispatchers.IO) {
      try {
        call.execute().use { response ->
          checkStatus(response)
          check(response.header("Content-Type")?.startsWith("text/event-stream") == true) { "Expected an online response stream." }
          var completed = false
          val reader = response.body?.charStream() ?: throw IOException("Empty online stream")
          OpenRouterSseParser.parse(reader) { payload ->
            if (payload == "[DONE]") { completed = true; false }
            else {
              val event = OpenRouterSseParser.event(payload)
              // Buffering is bounded by channelFlow. The reader runs on IO, never the UI thread.
              val result = trySendBlocking(event)
              if (result.isFailure) throw IOException("Response stream exceeded the consumer buffer")
              true
            }
          }
          if (!completed) throw IOException("Online stream ended before completion")
        }
        close()
      } catch (e: Exception) { close(e) }
    }
    awaitClose { call.cancel(); worker.cancel() }
  }.buffer(1024)
}
