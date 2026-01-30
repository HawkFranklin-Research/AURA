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

package com.hawkfranklin.aura.ui.home

// import androidx.compose.ui.tooling.preview.Preview
// import com.hawkfranklin.aura.ui.theme.GalleryTheme
// import com.hawkfranklin.aura.ui.preview.PreviewModelManagerViewModel
import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import com.hawkfranklin.aura.GalleryTopAppBar
import com.hawkfranklin.aura.R
import com.hawkfranklin.aura.data.AppBarAction
import com.hawkfranklin.aura.data.AppBarActionType
import com.hawkfranklin.aura.data.BuiltInTaskId
import com.hawkfranklin.aura.data.Category
import com.hawkfranklin.aura.data.CategoryInfo
import com.hawkfranklin.aura.data.ConfigKeys
import com.hawkfranklin.aura.data.DEFAULT_TEMPERATURE
import com.hawkfranklin.aura.data.Task
import com.hawkfranklin.aura.firebaseAnalytics
import com.hawkfranklin.aura.proto.ImportedModel
import com.hawkfranklin.aura.ui.common.RevealingText
import com.hawkfranklin.aura.ui.common.SwipingText
import com.hawkfranklin.aura.ui.common.TaskIcon
import com.hawkfranklin.aura.ui.common.rememberDelayedAnimationProgress
import com.hawkfranklin.aura.ui.common.tos.TosDialog
import com.hawkfranklin.aura.ui.common.tos.TosViewModel
import com.hawkfranklin.aura.ui.modelmanager.ModelManagerViewModel
import com.hawkfranklin.aura.ui.theme.customColors
import com.hawkfranklin.aura.ui.theme.homePageTitleStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "AGHomeScreen"
private const val TASK_COUNT_ANIMATION_DURATION = 250
private const val ANIMATION_INIT_DELAY = 0L
private const val TOP_APP_BAR_ANIMATION_DURATION = 600
private const val TITLE_FIRST_LINE_ANIMATION_DURATION = 600
private const val TITLE_SECOND_LINE_ANIMATION_DURATION = 600
private const val TITLE_SECOND_LINE_ANIMATION_DURATION2 = 800
private const val TITLE_SECOND_LINE_ANIMATION_START =
  ANIMATION_INIT_DELAY + (TITLE_FIRST_LINE_ANIMATION_DURATION * 0.5).toInt()
private const val TASK_LIST_ANIMATION_START = TITLE_SECOND_LINE_ANIMATION_START + 110
private const val TASK_CARD_ANIMATION_DELAY_OFFSET = 100
private const val TASK_CARD_ANIMATION_DURATION = 600
private const val CONTENT_COMPOSABLES_ANIMATION_DURATION = 1200
private const val CONTENT_COMPOSABLES_OFFSET_Y = 16

/** Navigation destination data */
object HomeScreenDestination {
  @StringRes val titleRes = R.string.app_name
}

private val PREDEFINED_CATEGORY_ORDER = listOf(Category.LLM.id, Category.EXPERIMENTAL.id)

