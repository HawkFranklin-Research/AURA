package com.hawkfranklin.aura.data.openrouter

import com.google.gson.JsonParser
import com.hawkfranklin.aura.proto.ChatMessageProto
import com.hawkfranklin.aura.proto.ChatSideProto

/** Only this conversation enters context; length-limited answers can be continued. */
object OnlineSessionContext {
  fun turns(messages: List<ChatMessageProto>): List<OpenRouterTurn> = messages.filter {
    it.side == ChatSideProto.CHAT_SIDE_USER ||
      (it.side == ChatSideProto.CHAT_SIDE_MODEL && it.responseStatus in setOf("complete", "truncated"))
  }.map {
    OpenRouterTurn(if (it.side == ChatSideProto.CHAT_SIDE_USER) "user" else "assistant", it.content,
      it.reasoningDetailsJson.takeIf(String::isNotBlank)?.let { json -> JsonParser.parseString(json).asJsonArray })
  }
  fun retry(messages: List<ChatMessageProto>): List<ChatMessageProto>? {
    if (messages.lastOrNull()?.responseStatus !in setOf("failed", "interrupted")) return null
    val previous = messages.dropLast(1)
    return previous.takeIf { it.lastOrNull()?.side == ChatSideProto.CHAT_SIDE_USER }
  }
  fun recover(messages: List<ChatMessageProto>) = messages.map {
    if (it.responseStatus == "streaming") it.toBuilder().setResponseStatus("interrupted").build() else it
  }
}
