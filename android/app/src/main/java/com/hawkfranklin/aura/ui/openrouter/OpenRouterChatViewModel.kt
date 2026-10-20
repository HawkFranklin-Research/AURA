package com.hawkfranklin.aura.ui.openrouter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import com.hawkfranklin.aura.data.chat.ChatTarget
import com.hawkfranklin.aura.data.BuiltInTaskId
import com.hawkfranklin.aura.data.DataStoreRepository
import com.hawkfranklin.aura.data.openrouter.*
import com.hawkfranklin.aura.proto.ChatMessageProto
import com.hawkfranklin.aura.proto.ChatSessionProto
import com.hawkfranklin.aura.proto.ChatSideProto
import com.hawkfranklin.aura.ui.common.chat.deleteChatSessionAttachments
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

data class OnlineChatState(
  val model: OpenRouterModel = OpenRouterModel.FREE,
  val sessionId: String = UUID.randomUUID().toString(),
  val messages: List<ChatMessageProto> = emptyList(),
  val history: List<ChatSessionProto> = emptyList(),
  val models: List<OpenRouterModel> = listOf(OpenRouterModel.FREE),
  val inProgress: Boolean = false,
  val loadingModels: Boolean = false,
  val mutatingHistory: Boolean = false,
  val notice: String? = null,
  val contextNotice: String? = null,
)

