package com.hawkfranklin.aura.data.openrouter

import android.content.Context
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenRouterCatalogRepository @Inject constructor(
  private val http: OpenRouterHttp,
  @ApplicationContext context: Context,
) {
  private val cache = File(context.cacheDir, "openrouter_models.json")
  suspend fun load(refresh: Boolean = false): List<OpenRouterModel> = withContext(Dispatchers.IO) {
    if (!refresh && cache.exists() && System.currentTimeMillis() - cache.lastModified() < 60 * 60 * 1000) {
      runCatching { parse(JsonParser.parseString(cache.readText()).asJsonObject) }.getOrNull()?.let { return@withContext it }
    }
    val result = http.json("models")
    val models = parse(result)
    cache.writeText(result.toString())
    models
  }
  companion object {
    fun parse(result: JsonObject): List<OpenRouterModel> = listOf(OpenRouterModel.FREE) +
      (result.getAsJsonArray("data") ?: throw IllegalArgumentException("Invalid catalog")).mapNotNull { element ->
        runCatching {
          val obj = element.asJsonObject
          val id = obj.get("id").asString
          if (!id.endsWith(":free") || !OpenRouterRequestPolicy.isFreeId(id)) return@runCatching null
          val prices = obj.getAsJsonObject("pricing") ?: return@runCatching null
          // Reject positive, negative, malformed and unknown-priced entries.
          if (!listOf("prompt", "completion").all { prices.has(it) && prices[it].asBigDecimal.signum() == 0 }) return@runCatching null
          if (prices.entrySet().any { (_, price) -> price.asBigDecimal.signum() != 0 }) return@runCatching null
          val architecture = obj.getAsJsonObject("architecture") ?: return@runCatching null
          if (architecture.getAsJsonArray("input_modalities")?.any { it.asString == "text" } != true ||
            architecture.getAsJsonArray("output_modalities")?.any { it.asString == "text" } != true) return@runCatching null
          OpenRouterModel(id, obj.get("name")?.asString ?: id, obj.get("context_length")?.asInt ?: 0,
            obj.getAsJsonObject("top_provider")?.get("max_completion_tokens")?.takeUnless { it.isJsonNull }?.asInt ?: 0)
        }.getOrNull()
      }.distinctBy { it.id }.sortedBy { it.name.lowercase() }
  }
}
