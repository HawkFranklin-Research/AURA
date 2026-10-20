package com.hawkfranklin.aura.ui.openrouter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import com.hawkfranklin.aura.data.openrouter.OPENROUTER_PROVIDER
import com.hawkfranklin.aura.proto.ChatSessionProto
import com.hawkfranklin.aura.proto.ChatSideProto
import com.hawkfranklin.aura.ui.common.MarkdownText
import com.hawkfranklin.aura.ui.common.chat.ChatHistorySideSheetContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenRouterChatScreen(
  viewModel: OpenRouterChatViewModel,
  onChooseModel: () -> Unit,
  onLocalSession: (ChatSessionProto) -> Unit,
  onUseLocal: () -> Unit,
) {
  val state by viewModel.state.collectAsState()
  val connection by viewModel.auth.state.collectAsState()
  val drawer = rememberDrawerState(DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val list = rememberLazyListState()
  var draft by rememberSaveable(state.sessionId) { mutableStateOf("") }
  var showConnection by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) { viewModel.reloadHistory() }
  LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.content) {
    if (state.messages.isNotEmpty() && (list.layoutInfo.visibleItemsInfo.isEmpty() ||
        list.layoutInfo.visibleItemsInfo.last().index >= state.messages.lastIndex - 1))
      list.scrollToItem(state.messages.lastIndex)
  }
  BackHandler {
    if (drawer.isOpen) scope.launch { drawer.close() }
    else if (!state.inProgress && !state.mutatingHistory) onUseLocal()
  }
  ModalNavigationDrawer(drawerState = drawer, gesturesEnabled = !state.inProgress && !state.mutatingHistory, drawerContent = {
    ModalDrawerSheet {
      ChatHistorySideSheetContent(
        history = state.history.filter { it.taskId == "llm_chat" },
        onHistoryItemClicked = { id ->
          if (!state.inProgress && !state.mutatingHistory) state.history.firstOrNull { it.sessionId == id }?.let {
            scope.launch { drawer.close() }
            if (it.providerId == OPENROUTER_PROVIDER) viewModel.load(it) else onLocalSession(it)
          }
        },
        onHistoryItemDeleted = viewModel::deleteSession,
        onHistoryItemRenamed = viewModel::renameSession,
        actionsEnabled = !state.inProgress && !state.mutatingHistory,
        onHistoryItemsDeleteAll = viewModel::clearHistory,
        onNewChatClicked = { if (!state.inProgress && !state.mutatingHistory) { viewModel.newChat(); scope.launch { drawer.close() } } },
        onDismissed = { scope.launch { drawer.close() } },
      )
    }
  }) {
    Scaffold(
      topBar = {
        CenterAlignedTopAppBar(
          title = {
            TextButton(onClick = onChooseModel, enabled = !state.inProgress && !state.mutatingHistory) {
              Column {
                Text(state.model.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Online · OpenRouter ▾", style = MaterialTheme.typography.labelSmall)
              }
            }
          },
          navigationIcon = { IconButton(onClick = { viewModel.reloadHistory(); scope.launch { drawer.open() } }, enabled = !state.inProgress && !state.mutatingHistory) {
            Icon(Icons.Rounded.Menu, "Open chat history")
          } },
          actions = {
            IconButton(onClick = { showConnection = true }, enabled = !state.inProgress && !state.mutatingHistory) { Icon(Icons.Rounded.Settings, "OpenRouter connection") }
            IconButton(onClick = viewModel::newChat, enabled = !state.inProgress && !state.mutatingHistory) { Icon(Icons.Rounded.AddComment, "New online chat") }
          },
        )
      },
      bottomBar = {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          if (!connection.connected) Button(onClick = { showConnection = true }, modifier = Modifier.fillMaxWidth()) { Text("Connect OpenRouter") }
          state.contextNotice?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
          if (state.messages.lastOrNull()?.responseStatus == "truncated" && !state.inProgress && !state.mutatingHistory) {
            TextButton(onClick = { viewModel.send("Continue from where your previous response stopped, without repeating it.") }, enabled = connection.connected) { Text("Continue response") }
          }
          state.notice?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
          if (state.messages.lastOrNull()?.responseStatus in setOf("failed", "interrupted") && !state.inProgress && !state.mutatingHistory) {
            TextButton(onClick = { viewModel.send("", retry = true) }, enabled = connection.connected) { Text("Retry last request") }
          }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = draft, onValueChange = { draft = it.take(100000) },
              placeholder = { Text("Message online model…") }, modifier = Modifier.weight(1f), maxLines = 4,
              enabled = !state.inProgress && !state.mutatingHistory && connection.connected)
            if (state.inProgress) FilledTonalButton(onClick = viewModel::stop) { Text("Stop") }
            else Button(onClick = { viewModel.send(draft); draft = "" }, enabled = connection.connected && !state.mutatingHistory && draft.isNotBlank()) { Text("Send") }
          }
          Text("Online messages go to OpenRouter and its providers. Their data retention policies vary.",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      },
    ) { padding ->
      if (state.messages.isEmpty()) {
        Column(Modifier.padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text("Free online chat", style = MaterialTheme.typography.headlineSmall)
          Text("Use your OpenRouter connection without downloading a model. Free Models Router may choose a different model for each reply. Availability and rate limits vary.")
          Text("Only messages in this online conversation are sent. Your other saved chats are not uploaded.")
          TextButton(onClick = onChooseModel) { Text("Choose a model") }
          TextButton(onClick = onUseLocal) { Text("Use a downloaded model") }
          TextButton(onClick = { showConnection = true }) { Text("How online privacy works") }
        }
      } else {
        LazyColumn(state = list, modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)) {
          itemsIndexed(state.messages) { index, message ->
            val user = message.side == ChatSideProto.CHAT_SIDE_USER
            Surface(color = if (user) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
              shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
              Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (user) "You" else message.resolvedModel.ifBlank { state.model.name }, style = MaterialTheme.typography.labelMedium)
                SelectionContainer {
                  if (user) Text(message.content) else MarkdownText(message.content.ifEmpty {
                    if (state.inProgress && index == state.messages.lastIndex) "Waiting for online model…" else "No answer received."
                  })
                }
                if (message.responseStatus in setOf("failed", "interrupted", "truncated", "filtered")) Text(when (message.responseStatus) {
                  "truncated" -> "Output limit reached · use Continue response or send a follow-up"
                  "filtered" -> "Response limited by the provider’s content filter"
                  else -> "${message.responseStatus.replaceFirstChar { it.uppercase() }} · partial response"
                }, style = MaterialTheme.typography.labelSmall)
              }
            }
          }
        }
      }
    }
  }
  if (showConnection) OpenRouterConnectSheet(onDismiss = { showConnection = false })
}
