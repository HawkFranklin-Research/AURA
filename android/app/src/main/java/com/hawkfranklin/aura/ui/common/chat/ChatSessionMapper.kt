/*
 * Copyright 2026 HawkFranklin Research
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

package com.hawkfranklin.aura.ui.common.chat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import com.hawkfranklin.aura.data.Model
import com.hawkfranklin.aura.data.Task
import com.hawkfranklin.aura.proto.AudioMessageProto
import com.hawkfranklin.aura.proto.ChatMessageProto
import com.hawkfranklin.aura.proto.ChatSessionProto
import com.hawkfranklin.aura.proto.ChatSideProto
import java.io.File
import java.io.FileOutputStream

private const val MESSAGE_TYPE_TEXT = "TEXT"
private const val MESSAGE_TYPE_INFO = "INFO"
private const val MESSAGE_TYPE_WARNING = "WARNING"
private const val MESSAGE_TYPE_ERROR = "ERROR"
private const val MESSAGE_TYPE_IMAGE = "IMAGE"
private const val MESSAGE_TYPE_AUDIO_CLIP = "AUDIO_CLIP"
private const val FALLBACK_SESSION_TITLE = "New chat"

fun buildChatSessionProto(
  context: Context,
  sessionId: String,
  task: Task,
  model: Model,
  messages: List<ChatMessage>,
): ChatSessionProto {
  val attachmentDir = File(context.filesDir, "chat_sessions/$sessionId")
  if (attachmentDir.exists()) {
    attachmentDir.deleteRecursively()
  }
  attachmentDir.mkdirs()

  val protoMessages =
    messages.mapIndexedNotNull { index, message ->
      message.toProto(attachmentDir = attachmentDir, index = index)
    }

  return ChatSessionProto.newBuilder()
    .setSessionId(sessionId)
    .setTitle(messages.toSessionTitle())
    .setTimestampMs(System.currentTimeMillis())
    .setOriginalModel(model.name)
    .setTaskId(task.id)
    .addAllMessages(protoMessages)
    .build()
}

fun ChatSessionProto.toChatMessages(): MutableList<ChatMessage> {
  return messagesList.mapNotNull { it.toChatMessage() }.toMutableList()
}

fun deleteChatSessionAttachments(context: Context, sessionId: String) {
  File(context.filesDir, "chat_sessions/$sessionId").deleteRecursively()
}

private fun ChatMessage.toProto(attachmentDir: File, index: Int): ChatMessageProto? {
  val builder =
    ChatMessageProto.newBuilder()
      .setSide(side.toProto())
      .setLatencyMs(latencyMs)
      .setAccelerator(accelerator)

  return when (this) {
    is ChatMessageText ->
      builder
        .setMessageType(MESSAGE_TYPE_TEXT)
        .setContent(content)
        .setIsMarkdown(isMarkdown)
        .build()

    is ChatMessageInfo -> builder.setMessageType(MESSAGE_TYPE_INFO).setContent(content).build()

    is ChatMessageWarning ->
      builder.setMessageType(MESSAGE_TYPE_WARNING).setContent(content).build()

    is ChatMessageError -> builder.setMessageType(MESSAGE_TYPE_ERROR).setContent(content).build()

    is ChatMessageImage -> {
      bitmaps.forEachIndexed { imageIndex, bitmap ->
        val file = File(attachmentDir, "message_${index}_image_$imageIndex.png")
        FileOutputStream(file).use { output ->
          bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        builder.addImageFilePaths(file.absolutePath)
      }
      builder.setMessageType(MESSAGE_TYPE_IMAGE).build()
    }

    is ChatMessageAudioClip -> {
      val file = File(attachmentDir, "message_${index}_audio_0.pcm")
      FileOutputStream(file).use { output -> output.write(audioData) }
      builder
        .setMessageType(MESSAGE_TYPE_AUDIO_CLIP)
        .addAudioClips(
          AudioMessageProto.newBuilder()
            .setFilePath(file.absolutePath)
            .setSampleRate(sampleRate)
            .build()
        )
        .build()
    }

    else -> null
  }
}

private fun ChatMessageProto.toChatMessage(): ChatMessage? {
  return when (messageType) {
    MESSAGE_TYPE_TEXT ->
      ChatMessageText(
        content = content,
        side = side.toChatSide(),
        latencyMs = latencyMs,
        isMarkdown = isMarkdown,
        accelerator = accelerator,
      )

    MESSAGE_TYPE_INFO -> ChatMessageInfo(content = content)
    MESSAGE_TYPE_WARNING -> ChatMessageWarning(content = content)
    MESSAGE_TYPE_ERROR -> ChatMessageError(content = content)
    MESSAGE_TYPE_IMAGE -> {
      val bitmaps = imageFilePathsList.mapNotNull { BitmapFactory.decodeFile(it) }
      if (bitmaps.isEmpty()) {
        null
      } else {
        ChatMessageImage(
          bitmaps = bitmaps,
          imageBitMaps = bitmaps.map { it.asImageBitmap() },
          side = side.toChatSide(),
          latencyMs = latencyMs,
        )
      }
    }

    MESSAGE_TYPE_AUDIO_CLIP -> {
      val audio = audioClipsList.firstOrNull() ?: return null
      val file = File(audio.filePath)
      if (!file.exists()) {
        null
      } else {
        ChatMessageAudioClip(
          audioData = file.readBytes(),
          sampleRate = audio.sampleRate,
          side = side.toChatSide(),
          latencyMs = latencyMs,
        )
      }
    }

    else -> null
  }
}

private fun List<ChatMessage>.toSessionTitle(): String {
  val text =
    filterIsInstance<ChatMessageText>()
      .firstOrNull { it.side == ChatSide.USER && it.content.isNotBlank() }
      ?.content
      ?.trim()
  if (!text.isNullOrBlank()) {
    return text.take(80)
  }
  if (any { it is ChatMessageAudioClip }) {
    return "Voice note"
  }
  if (any { it is ChatMessageImage }) {
    return "Image chat"
  }
  return FALLBACK_SESSION_TITLE
}

private fun ChatSide.toProto(): ChatSideProto {
  return when (this) {
    ChatSide.USER -> ChatSideProto.CHAT_SIDE_USER
    ChatSide.AGENT -> ChatSideProto.CHAT_SIDE_MODEL
    ChatSide.SYSTEM -> ChatSideProto.CHAT_SIDE_SYSTEM
  }
}

private fun ChatSideProto.toChatSide(): ChatSide {
  return when (this) {
    ChatSideProto.CHAT_SIDE_USER -> ChatSide.USER
    ChatSideProto.CHAT_SIDE_MODEL -> ChatSide.AGENT
    ChatSideProto.CHAT_SIDE_SYSTEM -> ChatSide.SYSTEM
    else -> ChatSide.SYSTEM
  }
}
