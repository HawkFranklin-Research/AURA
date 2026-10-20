package com.hawkfranklin.aura.data.openrouter

import com.hawkfranklin.aura.data.chat.GenerationFence
import org.junit.Assert.*
import org.junit.Test

class GenerationFenceTest {
  @Test fun `stopped and replaced native generations cannot update new sessions`() {
    val fence = GenerationFence()
    val old = fence.next()
    assertTrue(fence.accepts(old))
    fence.invalidate()
    assertFalse(fence.accepts(old))
    val current = fence.next()
    assertTrue(fence.accepts(current))
    assertFalse(fence.accepts(old))
    fence.invalidate() // restored/new chat
    assertFalse(fence.accepts(current))
  }
}
