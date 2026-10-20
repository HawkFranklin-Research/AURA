package com.hawkfranklin.aura.data.openrouter

class ContextBudgetException(message: String) : IllegalArgumentException(message)

data class BudgetedContext(
  val messages: List<OpenRouterTurn>,
  val outputTokens: Int,
  val omittedTurns: Int,
)

/**
 * Conservative UTF-8 byte estimate, including reasoning and message overhead. This is not
 * a model tokenizer: advertised context sizes and router/provider limits can still differ.
 * Reserve output space, then remove oldest whole user/assistant turns. Never cut a message.
 */
object OnlineContextBudget {
  fun prepare(model: OpenRouterModel, messages: List<OpenRouterTurn>): BudgetedContext {
    require(messages.isNotEmpty() && messages.first().role == "user")
    val limit = (if (model.id == OpenRouterModel.FREE.id) 8192
      else model.contextLength.takeIf { it > 0 } ?: 8192).coerceAtMost(32768)
    val output = minOf(4096, limit / 4,
      model.maxCompletionTokens.takeIf { it > 0 } ?: 4096).coerceAtLeast(1)
    val inputLimit = limit.toLong() - output - 512
    val groups = mutableListOf<MutableList<OpenRouterTurn>>()
    messages.forEach { message ->
      if (message.role == "user") groups.add(mutableListOf())
      groups.last().add(message)
    }
    fun cost(group: List<OpenRouterTurn>) = group.sumOf {
      it.content.toByteArray(Charsets.UTF_8).size.toLong() +
        (it.reasoning?.toString()?.toByteArray(Charsets.UTF_8)?.size ?: 0) + 32L
    }
    val costs = groups.map(::cost)
    if (costs.last() > inputLimit) throw ContextBudgetException(
      "Your latest message is too long for this model’s context budget. Shorten it or choose a model with a larger context.")
    var total = costs.sum()
    var omitted = 0
    while (total > inputLimit && omitted < groups.lastIndex) total -= costs[omitted++]
    return BudgetedContext(groups.drop(omitted).flatten(), output, omitted)
  }
}
