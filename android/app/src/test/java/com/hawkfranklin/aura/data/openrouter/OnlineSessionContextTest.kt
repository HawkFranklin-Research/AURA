package com.hawkfranklin.aura.data.openrouter

import com.hawkfranklin.aura.proto.ChatMessageProto
import com.hawkfranklin.aura.proto.ChatSessionProto
import com.hawkfranklin.aura.proto.ChatSideProto
import org.junit.Assert.*
import org.junit.Test

class OnlineSessionContextTest {
  private fun message(side: ChatSideProto, text: String, status: String = "") = ChatMessageProto.newBuilder()
    .setMessageType("TEXT").setSide(side).setContent(text).setResponseStatus(status).build()
  @Test fun `only current completed turns enter context`() {
    val current = listOf(
      message(ChatSideProto.CHAT_SIDE_USER, "question"),
      message(ChatSideProto.CHAT_SIDE_MODEL, "answer", "complete"),
      message(ChatSideProto.CHAT_SIDE_USER, "next"),
      message(ChatSideProto.CHAT_SIDE_MODEL, "incomplete", "interrupted"),
      message(ChatSideProto.CHAT_SIDE_SYSTEM, "error detail"),
    )
    assertEquals(listOf("question", "answer", "next"), OnlineSessionContext.turns(current).map { it.content })
    val other = listOf(message(ChatSideProto.CHAT_SIDE_USER, "unrelated private local question"))
    assertFalse(OnlineSessionContext.turns(current).any { it.content == other.single().content })
  }
  @Test fun `truncated assistant response remains available to an explicit continuation`() {
    val messages = listOf(message(ChatSideProto.CHAT_SIDE_USER, "Write a document"),
      message(ChatSideProto.CHAT_SIDE_MODEL, "First part", "truncated"),
      message(ChatSideProto.CHAT_SIDE_USER, "Continue"))
    assertEquals(listOf("Write a document", "First part", "Continue"),
      OnlineSessionContext.turns(messages).map { it.content })
    assertNull(OnlineSessionContext.retry(messages.dropLast(1)))
    assertEquals("truncated", OnlineSessionContext.recover(messages)[1].responseStatus)
  }
  @Test fun `retry keeps the last user message exactly once`() {
    val user = message(ChatSideProto.CHAT_SIDE_USER, "hello")
    val failed = message(ChatSideProto.CHAT_SIDE_MODEL, "partial", "failed")
    assertEquals(listOf(user), OnlineSessionContext.retry(listOf(user, failed)))
    assertNull(OnlineSessionContext.retry(listOf(user)))
    assertNull(OnlineSessionContext.retry(listOf(user, failed.toBuilder().setResponseStatus("complete").build())))
  }
  @Test fun `process death recovers streaming response as interrupted`() {
    val complete = message(ChatSideProto.CHAT_SIDE_MODEL, "done", "complete")
    val partial = message(ChatSideProto.CHAT_SIDE_MODEL, "partial", "streaming")
    val recovered = OnlineSessionContext.recover(listOf(complete, partial))
    assertEquals("complete", recovered[0].responseStatus)
    assertEquals("interrupted", recovered[1].responseStatus)
    assertEquals("partial", recovered[1].content)
  }
  @Test fun `v1_0_6 wire session remains local with preserved fields`() {
    // Encoded with the legacy fields only: session_id=legacy, original_model=AURA Chat 1B.
    val bytes = byteArrayOf(0x0a, 0x06) + "legacy".toByteArray() + byteArrayOf(0x22, 0x0c) + "AURA Chat 1B".toByteArray()
    val legacy = ChatSessionProto.parseFrom(bytes)
    assertEquals("legacy", legacy.sessionId)
    assertEquals("AURA Chat 1B", legacy.originalModel)
    assertEquals("", legacy.providerId)
    val reread = ChatSessionProto.parseFrom(legacy.toBuilder().setTitle("Preserved").build().toByteArray())
    assertEquals(legacy.originalModel, reread.originalModel)
    assertEquals("", reread.providerId)
  }
}
