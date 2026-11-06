package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.EqualizerPreset
import com.example.model.EqBand
import com.example.model.defaultBands
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    assertEquals("ปรับแต่งเสียง", appName)
  }

  @Test
  fun `verify equalizer preset titles in Thai`() {
    val normal = EqualizerPreset.NORMAL
    assertEquals("ทั่วไป (Normal / Flat)", normal.titleThai)

    val rock = EqualizerPreset.ROCK
    assertEquals("ร็อค (Rock)", rock.titleThai)

    val bass = EqualizerPreset.BASS_BOOST
    assertEquals("เบสหนัก (Bass Boost Focus)", bass.titleThai)

    val custom = EqualizerPreset.CUSTOM
    assertEquals("กำหนดเอง (Custom)", custom.titleThai)

    assertEquals(7, EqualizerPreset.selectablePresets.size)
  }

  @Test
  fun `verify eq band formatting`() {
    val band60 = EqBand(index = 0, centerFreqHz = 60, currentLevelMb = 300)
    assertEquals("60 Hz", band60.formattedFrequency)
    assertEquals("+3 dB", band60.formattedDb)

    val band14k = EqBand(index = 4, centerFreqHz = 14000, currentLevelMb = -200)
    assertEquals("14 kHz", band14k.formattedFrequency)
    assertEquals("-2 dB", band14k.formattedDb)

    val bandMid = EqBand(index = 2, centerFreqHz = 910, currentLevelMb = 0)
    assertEquals("910 Hz", bandMid.formattedFrequency)
    assertEquals("0 dB", bandMid.formattedDb)
  }

  @Test
  fun `verify default bands contain 5 standard frequencies`() {
    val bands = defaultBands()
    assertEquals(5, bands.size)
    assertEquals(60, bands[0].centerFreqHz)
    assertEquals(230, bands[1].centerFreqHz)
    assertEquals(910, bands[2].centerFreqHz)
    assertEquals(3600, bands[3].centerFreqHz)
    assertEquals(14000, bands[4].centerFreqHz)
  }
}
