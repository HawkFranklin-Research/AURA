package com.hawkfranklin.aura.ui.openrouter

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hawkfranklin.aura.data.openrouter.OpenRouterAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OpenRouterConnectionViewModel @Inject constructor(val auth: OpenRouterAuthRepository) : ViewModel() {
  private val _error = MutableStateFlow<String?>(null)
  val error = _error.asStateFlow()
  fun connect(context: Context, manual: Boolean = false) {
    _error.value = null
    viewModelScope.launch {
      try { CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(auth.begin(manual))) }
      catch (_: Exception) { _error.value = "Could not start secure browser sign-in. Cancel and try again." }
    }
  }
  fun openReturnLinkSettings(context: Context) {
    runCatching { context.startActivity(OpenRouterAppLinks.settingsIntent(context)) }
      .onFailure { _error.value = "Open Android app settings, select AURA, then Open by default to enable supported links." }
  }
  fun cancel() {
    viewModelScope.launch { runCatching { auth.cancel() }.onFailure { _error.value = "Could not cancel sign-in. Try again." } }
  }
  fun disconnect() {
    viewModelScope.launch { runCatching { auth.disconnect() }.onFailure { _error.value = "Could not remove the local connection. Try again." } }
  }
}
