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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.hawkfranklin.aura.data.chat.ChatHistoryTools
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hawkfranklin.aura.R
import com.hawkfranklin.aura.proto.ChatSessionProto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatHistorySideSheetContent(
  history: List<ChatSessionProto>,
  onHistoryItemClicked: (String) -> Unit,
  onHistoryItemDeleted: (String) -> Unit,
  onHistoryItemsDeleteAll: () -> Unit,
  onNewChatClicked: () -> Unit,
  onDismissed: () -> Unit,
  onHistoryItemRenamed: suspend (String, String) -> Unit,
  actionsEnabled: Boolean = true,
) {
  var showConfirmDeleteDialog by remember { mutableStateOf(false) }
  var itemToDelete by remember { mutableStateOf<String?>(null) }

  var query by rememberSaveable { mutableStateOf("") }
  var renameId by rememberSaveable { mutableStateOf<String?>(null) }
  var renameText by rememberSaveable { mutableStateOf("") }
  var pendingExportId by rememberSaveable { mutableStateOf<String?>(null) }
  var busy by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val enabled = actionsEnabled && !busy
  val filtered = remember(history, query) { ChatHistoryTools.search(history, query) }
  val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri ->
    val session = history.firstOrNull { it.sessionId == pendingExportId }
    pendingExportId = null
    if (uri != null) {
      busy = true
      scope.launch {
        try {
          withContext(Dispatchers.IO) {
            checkNotNull(session) { "Conversation no longer exists" }
            val output = context.contentResolver.openOutputStream(uri) ?: error("Cannot open export")
            output.bufferedWriter(Charsets.UTF_8).use { it.write(ChatHistoryTools.markdown(session)) }
          }
          Toast.makeText(context, "Chat exported", Toast.LENGTH_SHORT).show()
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { error = "Could not export this chat. Choose another destination and try again." }
        finally { busy = false }
      }
    }
  }

  Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(R.string.chat_history_title),
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
      )
      IconButton(onClick = onDismissed) {
        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cd_close_icon))
      }
    }

    Button(onClick = onNewChatClicked, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
      Icon(Icons.Rounded.AddComment, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.size(8.dp))
      Text(stringResource(R.string.new_chat))
    }

    OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true,
      label = { Text("Search chats") }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
    error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    if (busy) Text("Saving…", style = MaterialTheme.typography.bodySmall)

    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(R.string.recents),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      if (history.isNotEmpty()) {
        TextButton(onClick = { showConfirmDeleteDialog = true }, enabled = enabled) {
          Text(stringResource(R.string.clear_all))
        }
      }
    }

    if (filtered.isEmpty()) {
      Text(
        text = if (history.isEmpty()) stringResource(R.string.chat_history_empty) else "No matching chats",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
      )
    } else {
      LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filtered, key = { it.sessionId }) { session ->
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable(enabled = enabled) { onHistoryItemClicked(session.sessionId) }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = session.title.ifBlank { stringResource(R.string.new_chat) },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = session.subtitle(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Box {
              var menu by remember { mutableStateOf(false) }
              IconButton(onClick = { menu = true }, enabled = enabled) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Actions for ${session.title}")
              }
              DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("Rename") }, onClick = {
                  menu = false; renameId = session.sessionId; renameText = session.title; error = null
                })
                DropdownMenuItem(text = { Text("Export Markdown") }, onClick = {
                  menu = false; pendingExportId = session.sessionId; error = null
                  try { exporter.launch("AURA-chat-${session.timestampMs}.md") }
                  catch (_: Exception) { pendingExportId = null; error = "No document picker is available." }
                })
                DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; itemToDelete = session.sessionId })
              }
            }
          }
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(
        Icons.Rounded.Info,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(16.dp),
      )
      Text(
        stringResource(R.string.chat_history_local_notice),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }

  if (renameId != null) {
    AlertDialog(onDismissRequest = { if (!busy) renameId = null },
      title = { Text("Rename chat") },
      text = { OutlinedTextField(value = renameText, onValueChange = { renameText = it.take(80) },
        label = { Text("Chat name") }, singleLine = true, enabled = !busy) },
      confirmButton = { TextButton(enabled = enabled && renameText.isNotBlank(), onClick = {
        val id = renameId ?: return@TextButton
        busy = true
        scope.launch {
          try { onHistoryItemRenamed(id, renameText.trim()); renameId = null; error = null }
          catch (e: CancellationException) { throw e }
          catch (_: Exception) { error = "Could not rename this chat. Please try again."; renameId = null }
          finally { busy = false }
        }
      }) { Text("Save") } },
      dismissButton = { TextButton(enabled = !busy, onClick = { renameId = null }) { Text("Cancel") } })
  }

  if (showConfirmDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showConfirmDeleteDialog = false },
      title = { Text(stringResource(R.string.clear_history_dialog_title)) },
      text = { Text(stringResource(R.string.clear_history_dialog_content)) },
      confirmButton = {
        Button(
          onClick = {
            showConfirmDeleteDialog = false
            onHistoryItemsDeleteAll()
          }
        ) {
          Text(stringResource(R.string.ok))
        }
      },
      dismissButton = {
        TextButton(onClick = { showConfirmDeleteDialog = false }) {
          Text(stringResource(R.string.cancel))
        }
      },
    )
  }

  if (itemToDelete != null) {
    AlertDialog(
      onDismissRequest = { itemToDelete = null },
      title = { Text(stringResource(R.string.delete_chat_dialog_title)) },
      text = { Text(stringResource(R.string.delete_chat_dialog_content)) },
      confirmButton = {
        Button(
          onClick = {
            itemToDelete?.let(onHistoryItemDeleted)
            itemToDelete = null
          }
        ) {
          Text(stringResource(R.string.ok))
        }
      },
      dismissButton = {
        TextButton(onClick = { itemToDelete = null }) { Text(stringResource(R.string.cancel)) }
      },
    )
  }
}

private fun ChatSessionProto.subtitle(): String {
  val date = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestampMs))
  val model = originalModel.ifBlank { "Model" }
  val mode = if (providerId == "openrouter") "Online" else "On device"
  return "$mode · $model · $date"
}
