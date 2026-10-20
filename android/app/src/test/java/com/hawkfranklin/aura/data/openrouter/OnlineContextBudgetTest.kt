package com.hawkfranklin.aura.data.openrouter

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Assert.*
import org.junit.Test

class OnlineContextBudgetTest {
  private val model = OpenRouterModel("test/chat:free", "Test", 4096, 512)

  @Test fun `budget removes entire old turns and reserves output space`() {
    val input = listOf(OpenRouterTurn("user", "old".repeat(1000)),
      OpenRouterTurn("assistant", "answer".repeat(100)), OpenRouterTurn("user", "latest"))
    val budget = OnlineContextBudget.prepare(model, input)
    assertEquals(1, budget.omittedTurns)
    assertEquals(listOf(input.last()), budget.messages)
    assertEquals(512, budget.outputTokens)
    assertEquals(3, input.size) // Full transcript is never mutated.
    val request = OpenRouterRequestPolicy.request(model, input)
    assertEquals(1, request.getAsJsonArray("messages").size())
    assertEquals(512, request["max_tokens"].asInt)
  }

  @Test fun `reasoning and unicode count toward budget without altering retained details`() {
    val reasoning = JsonArray().apply { add(JsonObject().apply { addProperty("data", "界".repeat(1000)) }) }
    val input = listOf(OpenRouterTurn("user", "old"), OpenRouterTurn("assistant", "answer", reasoning),
      OpenRouterTurn("user", "next"))
    assertEquals(1, OnlineContextBudget.prepare(model, input).omittedTurns)
    val large = model.copy(contextLength = 32768)
    val kept = OnlineContextBudget.prepare(large, input)
    assertEquals(reasoning, kept.messages[1].reasoning)
    assertEquals(reasoning, OpenRouterRequestPolicy.request(large, input)
      .getAsJsonArray("messages")[1].asJsonObject.getAsJsonArray("reasoning_details"))
  }

  @Test fun `latest oversized prompt is rejected rather than silently shortened`() {
    try {
      OnlineContextBudget.prepare(model, listOf(OpenRouterTurn("user", "界".repeat(1500))))
      fail("Expected context budget rejection")
    } catch (_: ContextBudgetException) { }
  }

  @Test fun `unknown models and router have bounded fallback budgets`() {
    val input = listOf(OpenRouterTurn("user", "hello"))
    assertEquals(2048, OnlineContextBudget.prepare(OpenRouterModel.FREE, input).outputTokens)
    assertEquals(2048, OnlineContextBudget.prepare(model.copy(contextLength = 0, maxCompletionTokens = 0), input).outputTokens)
    assertEquals(4096, OnlineContextBudget.prepare(model.copy(contextLength = 1000000, maxCompletionTokens = 0), input).outputTokens)
  }

  @Test fun `length and filtering finish reasons survive parser and accounting frames`() {
    for (reason in listOf("length", "content_filter", "stop")) {
      val first = OpenRouterSseParser.event("""{"choices":[{"delta":{"content":"partial"},"finish_reason":"$reason"}]}""")
      val usage = OpenRouterSseParser.event("""{"choices":[{"delta":{},"finish_reason":"$reason"}],"usage":{"completion_tokens":42}}""")
      assertEquals(reason, first.finishReason)
      assertEquals(reason, usage.finishReason)
      assertEquals("partial", first.text)
      assertEquals(42, usage.usage!!["completion_tokens"].asInt)
    }
  }
}
