package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.LocalSoundCatalog
import com.example.data.preset.PresetEntity
import com.example.data.sound.CustomSoundEntity
import com.example.model.SoundPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Somnia", appName)
  }

  @Test
  fun `verify catalog sounds count`() {
    assertEquals(10, LocalSoundCatalog.builtInSounds.size)
  }

  @Test
  fun `preset entity serialization roundtrip`() {
    val preset = SoundPreset(
      id = "test_1",
      name = "Night Breeze",
      isBuiltIn = false,
      soundVolumes = mapOf("rain" to 0.7f, "wind" to 0.4f, "river" to 0.5f)
    )

    val entity = PresetEntity.fromSoundPreset(preset)
    val restored = entity.toSoundPreset()

    assertEquals(preset.id, restored.id)
    assertEquals(preset.name, restored.name)
    assertEquals(0.7f, restored.soundVolumes["rain"] ?: 0f, 0.01f)
    assertEquals(0.4f, restored.soundVolumes["wind"] ?: 0f, 0.01f)
    assertEquals(0.5f, restored.soundVolumes["river"] ?: 0f, 0.01f)
  }

  @Test
  fun `custom sound entity conversion`() {
    val entity = CustomSoundEntity(
      id = "custom_test_1",
      title = "Campfire",
      iconName = "FIRE",
      filePath = "/data/user/0/app/files/custom_sounds/custom_test_1.mp3"
    )

    val soundDef = entity.toSoundDefinition()
    assertEquals("custom_test_1", soundDef.id)
    assertEquals("Campfire", soundDef.title)
    assertEquals(false, soundDef.isBuiltIn)
    assertEquals(entity.filePath, soundDef.customFilePath)
    assertNotNull(soundDef.category)
  }
}
