package com.hawkfranklin.aura.ui.openrouter

import android.content.Context
import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.provider.Settings
import com.hawkfranklin.aura.data.openrouter.OPENROUTER_CALLBACK_HOST

/** Read Android's association state, not whether Chrome also resolves an HTTPS intent. */
object OpenRouterAppLinks {
  fun canReturnToApp(context: Context): Boolean = runCatching {
    val manager = context.getSystemService(DomainVerificationManager::class.java)
    val state = manager.getDomainVerificationUserState(context.packageName) ?: return@runCatching false
    val host = state.hostToStateMap[OPENROUTER_CALLBACK_HOST]
    state.isLinkHandlingAllowed && (host == DomainVerificationUserState.DOMAIN_STATE_VERIFIED ||
      host == DomainVerificationUserState.DOMAIN_STATE_SELECTED)
  }.getOrDefault(false)

  fun settingsIntent(context: Context): Intent = Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
    Uri.parse("package:${context.packageName}"))
}