@HiltViewModel
class OpenRouterChatViewModel @Inject constructor(
  private val repository: DataStoreRepository,
  val auth: OpenRouterAuthRepository,
  private val catalog: OpenRouterCatalogRepository,
  private val provider: OpenRouterChatProvider,
  private val savedState: SavedStateHandle,
  @ApplicationContext private val context: Context,
) : ViewModel() {
  private val _state = MutableStateFlow(OnlineChatState())
  val state = _state.asStateFlow()
  private var generation: Job? = null
  private val persistence = Mutex()
  init {
    viewModelScope.launch {
      refreshHistory()
      val id = savedState.get<String>("onlineSession")
      _state.value.history.firstOrNull { it.sessionId == id && it.providerId == OPENROUTER_PROVIDER }?.let(::load)
    }
  }
  fun refreshModels(force: Boolean = false) {
    if (_state.value.loadingModels) return
    _state.update { it.copy(loadingModels = true, notice = null) }
    viewModelScope.launch {
      try { val models = catalog.load(force); _state.update { it.copy(models = models) } }
      catch (_: Exception) { _state.update { it.copy(notice = "Could not refresh free models. Free Models Router is still selectable; internet is required to send.") } }
      finally { _state.update { it.copy(loadingModels = false) } }
    }
  }
  fun select(model: OpenRouterModel) {
    if (_state.value.inProgress || _state.value.mutatingHistory || !OpenRouterRequestPolicy.isFreeId(model.id)) return
    _state.update { it.copy(model = model, sessionId = UUID.randomUUID().toString(), messages = emptyList(), notice = null, contextNotice = null) }
    savedState["onlineSession"] = null
  }
  fun newChat() = select(_state.value.model)
  fun load(session: ChatSessionProto) {
    if (_state.value.inProgress || _state.value.mutatingHistory || session.providerId != OPENROUTER_PROVIDER) return
    _state.update { it.copy(
      model = OpenRouterModel(session.modelId, session.originalModel), sessionId = session.sessionId,
      messages = OnlineSessionContext.recover(session.messagesList), notice = null, contextNotice = null,
    ) }
    savedState["onlineSession"] = session.sessionId
  }
  suspend fun refreshHistory() {
    val history = withContext(Dispatchers.IO) { repository.readChatSessions() }
    _state.update { it.copy(history = history) }
  }
  fun reloadHistory() { viewModelScope.launch { refreshHistory() } }
  suspend fun renameSession(id: String, title: String) = persistence.withLock {
    check(!_state.value.inProgress && !_state.value.mutatingHistory)
    withContext(Dispatchers.IO) { repository.renameChatSession(id, title) }
    refreshHistory()
  }
  fun deleteSession(id: String) {
    if (_state.value.inProgress || _state.value.mutatingHistory) return
    _state.update { it.copy(mutatingHistory = true) }
    viewModelScope.launch {
      try { persistence.withLock {
        withContext(Dispatchers.IO) { repository.deleteChatSession(id); deleteChatSessionAttachments(context, id) }
        if (_state.value.sessionId == id) {
          _state.update { it.copy(sessionId = UUID.randomUUID().toString(), messages = emptyList()) }
          savedState["onlineSession"] = null
        }
        refreshHistory()
      } } catch (_: Exception) {
        _state.update { it.copy(notice = "Could not delete the saved conversation. Please try again.") }
      } finally { _state.update { it.copy(mutatingHistory = false) } }
    }
  }
  fun clearHistory() {
    if (_state.value.inProgress || _state.value.mutatingHistory) return
    _state.update { it.copy(mutatingHistory = true) }
    viewModelScope.launch {
      try { persistence.withLock {
        withContext(Dispatchers.IO) {
          repository.readChatSessions().forEach { deleteChatSessionAttachments(context, it.sessionId) }
          repository.clearChatSessions()
        }
        _state.update { it.copy(sessionId = UUID.randomUUID().toString(), messages = emptyList()) }
        savedState["onlineSession"] = null
        refreshHistory()
      } } catch (_: Exception) {
        _state.update { it.copy(notice = "Could not clear saved conversations. Please try again.") }
      } finally { _state.update { it.copy(mutatingHistory = false) } }
    }
  }
  private suspend fun persist(snapshot: OnlineChatState) = persistence.withLock {
    if (snapshot.messages.isEmpty()) return@withLock
    val title = snapshot.messages.firstOrNull { it.side == ChatSideProto.CHAT_SIDE_USER }?.content?.trim()?.take(80) ?: "New online chat"
    val session = ChatSessionProto.newBuilder().setSessionId(snapshot.sessionId).setTitle(title)
      .setTimestampMs(System.currentTimeMillis()).setTaskId(BuiltInTaskId.LLM_CHAT)
      .setOriginalModel(snapshot.model.name).setModelId(snapshot.model.id).setProviderId(OPENROUTER_PROVIDER)
      .addAllMessages(snapshot.messages).build()
    withContext(Dispatchers.IO) { repository.upsertChatSession(session) }
    savedState["onlineSession"] = snapshot.sessionId
    refreshHistory()
  }
  fun send(text: String, retry: Boolean = false) {
    if (_state.value.inProgress || _state.value.mutatingHistory || (!retry && text.isBlank()) || !auth.state.value.connected) return
    var messages = _state.value.messages
    if (retry) {
      messages = OnlineSessionContext.retry(messages) ?: return
    } else {
      messages = messages + ChatMessageProto.newBuilder().setMessageType("TEXT")
        .setSide(ChatSideProto.CHAT_SIDE_USER).setContent(text.trim()).build()
    }
    val input = OnlineSessionContext.turns(messages)
    val assistant = ChatMessageProto.newBuilder().setMessageType("TEXT").setSide(ChatSideProto.CHAT_SIDE_MODEL)
      .setIsMarkdown(true).setResponseStatus("streaming").build()
    _state.update { it.copy(messages = messages + assistant, inProgress = true, notice = null, contextNotice = null) }
    val sessionId = _state.value.sessionId
    generation = viewModelScope.launch {
      var outcome = "complete"
      val reasoning = OpenRouterReasoningAccumulator()
      var lastSave = 0L
      try {
        persist(_state.value)
        provider.stream(ChatTarget.Online(_state.value.model), input).collect { event ->
          if (_state.value.sessionId != sessionId) return@collect
          if (event.finishReason == "length") outcome = "truncated"
          if (event.finishReason == "content_filter") outcome = "filtered"
          event.omittedTurns?.let { count ->
            _state.update { it.copy(contextNotice = if (count > 0)
              "$count older conversation turn(s) were left out of this request to fit the context budget. Full history is still saved on this device." else null) }
          }
          reasoning.append(event.reasoning)
          _state.update { state ->
            val current = state.messages.last().toBuilder()
              .setContent(state.messages.last().content + event.text)
            event.model?.let(current::setResolvedModel)
            event.generationId?.let(current::setGenerationId)
            event.usage?.let { current.usageJson = it.toString() }
            if (event.reasoning != null) current.reasoningDetailsJson = reasoning.snapshot().toString()
            state.copy(messages = state.messages.dropLast(1) + current.build())
          }
          if (System.currentTimeMillis() - lastSave > 2000) { persist(_state.value); lastSave = System.currentTimeMillis() }
        }
      } catch (e: CancellationException) { outcome = "interrupted"; throw e }
      catch (e: Exception) {
        outcome = "failed"
        val notice = when (e) {
          is OpenRouterException -> e.message
          is ContextBudgetException -> e.message
          else -> "The online request could not complete. Check your connection, reconnect OpenRouter, or try a downloaded model."
        }
        _state.update { it.copy(notice = notice) }
      } finally {
        withContext(NonCancellable) {
          if (_state.value.sessionId == sessionId) {
            _state.update { state -> state.copy(messages = state.messages.dropLast(1) +
              state.messages.last().toBuilder().setResponseStatus(outcome).build()) }
            try { persist(_state.value) }
            catch (_: Exception) { _state.update { it.copy(notice = "Could not save this conversation on the device.") } }
            _state.update { it.copy(inProgress = false) }
          }
        }
      }
    }
  }
  fun stop() { generation?.cancel() }
  fun disconnect() {
    viewModelScope.launch {
      generation?.cancel(); generation?.join()
      try { auth.disconnect() }
      catch (_: Exception) { _state.update { it.copy(notice = "Could not disconnect securely. Please try again.") } }
    }
  }
}
