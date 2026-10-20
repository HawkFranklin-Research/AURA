package com.hawkfranklin.aura.ui.openrouter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hawkfranklin.aura.data.Model
import com.hawkfranklin.aura.data.Task
import com.hawkfranklin.aura.data.openrouter.OpenRouterModel
import com.hawkfranklin.aura.ui.common.ModelPicker
import com.hawkfranklin.aura.ui.modelmanager.ModelManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatModelPicker(
  task: Task,
  modelManager: ModelManagerViewModel,
  online: OpenRouterChatViewModel,
  onLocal: (Model) -> Unit,
  onOnline: (OpenRouterModel) -> Unit,
  onDismiss: () -> Unit,
) {
  val state by online.state.collectAsState()
  val connection by online.auth.state.collectAsState()
  var showConnect by remember { mutableStateOf(false) }
  LaunchedEffect(connection.connected) { if (connection.connected) online.refreshModels() }
  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
      Text("On this device", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
      Text("Download once. Chat offline.", modifier = Modifier.padding(horizontal = 16.dp))
      ModelPicker(task = task, modelManagerViewModel = modelManager, onModelSelected = onLocal)
      HorizontalDivider(Modifier.padding(vertical = 12.dp))
      Text("Online · OpenRouter", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
      Text("Free models · Internet required", modifier = Modifier.padding(16.dp))
      Row(Modifier.padding(horizontal = 8.dp)) {
        TextButton(onClick = { showConnect = true }) { Text(if (connection.connected) "Manage connection" else "Connect OpenRouter") }
        TextButton(onClick = { online.refreshModels(true) }, enabled = connection.connected && !state.loadingModels) { Text("Refresh") }
      }
      if (state.loadingModels) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp))
      state.notice?.let { Text(it, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall) }
      state.models.forEach { model ->
        Column(Modifier.fillMaxWidth().clickable {
          if (connection.connected) onOnline(model) else showConnect = true
        }.padding(horizontal = 20.dp, vertical = 12.dp)) {
          Text(model.name, style = MaterialTheme.typography.bodyLarge)
          Text(if (model.id == "openrouter/free") "Automatically selects an eligible free model" else model.id,
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }
  if (showConnect) OpenRouterConnectSheet(onDismiss = { showConnect = false })
}
