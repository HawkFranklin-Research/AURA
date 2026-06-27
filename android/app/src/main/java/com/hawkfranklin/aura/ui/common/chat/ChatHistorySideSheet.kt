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
) {
  var showConfirmDeleteDialog by remember { mutableStateOf(false) }
  var itemToDelete by remember { mutableStateOf<String?>(null) }

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

    Button(onClick = onNewChatClicked, modifier = Modifier.fillMaxWidth()) {
      Icon(Icons.Rounded.AddComment, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.size(8.dp))
      Text(stringResource(R.string.new_chat))
    }

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
        TextButton(onClick = { showConfirmDeleteDialog = true }) {
          Text(stringResource(R.string.clear_all))
        }
      }
    }

    if (history.isEmpty()) {
      Text(
        text = stringResource(R.string.chat_history_empty),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
      )
    } else {
      LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(history, key = { it.sessionId }) { session ->
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable { onHistoryItemClicked(session.sessionId) }
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
            IconButton(onClick = { itemToDelete = session.sessionId }) {
              Icon(
                Icons.Rounded.Delete,
                contentDescription = stringResource(R.string.cd_delete_input_history_entry_icon),
              )
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
  return "$model - $date"
}
