package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CanvasBg
import com.example.model.DefaultPresets
import com.example.model.FilterMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Shrinky Trace", appName)
  }

  @Test
  fun `default presets are loaded with charms`() {
    assertTrue("Should have at least 2 default charm presets", DefaultPresets.size >= 2)
    assertEquals("Dino Charm", DefaultPresets[0].title)
    assertEquals("Floral Kitten", DefaultPresets[1].title)
  }

  @Test
  fun `filter modes provide valid configuration`() {
    assertEquals(4, FilterMode.entries.size)
    assertEquals("Normal", FilterMode.ORIGINAL.label)
    assertEquals("High Contrast", FilterMode.HIGH_CONTRAST.label)
    assertEquals("Invert", FilterMode.INVERTED.label)
    assertEquals("B&W Outline", FilterMode.GRAYSCALE.label)
  }

  @Test
  fun `canvas background has valid color options`() {
    assertEquals(4, CanvasBg.entries.size)
    assertNotNull(CanvasBg.WHITE.color)
    assertNotNull(CanvasBg.PURE_BLACK.color)
    assertNotNull(CanvasBg.SOFT_AMBER.color)
    assertNotNull(CanvasBg.LIGHT_GRAY.color)
  }
}
