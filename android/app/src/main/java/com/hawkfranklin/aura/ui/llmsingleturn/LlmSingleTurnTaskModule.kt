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

package com.hawkfranklin.aura.ui.llmsingleturn

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.runtime.Composable
import com.hawkfranklin.aura.R
import com.hawkfranklin.aura.customtasks.common.CustomTask
import com.hawkfranklin.aura.customtasks.common.CustomTaskDataForBuiltinTask
import com.hawkfranklin.aura.data.BuiltInTaskId
import com.hawkfranklin.aura.data.Category
import com.hawkfranklin.aura.data.Model
import com.hawkfranklin.aura.data.Task
import com.hawkfranklin.aura.ui.llmchat.LlmChatModelHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

class LlmSingleTurnTask @Inject constructor() : CustomTask {
  override val task: Task =
    Task(
      id = BuiltInTaskId.LLM_PROMPT_LAB,
      label = "Prompt Lab",
      category = Category.LLM,
      icon = Icons.Outlined.AutoFixHigh,
      models = mutableListOf(),
      description = "Single-turn prompts with on-device models",
      textInputPlaceHolderRes = R.string.text_input_placeholder_llm_chat,
    )

  override fun initializeModelFn(
    context: Context,
    coroutineScope: CoroutineScope,
    model: Model,
    onDone: (String) -> Unit,
  ) {
    LlmChatModelHelper.initialize(
      context = context,
      model = model,
      supportImage = false,
      supportAudio = false,
      onDone = onDone,
    )
  }

  override fun cleanUpModelFn(
    context: Context,
    coroutineScope: CoroutineScope,
    model: Model,
    onDone: () -> Unit,
  ) {
    LlmChatModelHelper.cleanUp(model = model, onDone = onDone)
  }

  @Composable
  override fun MainScreen(data: Any) {
    val myData = data as CustomTaskDataForBuiltinTask
    LlmSingleTurnScreen(
      modelManagerViewModel = myData.modelManagerViewModel,
      navigateUp = myData.onNavUp,
    )
  }
}

@Module
@InstallIn(SingletonComponent::class) // Or another component that fits your scope
internal object LlmSingleTurnTaskModule {
  @Provides
  @IntoSet
  fun provideTask(): CustomTask {
    return LlmSingleTurnTask()
  }
}
