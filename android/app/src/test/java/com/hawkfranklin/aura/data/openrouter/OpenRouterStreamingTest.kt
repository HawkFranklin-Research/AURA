package com.hawkfranklin.aura.data.openrouter

import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test
import java.io.Reader
import java.io.StringReader
import java.io.IOException

class OpenRouterStreamingTest {
  @Test fun `SSE handles unicode comments CRLF multiline frames and done`() {
    val source = ": keepalive\r\n\r\ndata: {\"text\":\r\ndata: \"héllo 🌱\"}\r\n\r\ndata: [DONE]\r\n\r\ndata: ignored\n\n"
    val events = mutableListOf<String>()
    OpenRouterSseParser.parse(StringReader(source)) { events.add(it); it != "[DONE]" }
    assertEquals(2, events.size)
    assertEquals("héllo 🌱", JsonParser.parseString(events[0]).asJsonObject["text"].asString)
    assertEquals("[DONE]", events[1])
  }
  @Test fun `incomplete frames do not become completion`() {
    val events = mutableListOf<String>()
    OpenRouterSseParser.parse(StringReader("data: [DONE]")) { events.add(it); true }
    assertTrue(events.isEmpty())
  }
  @Test fun `answer usage and reasoning are separate`() {
    val event = OpenRouterSseParser.event("""{"model":"actual/free","choices":[{"delta":{"content":"Answer","reasoning_details":[{"index":0,"type":"reasoning.text","text":"detail"}]}}],"usage":{"total_tokens":42}}""")
    assertEquals("Answer", event.text)
    assertEquals("actual/free", event.model)
    assertEquals(42, event.usage!!["total_tokens"].asInt)
    assertEquals("detail", event.reasoning!![0].asJsonObject["text"].asString)
    val usage = OpenRouterSseParser.event("""{"choices":[],"usage":{"total_tokens":4}}""")
    assertEquals("", usage.text)
  }
  @Test fun `midstream errors are failures with no echoed provider content`() {
    try {
      OpenRouterSseParser.event("""{"error":{"message":"SECRET_PROMPT"},"choices":[{"finish_reason":"error"}]}""")
      fail("Expected provider failure")
    } catch (e: OpenRouterException) { assertFalse(e.message!!.contains("SECRET_PROMPT")) }
  }
  @Test fun `reasoning fragments preserve index type and signed fields`() {
    val accumulator = OpenRouterReasoningAccumulator()
    accumulator.append(JsonParser.parseString("""[{"index":0,"type":"reasoning.text","text":"part "},{"index":1,"type":"reasoning.encrypted","data":"abc","signature":"sig"}]""").asJsonArray)
    accumulator.append(JsonParser.parseString("""[{"index":0,"type":"reasoning.text","text":"two"},{"index":1,"type":"reasoning.encrypted","data":"def"}]""").asJsonArray)
    val details = accumulator.snapshot()
    assertEquals("part two", details[0].asJsonObject["text"].asString)
    assertEquals("abcdef", details[1].asJsonObject["data"].asString)
    assertEquals("sig", details[1].asJsonObject["signature"].asString)
    val body = OpenRouterRequestPolicy.request(OpenRouterModel.FREE, listOf(OpenRouterTurn("user", "question"), OpenRouterTurn("assistant", "answer", details)))
    assertEquals(details, body.getAsJsonArray("messages")[1].asJsonObject.getAsJsonArray("reasoning_details"))
  }
  @Test fun `malformed and oversized frames are rejected`() {
    try { OpenRouterSseParser.event("not-json"); fail("Expected invalid JSON") } catch (_: IOException) { }
    try {
      OpenRouterSseParser.parse(StringReader("data: " + "x".repeat(1024 * 1024 + 1))) { true }
      fail("Expected size limit")
    } catch (_: IOException) { }
  }
}
