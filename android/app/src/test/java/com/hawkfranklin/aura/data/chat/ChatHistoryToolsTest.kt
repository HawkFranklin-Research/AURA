package com.hawkfranklin.aura.data.chat

import com.hawkfranklin.aura.proto.ChatMessageProto
import com.hawkfranklin.aura.proto.ChatSessionProto
import com.hawkfranklin.aura.proto.ChatSideProto
import org.junit.Assert.*
import org.junit.Test

class ChatHistoryToolsTest {
  private val message = ChatMessageProto.newBuilder().setSide(ChatSideProto.CHAT_SIDE_MODEL)
    .setContent("Research about orchids").setResponseStatus("truncated")
    .setReasoningDetailsJson("hidden reasoning").setUsageJson("hidden usage")
    .addImageFilePaths("/private/attachment.jpg").build()
  private val session = ChatSessionProto.newBuilder().setSessionId("one").setTitle("Research")
    .setOriginalModel("Local model").addMessages(message).build()

  @Test fun `search matches names and message text locally with case folding`() {
    assertEquals(listOf(session), ChatHistoryTools.search(listOf(session), " ORCHIDS "))
    assertEquals(listOf(session), ChatHistoryTools.search(listOf(session), "local MODEL"))
    assertEquals(emptyList<ChatSessionProto>(), ChatHistoryTools.search(listOf(session), "hidden reasoning"))
    assertEquals(listOf(session), ChatHistoryTools.search(listOf(session), ""))
  }

  @Test fun `automatic save preserves user rename and incoming messages`() {
    val renamed = session.toBuilder().setTitle("My title").setCustomTitle("My title").build()
    val incoming = session.toBuilder().setTitle("Automatic title").setTimestampMs(1234).build()
    val result = ChatHistoryTools.preserveTitle(incoming, renamed)
    assertEquals("My title", result.title)
    assertEquals("My title", result.customTitle)
    assertEquals(1234L, result.timestampMs)
    assertEquals(incoming.messagesList, result.messagesList)
    assertEquals(incoming, ChatHistoryTools.preserveTitle(incoming, session))
    assertEquals(incoming, ChatHistoryTools.preserveTitle(incoming, null))
  }

  @Test fun `export includes visible text and status without private paths or metadata`() {
    val exported = ChatHistoryTools.markdown(session)
    assertTrue(exported.contains("Research about orchids"))
    assertTrue(exported.contains("Output limit reached"))
    assertTrue(exported.contains("attachments are not included"))
    assertFalse(exported.contains("hidden reasoning"))
    assertFalse(exported.contains("hidden usage"))
    assertFalse(exported.contains("/private/"))
    assertEquals("", ChatSessionProto.parseFrom(session.toByteArray()).customTitle)
  }
}
