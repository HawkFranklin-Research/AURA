package com.hawkfranklin.aura.ui.openrouter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hawkfranklin.aura.data.openrouter.OPENROUTER_CALLBACK_HOST
import com.hawkfranklin.aura.data.openrouter.OPENROUTER_PRIVACY

@Composable
fun OpenRouterConnectSheet(onDismiss: () -> Unit, viewModel: OpenRouterConnectionViewModel = hiltViewModel()) {
  val state by viewModel.auth.state.collectAsState()
  val error by viewModel.error.collectAsState()
  val context = LocalContext.current
  val links = LocalUriHandler.current
  var code by remember { mutableStateOf("") }
  var returnLinkReady by remember { mutableStateOf(OpenRouterAppLinks.canReturnToApp(context)) }
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner, context) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) returnLinkReady = OpenRouterAppLinks.canReturnToApp(context)
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (state.connected) "OpenRouter connection" else "Use free online models") },
    text = {
      Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Connect your own OpenRouter account in your browser. AURA does not receive your OpenRouter password.")
        Text("OpenRouter will show $OPENROUTER_CALLBACK_HOST as the return website. AURA exchanges the authorization code on your phone; the website does not receive your API key.")
        if (!returnLinkReady) {
          Text("Automatic return from $OPENROUTER_CALLBACK_HOST is not enabled for this installation. Enable supported links, use the website’s Return to AURA button after authorization, or choose the authorization-code option below.")
          TextButton(onClick = { viewModel.openReturnLinkSettings(context) }) { Text("Enable return to AURA") }
        }
        Text("Online messages and the current online conversation context are sent to OpenRouter and an inference provider. Downloaded models continue to work on this device without an OpenRouter account.")
        Text("AURA requests free endpoints with zero price limits. If none are available, the request fails rather than switching to paid endpoints. Free requests have limits and are served by third-party providers under their respective policies.")
        Text("OpenRouter may retain request metadata. Its optional content logging and account settings also apply. AURA cannot control or independently guarantee third-party data practices.")
        Text("HawkFranklin Research operates AURA independently and does not operate OpenRouter or its model providers.")
        TextButton(onClick = { links.openUri(OPENROUTER_PRIVACY) }) { Text("OpenRouter privacy policy") }
        TextButton(onClick = { links.openUri("https://openrouter.ai/docs/guides/privacy/data-collection") }) { Text("Data collection and account controls") }
        TextButton(onClick = { links.openUri("https://openrouter.ai/providers") }) { Text("Provider privacy policies") }
        state.message?.let { Text(it) }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.exchanging) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (state.pending && !state.exchanging) {
          Text("Complete authorization in your browser. If the return website opens, tap Return to AURA there. Sign-in expires after 10 minutes.")
          if (state.manual) {
            OutlinedTextField(value = code, onValueChange = { code = it.take(4096) }, label = { Text("Authorization code") },
              visualTransformation = PasswordVisualTransformation(), singleLine = true)
            Button(onClick = { viewModel.auth.acceptCode(code.trim()); code = "" }, enabled = code.isNotBlank()) { Text("Finish connecting") }
          }
          TextButton(onClick = { viewModel.cancel(); code = "" }) { Text("Cancel pending sign-in") }
        }
        if (!state.pending && !state.exchanging) {
          Button(onClick = { viewModel.connect(context) }) { Text(if (state.connected) "Reconnect in browser" else "Continue to OpenRouter") }
          TextButton(onClick = { viewModel.connect(context, manual = true) }) { Text("Use a browser authorization code instead") }
        }
        if (state.connected) {
          Text("Disconnect removes the key from this device. It does not revoke it on OpenRouter or delete your saved chats.")
          TextButton(onClick = { links.openUri("https://openrouter.ai/settings/keys") }) { Text("Manage or revoke access on OpenRouter") }
          TextButton(onClick = { viewModel.disconnect() }, enabled = !state.exchanging) { Text("Disconnect this device") }
        }
      }
    },
    confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
  )
}
