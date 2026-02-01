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

package com.hawkfranklin.aura.ui.common.tos

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hawkfranklin.aura.R

/** A composable for Terms of Service dialog, shown once when app is launched. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TosDialog(onTosAccepted: () -> Unit, viewingMode: Boolean = false) {
  var step by remember { mutableStateOf(0) }
  val pages =
    listOf(
      Pair(
        "Research Notice",
        "AURA is a private, on-device AI tool intended for research and internal use. " +
          "By continuing, you acknowledge outputs may be inaccurate or unsafe and you are " +
          "responsible for how you use them.",
      ),
      Pair(
        "Local + Offline",
        "Models run on your device. Once downloaded, no internet connection is required " +
          "for inference, and the app remains ad-free.",
      ),
    )

  Dialog(
    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    onDismissRequest = { if (viewingMode) onTosAccepted() },
  ) {
    Card(shape = RoundedCornerShape(28.dp)) {
      Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        // Title.
        val titleColor = MaterialTheme.colorScheme.onSurface
        BasicText(
          stringResource(R.string.tos_dialog_title),
          modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
          style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
          color = { titleColor },
          maxLines = 1,
          autoSize =
            TextAutoSize.StepBased(minFontSize = 16.sp, maxFontSize = 24.sp, stepSize = 1.sp),
        )

        AnimatedContent(
          targetState = step,
          transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
          label = "OnboardingContent",
        ) { pageIndex ->
          val page = pages[pageIndex]
          Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
              page.first,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Start,
            )
            Text(
              page.second,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
          horizontalArrangement = Arrangement.Center,
        ) {
          pages.forEachIndexed { index, _ ->
            val width = if (index == step) 24.dp else 8.dp
            val color =
              if (index == step) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.outline
            androidx.compose.foundation.layout.Box(
              modifier =
                Modifier.padding(horizontal = 3.dp)
                  .width(width)
                  .height(6.dp)
                  .background(color = color, shape = RoundedCornerShape(999.dp)),
            )
          }
        }

        // Accept button.
        Button(
          onClick = {
            if (step < pages.lastIndex) {
              step += 1
            } else {
              onTosAccepted()
            }
          },
          modifier = Modifier.padding(top = 20.dp, bottom = 24.dp).align(Alignment.End),
        ) {
          val label =
            if (step < pages.lastIndex) "Next"
            else stringResource(if (viewingMode) R.string.close else R.string.tos_dialog_view_accept_button_label)
          Text(label)
        }
      }
    }
  }
}
