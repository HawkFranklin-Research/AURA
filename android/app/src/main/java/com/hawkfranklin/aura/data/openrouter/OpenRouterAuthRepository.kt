package com.hawkfranklin.aura.data.openrouter

import com.google.gson.JsonObject
import com.hawkfranklin.aura.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenRouterAuthRepository @Inject constructor(
  private val store: OpenRouterCredentialStore,
  private val http: OpenRouterHttp,
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val mutex = Mutex()
  private val _state = MutableStateFlow(OpenRouterConnection())
  val state = _state.asStateFlow()
  init {
    scope.launch {
      mutex.withLock {
        runCatching {
          val saved = store.read()
          val pending = saved.pending?.takeIf {
            it.callbackUrl == OPENROUTER_CALLBACK && System.currentTimeMillis() - it.createdAt in 0 until OpenRouterPkce.EXPIRY_MS
          }
          if (pending != saved.pending) store.write(OpenRouterSecrets(saved.key, pending, saved.disclosureVersion))
          _state.value = OpenRouterConnection(connected = !saved.key.isNullOrBlank(), pending = pending != null, manual = pending?.manual == true)
        }.onFailure { _state.value = OpenRouterConnection(message = "Secure connection storage is unavailable. Disconnect and reconnect to reset it.") }
      }
    }
  }
  suspend fun begin(manual: Boolean = false): String = withContext(Dispatchers.IO) {
    mutex.withLock {
      val saved = store.read()
      check(!state.value.exchanging) { "Sign-in is already being completed." }
      val created = OpenRouterPkce.create(System.currentTimeMillis())
      val pending = PendingOpenRouterAuth(created.verifier, created.state, created.createdAt, manual)
      store.write(OpenRouterSecrets(saved.key, pending, ONLINE_DISCLOSURE_VERSION))
      _state.value = OpenRouterConnection(connected = !saved.key.isNullOrBlank(), pending = true, manual = manual)
      OpenRouterPkce.url(pending, BuildConfig.APPLICATION_ID)
    }
  }
  fun acceptCallback(callback: String) = complete(callback, false)
  fun acceptCode(code: String) = complete(code, true)
  private fun complete(value: String, manual: Boolean) {
    scope.launch {
      mutex.withLock {
        try {
          val saved = store.read()
          val pending = saved.pending ?: throw IllegalStateException("No pending sign-in. Start again.")
          require(pending.manual == manual)
          val code = if (manual) {
            require(System.currentTimeMillis() - pending.createdAt in 0 until OpenRouterPkce.EXPIRY_MS)
            require(value.isNotBlank() && value.length <= 4096 && value.none { it.isWhitespace() } && !value.contains("://"))
            value
          } else OpenRouterPkce.validate(value, pending, System.currentTimeMillis(), BuildConfig.APPLICATION_ID)
          // Consume before network transmission. A duplicate callback cannot repeat the exchange.
          store.write(OpenRouterSecrets(saved.key, disclosureVersion = saved.disclosureVersion))
          _state.value = OpenRouterConnection(connected = !saved.key.isNullOrBlank(), exchanging = true)
          val body = JsonObject().apply {
            addProperty("code", code); addProperty("code_verifier", pending.verifier)
            addProperty("code_challenge_method", "S256")
          }
          val result = http.json("auth/keys", body)
          val key = result.get("key")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
          require(!key.isNullOrBlank() && key.length <= 8192 && key.none { it.isWhitespace() })
          store.write(OpenRouterSecrets(key = key, disclosureVersion = ONLINE_DISCLOSURE_VERSION))
          _state.value = OpenRouterConnection(connected = true, message = "OpenRouter connected. Choose an online model to start a new chat.")
        } catch (_: Exception) {
          // Callback/exception text can include secrets. Show only fixed text.
          val saved = runCatching { store.read() }.getOrNull()
          _state.value = OpenRouterConnection(connected = !saved?.key.isNullOrBlank(),
            pending = saved?.pending != null, manual = saved?.pending?.manual == true, message = "Sign-in was not completed or the callback was invalid. Cancel and start again if needed.")
        }
      }
    }
  }
  suspend fun cancel() = withContext(Dispatchers.IO) {
    mutex.withLock {
      val saved = store.read()
      store.write(OpenRouterSecrets(saved.key, disclosureVersion = saved.disclosureVersion))
      _state.value = OpenRouterConnection(connected = !saved.key.isNullOrBlank())
    }
  }
  suspend fun disconnect() = withContext(Dispatchers.IO) {
    mutex.withLock { store.clear(); _state.value = OpenRouterConnection() }
  }
  suspend fun key(): String = withContext(Dispatchers.IO) {
    val saved = store.read()
    check(saved.disclosureVersion == ONLINE_DISCLOSURE_VERSION) { "Reconnect to review online privacy information." }
    saved.key?.takeIf { it.isNotBlank() } ?: throw IllegalStateException("Connect OpenRouter to send online messages.")
  }
}
