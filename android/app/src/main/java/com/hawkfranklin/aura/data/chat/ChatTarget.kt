package com.hawkfranklin.aura.data.chat

import com.hawkfranklin.aura.data.Model
import com.hawkfranklin.aura.data.openrouter.OpenRouterModel

/** Online targets do not participate in local download/engine state. */
sealed interface ChatTarget {
  val id: String
  data class Local(val model: Model) : ChatTarget {
    override val id: String get() = "local:${model.name}"
  }
  data class Online(val model: OpenRouterModel) : ChatTarget {
    override val id: String get() = "openrouter:${model.id}"
  }
}