private val PREDEFINED_LLM_TASK_ORDER =
  listOf(
    BuiltInTaskId.LLM_CHAT,
    BuiltInTaskId.LLM_ASK_IMAGE,
    BuiltInTaskId.LLM_PROMPT_LAB,
  )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  modelManagerViewModel: ModelManagerViewModel,
  tosViewModel: TosViewModel,
  navigateToTaskScreen: (Task) -> Unit,
  enableAnimation: Boolean,
  modifier: Modifier = Modifier,
) {
  val uiState by modelManagerViewModel.uiState.collectAsState()
  var showSettingsDialog by remember { mutableStateOf(false) }
  var showUnsupportedFileTypeDialog by remember { mutableStateOf(false) }
  var showUnsupportedWebModelDialog by remember { mutableStateOf(false) }
  var showTosDialog by remember { mutableStateOf(!tosViewModel.getIsTosAccepted()) }
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  val tasks = uiState.tasks
  val categoryMap: Map<String, CategoryInfo> =
    remember(tasks) { tasks.associateBy { it.category.id }.mapValues { it.value.category } }
  val tasksByCategories: Map<String, List<Task>> =
    remember(tasks) {
      val groupedTasks = tasks.groupBy { it.category.id }
      val groupedSortedTasks: MutableMap<String, List<Task>> = mutableMapOf()
      // Sort the tasks in categories by pre-defined order. Sort other tasks by label.
      for (categoryId in groupedTasks.keys) {
        val sortedTasks =
          groupedTasks[categoryId]!!.sortedWith { a, b ->
            if (categoryId == Category.LLM.id) {
              val order: List<String> =
                when (categoryId) {
                  Category.LLM.id -> PREDEFINED_LLM_TASK_ORDER
                  else -> listOf()
                }
              val indexA = order.indexOf(a.id)
              val indexB = order.indexOf(b.id)
              if (indexA != -1 && indexB != -1) {
                indexA.compareTo(indexB)
              } else if (indexA != -1) {
                -1
              } else if (indexB != -1) {
                1
              } else {
                val ca = categoryMap[a.id]!!
                val cb = categoryMap[b.id]!!
                val caLabel = getCategoryLabel(context = context, category = ca)
                val cbLabel = getCategoryLabel(context = context, category = cb)
                caLabel.compareTo(cbLabel)
              }
            } else {
              a.label.compareTo(b.label)
            }
          }
        for ((index, task) in sortedTasks.withIndex()) {
          task.index = index
        }
        groupedSortedTasks[categoryId] = sortedTasks
      }
      groupedSortedTasks
    }
  val sortedCategories =
    remember(categoryMap) {
      categoryMap.keys
        .toList()
        .sortedWith { a, b ->
          val indexA = PREDEFINED_CATEGORY_ORDER.indexOf(a)
          val indexB = PREDEFINED_CATEGORY_ORDER.indexOf(b)
          // Check if both categories are in the predefined order
          if (indexA != -1 && indexB != -1) {
            indexA.compareTo(indexB)
          }
          // Check if only category 'a' is in the predefined order
          else if (indexA != -1) {
            -1
          }
          // Check if only category 'b' is in the predefined order
          else if (indexB != -1) {
            1
          }
          // If neither is in the predefined order, sort by label
          else {
            val ca = categoryMap[a]!!
            val cb = categoryMap[b]!!
            val caLabel = getCategoryLabel(context = context, category = ca)
            val cbLabel = getCategoryLabel(context = context, category = cb)
            caLabel.compareTo(cbLabel)
          }
        }
        .map { categoryMap[it]!! }
    }

  // Model import flow removed for AURA.

  // Show home screen content when TOS has been accepted.
  if (!showTosDialog) {
    // The code below manages the display of the model allowlist loading indicator with a debounced
    // delay. It ensures that a progress indicator is only shown if the loading operation
    // (represented by `uiState.loadingModelAllowlist`) takes longer than 200 milliseconds.
    // If the loading completes within 200ms, the indicator is never shown,
    // preventing a "flicker" and improving the perceived responsiveness of the UI.
    // The `loadingModelAllowlistDelayed` state is used to control the actual
    // visibility of the indicator based on this debounced logic.
    var loadingModelAllowlistDelayed by remember { mutableStateOf(false) }
    // This effect runs whenever uiState.loadingModelAllowlist changes
    LaunchedEffect(uiState.loadingModelAllowlist) {
      if (uiState.loadingModelAllowlist) {
        // If loading starts, wait for 200ms
        delay(200)
        // After 200ms, check if loadingModelAllowlist is still true
        if (uiState.loadingModelAllowlist) {
          loadingModelAllowlistDelayed = true
        }
      } else {
        // If loading finishes, immediately hide the indicator
        loadingModelAllowlistDelayed = false
      }
    }

    // Label and spinner to show when in the process of loading model allowlist.
    if (loadingModelAllowlistDelayed) {
      Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        CircularProgressIndicator(
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
          strokeWidth = 3.dp,
          modifier = Modifier.padding(end = 8.dp).size(20.dp),
        )
        Text(
          stringResource(R.string.loading_model_list),
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
    // Main UI when allowlist is done loading.
    if (!loadingModelAllowlistDelayed && !uiState.loadingModelAllowlist) {
      Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
          // Top bar animation:
          //
          // Fade in and move down at the same time.
          val progress =
            if (!enableAnimation) 1f
            else
              rememberDelayedAnimationProgress(
                initialDelay = ANIMATION_INIT_DELAY - 50,
                animationDurationMs = TOP_APP_BAR_ANIMATION_DURATION,
                animationLabel = "top bar",
              )
          Box(
            modifier =
              Modifier.graphicsLayer {
                alpha = progress
                translationY = ((-16).dp * (1 - progress)).toPx()
              }
          ) {
            GalleryTopAppBar(
              title = stringResource(HomeScreenDestination.titleRes),
              rightAction =
                AppBarAction(
                  actionType = AppBarActionType.APP_SETTING,
                  actionFn = { showSettingsDialog = true },
                ),
            )
          }
        },
        floatingActionButton = {},
      ) { innerPadding ->
        // Outer box for coloring the background edge to edge.
        Box(
          contentAlignment = Alignment.TopCenter,
          modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
          // Inner box to hold content.
          Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
          ) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
              var selectedCategoryIndex by remember { mutableIntStateOf(0) }

              // App title and intro text.
              Column(
                modifier =
                  Modifier.padding(horizontal = 40.dp, vertical = 48.dp).semantics(
                    mergeDescendants = true
                  ) {},
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                AppTitle(enableAnimation = enableAnimation)
                IntroText(enableAnimation = enableAnimation)
              }

              // Tab header for categories.
              //
              // synchronizes the `pagerState` and the `selectedCategoryIndex` to ensure that
              //  both the tab header and the task list always show the correct category and page.
              val pagerState = rememberPagerState(pageCount = { sortedCategories.size })
              LaunchedEffect(pagerState.settledPage) {
                selectedCategoryIndex = pagerState.settledPage
              }
              if (sortedCategories.size > 1) {
                CategoryTabHeader(
                  sortedCategories = sortedCategories,
                  selectedIndex = selectedCategoryIndex,
                  enableAnimation = enableAnimation,
                  onCategorySelected = { index ->
                    selectedCategoryIndex = index
                    scope.launch { pagerState.animateScrollToPage(page = index) }
                  },
                )
              }

              // Task list in a horizontal pager. Each page shows the list of tasks for the
              // category.
              TaskList(
                pagerState = pagerState,
                sortedCategories = sortedCategories,
                tasksByCategories = tasksByCategories,
                enableAnimation = enableAnimation,
                navigateToTaskScreen = { task ->
                  navigateToTaskScreen(task)
                },
              )
            }

            SnackbarHost(
              hostState = snackbarHostState,
              modifier = Modifier.align(alignment = Alignment.BottomCenter).padding(bottom = 32.dp),
            )
          }
        }
      }
    }
  }

  // Show TOS dialog for users to accept.
  if (showTosDialog) {
    TosDialog(
      onTosAccepted = {
        showTosDialog = false
        tosViewModel.acceptTos()
      }
    )
  }

  // Settings dialog.
  if (showSettingsDialog) {
    SettingsDialog(
      curThemeOverride = modelManagerViewModel.readThemeOverride(),
      modelManagerViewModel = modelManagerViewModel,
      onDismissed = { showSettingsDialog = false },
    )
  }


  // Model import UI removed for AURA.

  // Alert dialog for unsupported file type.
  if (showUnsupportedFileTypeDialog) {
    AlertDialog(
      icon = {
        Icon(
          Icons.Rounded.Error,
          contentDescription = stringResource(R.string.cd_error),
          tint = MaterialTheme.colorScheme.error,
        )
      },
      onDismissRequest = { showUnsupportedFileTypeDialog = false },
      title = { Text("Unsupported file type") },
      text = { Text("Only \".task\" or \".litertlm\" file type is supported.") },
      confirmButton = {
        Button(onClick = { showUnsupportedFileTypeDialog = false }) {
          Text(stringResource(R.string.ok))
        }
      },
    )
  }

  // Alert dialog for unsupported web model.
  if (showUnsupportedWebModelDialog) {
    AlertDialog(
      icon = {
        Icon(
          Icons.Rounded.Error,
          contentDescription = stringResource(R.string.cd_error),
          tint = MaterialTheme.colorScheme.error,
        )
      },
      onDismissRequest = { showUnsupportedWebModelDialog = false },
      title = { Text("Unsupported model type") },
      text = { Text("Looks like the model is a web-only model and is not supported by the app.") },
      confirmButton = {
        Button(onClick = { showUnsupportedWebModelDialog = false }) {
          Text(stringResource(R.string.ok))
        }
      },
    )
  }

  if (uiState.loadingModelAllowlistError.isNotEmpty()) {
    AlertDialog(
      icon = {
        Icon(
          Icons.Rounded.Error,
          contentDescription = stringResource(R.string.cd_error),
          tint = MaterialTheme.colorScheme.error,
        )
      },
      title = { Text(uiState.loadingModelAllowlistError) },
      text = { Text("Please check your internet connection and try again later.") },
      onDismissRequest = { modelManagerViewModel.loadModelAllowlist() },
      confirmButton = {
        TextButton(onClick = { modelManagerViewModel.loadModelAllowlist() }) { Text("Retry") }
      },
      dismissButton = {
        TextButton(onClick = { modelManagerViewModel.clearLoadModelAllowlistError() }) {
          Text("Cancel")
        }
      },
    )
  }
}

