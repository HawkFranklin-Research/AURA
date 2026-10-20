package com.hawkfranklin.aura.data.openrouter

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

@Module
@InstallIn(SingletonComponent::class)
object OpenRouterModule {
  @Provides @Singleton fun httpClient(): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS).readTimeout(90, TimeUnit.SECONDS)
    .callTimeout(5, TimeUnit.MINUTES)
    .followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false).build()
}

/** Fixed origin, no request/body logging, redirects or transparent POST retries. */
@Singleton
class OpenRouterHttp @Inject constructor(private val client: OkHttpClient) {
  fun call(path: String, key: String? = null, body: JsonObject? = null): Call {
    require(path in setOf("auth/keys", "models", "chat/completions"))
    val request = Request.Builder().url(OPENROUTER_API + path)
      .header("Accept", if (path == "chat/completions") "text/event-stream" else "application/json")
      .header("HTTP-Referer", "https://hawkfranklin.eu/products/aura")
      .header("X-OpenRouter-Title", "AURA")
    key?.let { request.header("Authorization", "Bearer $it") }
    body?.let { request.post(it.toString().toRequestBody("application/json".toMediaType())) }
    return client.newCall(request.build()).also {
      if (path != "chat/completions") it.timeout().timeout(30, TimeUnit.SECONDS)
    }
  }
  suspend fun json(path: String, body: JsonObject? = null): JsonObject = call(path, body = body).await().use {
    checkStatus(it)
    val source = it.body?.source() ?: throw IOException("Empty response")
    require(source.request((if (path == "models") 8 * 1024 * 1024L else 65536L) + 1).not()) { "Response too large" }
    JsonParser.parseString(source.readUtf8()).asJsonObject
  }
}

suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
  continuation.invokeOnCancellation { cancel() }
  enqueue(object : Callback {
    override fun onFailure(call: Call, e: IOException) {
      if (continuation.isActive) continuation.resumeWithException(e)
    }
    override fun onResponse(call: Call, response: Response) {
      continuation.resume(response) { _, value, _ -> value.close() }
    }
  })
}

fun checkStatus(response: Response) {
  if (response.isSuccessful) return
  // Never expose upstream error bodies: they can echo prompts or credentials.
  val message = when (response.code) {
    401, 403 -> "OpenRouter access was rejected. Reconnect or check your OpenRouter account settings."
    402 -> "OpenRouter could not serve this free request. No paid fallback was requested."
    429 -> "OpenRouter's free request limit was reached. Try again later or use a downloaded model."
    404, 502, 503 -> "No free endpoint is currently available on OpenRouter. Try later or use a local model."
    else -> "OpenRouter could not complete the request (HTTP ${response.code})."
  }
  throw OpenRouterException(response.code, message)
}
