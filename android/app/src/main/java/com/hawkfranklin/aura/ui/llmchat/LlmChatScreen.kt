/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hawkfranklin.aura.ui.llmchat

import androidx.hilt.navigation.compose.hiltViewModel

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.hawkfranklin.aura.ui.openrouter.*
import kotlinx.coroutines.launch
import com.hawkfranklin.aura.proto.ChatSessionProto
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import com.hawkfranklin.aura.data.BuiltInTaskId
import com.hawkfranklin.aura.firebaseAnalytics
import com.hawkfranklin.aura.ui.common.chat.ChatMessageAudioClip
import com.hawkfranklin.aura.ui.common.chat.ChatMessageImage
import com.hawkfranklin.aura.ui.common.chat.ChatMessageText
import com.hawkfranklin.aura.ui.common.chat.ChatView
import com.hawkfranklin.aura.ui.modelmanager.ModelManagerViewModel

@Composable
fun LlmChatScreen(
  modelManagerViewModel: ModelManagerViewModel,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: LlmChatViewModel = hiltViewModel(),
) {
  val online: OpenRouterChatViewModel = hiltViewModel()
  var onlineMode by rememberSaveable { mutableStateOf(false) }
  var showPicker by remember { mutableStateOf(false) }
  val managerState by modelManagerViewModel.uiState.collectAsState()
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val task = modelManagerViewModel.getTaskById(BuiltInTaskId.LLM_CHAT) ?: return
  val restoreLocal: (ChatSessionProto) -> Unit = { session ->
    val model = task.models.firstOrNull { it.name == session.originalModel }
    if (model != null) {
      LlmChatModelHelper.resetConversation(model, model.llmSupportImage, model.llmSupportAudio)
      modelManagerViewModel.selectModel(model)
      viewModel.loadSession(session.sessionId, model)
      onlineMode = false
    } else android.widget.Toast.makeText(context, "The original local model is unavailable.", android.widget.Toast.LENGTH_LONG).show()
  }
  if (onlineMode) {
    OpenRouterChatScreen(online, onChooseModel = { showPicker = true }, onLocalSession = restoreLocal,
      onUseLocal = { showPicker = true })
  } else {
    ChatViewWrapper(
      viewModel = viewModel, modelManagerViewModel = modelManagerViewModel,
      taskId = BuiltInTaskId.LLM_CHAT, navigateUp = navigateUp, modifier = modifier,
      onOpenModelPicker = { showPicker = true },
      onOnlineSession = { online.load(it); onlineMode = true },
    )
  }
  if (showPicker) ChatModelPicker(
    task = task, modelManager = modelManagerViewModel, online = online,
    onLocal = { model ->
      showPicker = false
      val sameLocalTarget = !onlineMode && managerState.selectedModel.name == model.name
      scope.launch {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
          modelManagerViewModel.cleanupModel(context, task, managerState.selectedModel)
        }
        // Selecting another target starts a new local chat; no online transcript is transferred.
        viewModel.startNewSession(model)
        modelManagerViewModel.selectModel(model)
        onlineMode = false
        // A different target is initialized by ChatView's download effect. Same-name selection
        // does not change that effect's key, so only that case needs an explicit initialization.
        if (sameLocalTarget && managerState.modelDownloadStatus[model.name]?.status ==
          com.hawkfranklin.aura.data.ModelDownloadStatusType.SUCCEEDED) {
          modelManagerViewModel.initializeModel(context, task, model)
        }
      }
    },
    onOnline = { model ->
      showPicker = false
      scope.launch(kotlinx.coroutines.Dispatchers.Default) {
        modelManagerViewModel.cleanupModel(context, task, managerState.selectedModel)
      }
      online.select(model); onlineMode = true
    }, onDismiss = { showPicker = false },
  )
}

@Composable
fun LlmAskImageScreen(
  modelManagerViewModel: ModelManagerViewModel,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: LlmAskImageViewModel = hiltViewModel(),
) {
  ChatViewWrapper(
    viewModel = viewModel,
    modelManagerViewModel = modelManagerViewModel,
    taskId = BuiltInTaskId.LLM_ASK_IMAGE,
    navigateUp = navigateUp,
    modifier = modifier,
  )
}

@Composable
fun LlmAskAudioScreen(
  modelManagerViewModel: ModelManagerViewModel,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: LlmAskAudioViewModel = hiltViewModel(),
) {
  ChatViewWrapper(
    viewModel = viewModel,
    modelManagerViewModel = modelManagerViewModel,
    taskId = BuiltInTaskId.LLM_ASK_AUDIO,
    navigateUp = navigateUp,
    modifier = modifier,
  )
}

@Composable
fun ChatViewWrapper(
  viewModel: LlmChatViewModelBase,
  modelManagerViewModel: ModelManagerViewModel,
  taskId: String,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  onOpenModelPicker: (() -> Unit)? = null,
  onOnlineSession: ((ChatSessionProto) -> Unit)? = null,
) {
  val context = LocalContext.current
  val task = modelManagerViewModel.getTaskById(id = taskId)!!
  val persistenceScope = rememberCoroutineScope()

  ChatView(
    onOpenModelPicker = onOpenModelPicker,
    onOnlineSession = onOnlineSession,
    task = task,
    viewModel = viewModel,
    modelManagerViewModel = modelManagerViewModel,
    onSendMessage = { model, messages ->
      for (message in messages) {
        viewModel.addMessage(model = model, message = message)
      }
      viewModel.persistCurrentSession(context = context, task = task, model = model)

      var text = ""
      val images: MutableList<Bitmap> = mutableListOf()
      val audioMessages: MutableList<ChatMessageAudioClip> = mutableListOf()
      var chatMessageText: ChatMessageText? = null
      for (message in messages) {
        if (message is ChatMessageText) {
          chatMessageText = message
          text = message.content
        } else if (message is ChatMessageImage) {
          images.addAll(message.bitmaps)
        } else if (message is ChatMessageAudioClip) {
          audioMessages.add(message)
        }
      }
      if ((text.isNotEmpty() && chatMessageText != null) || audioMessages.isNotEmpty()) {
        modelManagerViewModel.addTextInputHistory(text)
        viewModel.generateResponse(
          context = context,
          task = task,
          model = model,
          input = text,
          images = images,
          audioMessages = audioMessages,
          onError = { errorMessage ->
            viewModel.handleError(
              context = context,
              task = task,
              model = model,
              errorMessage = errorMessage,
              modelManagerViewModel = modelManagerViewModel,
            )
          },
        )

        firebaseAnalytics?.logEvent(
          "generate_action",
          bundleOf("capability_name" to task.id, "model_id" to model.name),
        )
      }
    },
    onRunAgainClicked = { model, message ->
      if (message is ChatMessageText) {
        viewModel.runAgain(
          context = context,
          task = task,
          model = model,
          message = message,
          onError = { errorMessage ->
            viewModel.handleError(
              context = context,
              task = task,
              model = model,
              errorMessage = errorMessage,
              modelManagerViewModel = modelManagerViewModel,
            )
          },
        )
      }
    },
    onResetSessionClicked = { model -> viewModel.resetSession(task = task, model = model) },
    showStopButtonInInputWhenInProgress = true,
    onStopButtonClicked = { model ->
      viewModel.stopResponse(model = model)
      persistenceScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        viewModel.persistCurrentSession(context, task, model)
      }
    },
    navigateUp = navigateUp,
    modifier = modifier,
  )
}