@Composable
private fun AppTitle(enableAnimation: Boolean) {
  val titleText = stringResource(R.string.app_name)
  val screenWidthInDp = LocalConfiguration.current.screenWidthDp.dp
  val fontSize = with(LocalDensity.current) { (screenWidthInDp.toPx() * 0.12f).toSp() }
  val titleStyle = homePageTitleStyle.copy(fontSize = fontSize, lineHeight = fontSize)

  Box(modifier = Modifier.clearAndSetSemantics {}) {
    RevealingText(
      text = titleText,
      style =
        titleStyle.copy(
          brush = linearGradient(colors = MaterialTheme.customColors.appTitleGradientColors)
        ),
      animationDelay = if (enableAnimation) ANIMATION_INIT_DELAY else 0,
      animationDurationMs = if (enableAnimation) TITLE_SECOND_LINE_ANIMATION_DURATION2 else 0,
    )
  }
}

@Composable
private fun IntroText(enableAnimation: Boolean) {
  val url = "https://huggingface.co/litert-community"
  val linkColor = MaterialTheme.customColors.linkColor
  val uriHandler = LocalUriHandler.current

  // Intro text animation:
  //
  // fade in + slide up.
  val progress =
    if (!enableAnimation) 1f
    else
      rememberDelayedAnimationProgress(
        initialDelay = TITLE_SECOND_LINE_ANIMATION_START,
        animationDurationMs = CONTENT_COMPOSABLES_ANIMATION_DURATION,
        animationLabel = "intro text animation",
      )

  val introText = buildAnnotatedString {
    append("${stringResource(R.string.app_intro)} ")
    // TODO: Consolidate the link clicking logic into ui/common/ClickableLink.kt.
    withLink(
      link =
        LinkAnnotation.Url(
          url = url,
          styles =
            TextLinkStyles(
              style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
            ),
          linkInteractionListener = { _ ->
            firebaseAnalytics?.logEvent("resource_link_click", bundleOf("link_destination" to url))
            uriHandler.openUri(url)
          },
        )
    ) {
      append(stringResource(R.string.litert_community_label))
    }
  }
  Text(
    introText,
    style = MaterialTheme.typography.bodyMedium,
    modifier =
      Modifier.graphicsLayer {
        alpha = progress
        translationY = (CONTENT_COMPOSABLES_OFFSET_Y.dp * (1 - progress)).toPx()
      },
  )
}

