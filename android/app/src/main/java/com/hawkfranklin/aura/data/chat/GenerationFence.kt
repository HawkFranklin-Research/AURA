package com.hawkfranklin.aura.data.chat

import java.util.concurrent.atomic.AtomicLong

/** Prevent late native callbacks from modifying a newly selected/restored session. */
class GenerationFence {
  private val value = AtomicLong()
  fun next(): Long = value.incrementAndGet()
  fun accepts(generation: Long): Boolean = value.get() == generation
  fun invalidate() { value.incrementAndGet() }
}
