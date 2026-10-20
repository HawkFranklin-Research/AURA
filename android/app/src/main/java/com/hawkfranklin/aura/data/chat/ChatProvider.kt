package com.hawkfranklin.aura.data.chat

import com.hawkfranklin.aura.data.openrouter.OpenRouterEvent
import com.hawkfranklin.aura.data.openrouter.OpenRouterTurn
import kotlinx.coroutines.flow.Flow

/** Streaming API contract. Cancellation of collection must close the underlying request. */
interface ChatProvider {
  fun stream(target: ChatTarget, messages: List<OpenRouterTurn>): Flow<OpenRouterEvent>
}