@Composable
private fun CategoryTabHeader(
  sortedCategories: List<CategoryInfo>,
  selectedIndex: Int,
  enableAnimation: Boolean,
  onCategorySelected: (Int) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  val progress =
    if (!enableAnimation) 1f
    else
      rememberDelayedAnimationProgress(
        initialDelay = TASK_LIST_ANIMATION_START,
        animationDurationMs = CONTENT_COMPOSABLES_ANIMATION_DURATION,
        animationLabel = "task card animation",
      )

  LazyRow(
    state = listState,
    modifier =
      Modifier.fillMaxWidth().padding(bottom = 32.dp).graphicsLayer {
        alpha = progress
        translationY = (CONTENT_COMPOSABLES_OFFSET_Y.dp * (1 - progress)).toPx()
      },
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item(key = "spacer_start") { Spacer(modifier = Modifier.width(8.dp)) }
    itemsIndexed(items = sortedCategories) { index, category ->
      Row(
        modifier =
          Modifier.height(40.dp)
            .clip(CircleShape)
            .background(
              color =
                if (selectedIndex == index) MaterialTheme.customColors.tabHeaderBgColor
                else Color.Transparent
            )
            .clickable {
              onCategorySelected(index)

              // Scroll to clicked item when the item is not fully inside view.
              scope.launch {
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                val targetItem =
                  visibleItems.find {
                    // +1 because the first item is the item keyed at spacer_start.
                    it.index == index + 1
                  }
                if (
                  targetItem == null ||
                    targetItem.offset < 0 ||
                    targetItem.offset + targetItem.size > listState.layoutInfo.viewportSize.width
                ) {
                  listState.animateScrollToItem(index = index)
                }
              }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Text(
          getCategoryLabel(context = context, category = category),
          modifier = Modifier.padding(horizontal = 16.dp),
          style = MaterialTheme.typography.labelLarge,
          color =
            if (selectedIndex == index) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    item(key = "spacer_end") { Spacer(modifier = Modifier.width(8.dp)) }
  }
}

@Composable
private fun TaskList(
  pagerState: PagerState,
  sortedCategories: List<CategoryInfo>,
  tasksByCategories: Map<String, List<Task>>,
  enableAnimation: Boolean,
  navigateToTaskScreen: (Task) -> Unit,
) {
  // Model list animation:
  //
  // 1.  Slide Up: The entire column of task cards translates upwards,
  // 2.  Fade in one by one: The task card fade in one by one. See TaskCard for details.
  val progress =
    if (!enableAnimation) 1f
    else
      rememberDelayedAnimationProgress(
        initialDelay = TASK_LIST_ANIMATION_START,
        animationDurationMs = CONTENT_COMPOSABLES_ANIMATION_DURATION,
        animationLabel = "task card animation",
      )

  // Tracks when the initial animation is done.
  //
  var initialAnimationDone by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    // Use 5 iterations to make sure all visible task cards are animated.
    delay(((TASK_CARD_ANIMATION_DURATION + TASK_CARD_ANIMATION_DELAY_OFFSET) * 5).toLong())
    initialAnimationDone = true
  }

  HorizontalPager(
    state = pagerState,
    verticalAlignment = Alignment.Top,
    contentPadding = PaddingValues(horizontal = 20.dp),
  ) { pageIndex ->
    val tasks = tasksByCategories[sortedCategories[pageIndex].id]!!
    Column(
      modifier =
        Modifier.fillMaxWidth().padding(4.dp).graphicsLayer {
          translationY = (CONTENT_COMPOSABLES_OFFSET_Y.dp * (1 - progress)).toPx()
        },
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      var index = 0
      for (task in tasks) {
        TaskCard(
          task = task,
          index = index,
          animate = (pageIndex == 0 || pageIndex == 1) && !initialAnimationDone && enableAnimation,
          onClick = { navigateToTaskScreen(task) },
          modifier = Modifier.fillMaxWidth(),
        )
        index++
      }
    }
  }
}

@Composable
private fun TaskCard(
  task: Task,
  index: Int,
  animate: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // Observes the model count and updates the model count label with a fade-in/fade-out animation
  // whenever the count changes.
  val modelCount by remember {
    derivedStateOf {
      val trigger = task.updateTrigger.value
      if (trigger >= 0) {
        task.models.size
      } else {
        0
      }
    }
  }
  val modelCountLabel by remember {
    derivedStateOf {
      when (modelCount) {
        1 -> "1 Model"
        else -> "%d Models".format(modelCount)
      }
    }
  }
  var curModelCountLabel by remember { mutableStateOf("") }
  var modelCountLabelVisible by remember { mutableStateOf(true) }

  LaunchedEffect(modelCountLabel) {
    if (curModelCountLabel.isEmpty()) {
      curModelCountLabel = modelCountLabel
    } else {
      modelCountLabelVisible = false
      delay(TASK_COUNT_ANIMATION_DURATION.toLong())
      curModelCountLabel = modelCountLabel
      modelCountLabelVisible = true
    }
  }

  // Task card animation:
  //
  // This animation makes the task cards appear with a delayed fade-in effect. Each card will become
  // visible sequentially, starting after an initial delay and then with an additional offset for
  // subsequent cards.
  val progress =
    if (animate)
      rememberDelayedAnimationProgress(
        initialDelay = TASK_LIST_ANIMATION_START + index * TASK_CARD_ANIMATION_DELAY_OFFSET,
        animationDurationMs = TASK_CARD_ANIMATION_DURATION,
        animationLabel = "task card animation",
      )
    else 1f

  val cbTask = stringResource(R.string.cd_task_card, task.label, task.models.size)
  Card(
    modifier =
      modifier
        .clip(RoundedCornerShape(24.dp))
        .clickable(onClick = onClick)
        .graphicsLayer { alpha = progress }
        .semantics { contentDescription = cbTask },
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.customColors.taskCardBgColor),
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(24.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      // Title and model count
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            task.label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
          )
          if (task.experimental) {
            Icon(
              painter = painterResource(R.drawable.ic_experiment),
              contentDescription = "Experimental",
              modifier = Modifier.size(20.dp).padding(start = 4.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Text(
          curModelCountLabel,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.clearAndSetSemantics {},
        )
      }

      // Icon.
      TaskIcon(task = task, width = 40.dp)
    }
  }
}

// Model import helpers removed for AURA.

private fun getCategoryLabel(context: Context, category: CategoryInfo): String {
  val stringRes = category.labelStringRes
  val label = category.label
  if (stringRes != null) {
    return context.getString(stringRes)
  } else if (label != null) {
    return label
  }
  return context.getString(R.string.category_unlabeled)
}

// @Preview
// @Composable
// fun HomeScreenPreview() {
//   GalleryTheme {
//     HomeScreen(
//       modelManagerViewModel = PreviewModelManagerViewModel(context = LocalContext.current),
//       navigateToTaskScreen = {},
//     )
//   }
// }
