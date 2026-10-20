package com.hawkfranklin.aura.data.openrouter

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException
import java.io.Reader

/** SSE framing, independent of HTTP packet boundaries. Limits prevent unbounded frames. */
object OpenRouterSseParser {
  fun parse(reader: Reader, onData: (String) -> Boolean) {
    val data = StringBuilder()
    val line = StringBuilder()
    var first = true
    var previousWasCr = false
    fun lineFinished(): Boolean {
      var value = line.toString().removeSuffix("\r")
      line.setLength(0)
      if (first) { value = value.removePrefix("\uFEFF"); first = false }
      if (value.isEmpty()) {
        if (data.isEmpty()) return true
        val payload = data.toString().removeSuffix("\n")
        data.setLength(0)
        return onData(payload)
      }
      if (value.startsWith("data:")) {
        data.append(value.substring(5).removePrefix(" ")).append('\n')
        if (data.length > 1024 * 1024) throw IOException("Stream frame too large")
      }
      return true // comments, event/id/retry fields are not answer content
    }
    while (true) {
      val next = reader.read()
      if (next == -1) break
      if (next == '\n'.code && previousWasCr) { previousWasCr = false; continue }
      previousWasCr = next == '\r'.code
      if (next == '\n'.code || next == '\r'.code) { if (!lineFinished()) return }
      else {
        line.append(next.toChar())
        if (line.length > 1024 * 1024) throw IOException("Stream line too large")
      }
    }
    // An incomplete event at EOF is never a successful completion.
  }

  fun event(payload: String): OpenRouterEvent {
    try {
      val obj = JsonParser.parseString(payload).asJsonObject
      if (obj.has("error")) throw OpenRouterException(0, "The online provider interrupted this response. Try again or use a local model.")
      val choice = obj.getAsJsonArray("choices")?.firstOrNull()?.asJsonObject
      if (choice?.get("finish_reason")?.takeUnless { it.isJsonNull }?.asString == "error")
        throw OpenRouterException(0, "The online provider interrupted this response.")
      val delta = choice?.getAsJsonObject("delta")
      return OpenRouterEvent(
        text = delta?.get("content")?.takeUnless { it.isJsonNull }?.asString ?: "",
        reasoning = delta?.getAsJsonArray("reasoning_details"),
        model = obj.get("model")?.takeUnless { it.isJsonNull }?.asString,
        usage = obj.getAsJsonObject("usage"),
        generationId = obj.get("id")?.takeUnless { it.isJsonNull }?.asString,
        finishReason = choice?.get("finish_reason")?.takeUnless { it.isJsonNull }?.asString,
      )
    } catch (e: OpenRouterException) { throw e }
    catch (_: Exception) { throw IOException("Invalid online stream event") }
  }
}

/** Merge indexed reasoning fragments without concatenating opaque signed values incorrectly. */
class OpenRouterReasoningAccumulator {
  private val details = linkedMapOf<String, JsonObject>()
  fun append(fragments: JsonArray?) {
    fragments?.forEach { element ->
      val fragment = element.asJsonObject
      val index = fragment.get("index")?.asInt ?: 0
      val type = fragment.get("type")?.asString ?: "unknown"
      val key = "$index:$type"
      val previous = details[key]
      if (previous == null) details[key] = fragment.deepCopy()
      else fragment.entrySet().forEach { (field, value) ->
        if (field in setOf("text", "summary", "data") && previous.has(field) && value.isJsonPrimitive) {
          previous.addProperty(field, previous[field].asString + value.asString)
        } else previous.add(field, value.deepCopy())
      }
    }
  }
  fun snapshot(): JsonArray = JsonArray().apply { details.values.forEach { add(it.deepCopy()) } }
}
