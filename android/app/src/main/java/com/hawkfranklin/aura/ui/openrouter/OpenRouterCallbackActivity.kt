package com.hawkfranklin.aura.ui.openrouter

import androidx.activity.ComponentActivity
import android.content.Intent
import android.os.Bundle
import com.hawkfranklin.aura.MainActivity
import com.hawkfranklin.aura.data.openrouter.OpenRouterAuthRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Only this activity receives HTTPS callbacks and explicit website return links. Never forward its URI to navigation/logs. */
@AndroidEntryPoint
class OpenRouterCallbackActivity : ComponentActivity() {
  @Inject lateinit var auth: OpenRouterAuthRepository
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    intent.data?.toString()?.let(auth::acceptCallback)
    intent.data = null
    startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
    finish()
  }
}
