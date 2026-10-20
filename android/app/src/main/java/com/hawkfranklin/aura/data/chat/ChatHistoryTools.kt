package com.hawkfranklin.aura.data.chat

import com.hawkfranklin.aura.proto.ChatSessionProto
import com.hawkfranklin.aura.proto.ChatSideProto

object ChatHistoryTools {
  fun preserveTitle(incoming: ChatSessionProto, existing: ChatSessionProto?): ChatSessionProto =
    existing?.customTitle?.takeIf { it.isNotBlank() }?.let {
      incoming.toBuilder().setTitle(it).setCustomTitle(it).build()
    } ?: incoming

  fun search(history: List<ChatSessionProto>, query: String): List<ChatSessionProto> {
    val text = query.trim()
    if (text.isEmpty()) return history
    return history.filter { session ->
      session.title.contains(text, ignoreCase = true) ||
        session.originalModel.contains(text, ignoreCase = true) ||
        session.messagesList.any {
          it.side in setOf(ChatSideProto.CHAT_SIDE_USER, ChatSideProto.CHAT_SIDE_MODEL) &&
            it.content.contains(text, ignoreCase = true)
        }
    }
  }

  fun markdown(session: ChatSessionProto): String = buildString {
    appendLine("# ${session.title.replace('\n', ' ').replace('\r', ' ').ifBlank { "AURA chat" }}")
    appendLine()
    appendLine("Mode: ${if (session.providerId == "openrouter") "Online (OpenRouter)" else "On device"}")
    appendLine("Model: ${session.originalModel.replace('\n', ' ').replace('\r', ' ')}")
    appendLine()
    session.messagesList.filter {
      it.side in setOf(ChatSideProto.CHAT_SIDE_USER, ChatSideProto.CHAT_SIDE_MODEL)
    }.forEach { message ->
      appendLine("## ${if (message.side == ChatSideProto.CHAT_SIDE_USER) "You" else "Assistant"}")
      appendLine()
      appendLine(message.content)
      appendLine()
      when (message.responseStatus) {
        "truncated" -> appendLine("_Output limit reached; this response is incomplete._")
        "interrupted", "failed", "streaming" -> appendLine("_Partial response (${message.responseStatus})._")
        "filtered" -> appendLine("_Response limited by the provider's content filter._")
      }
      if (message.imageFilePathsCount > 0 || message.audioClipsCount > 0)
        appendLine("_Image/audio attachments are not included in this text export._")
      appendLine()
    }
  }
}
