package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEffectManager
import com.example.audio.ToneBeatGenerator
import com.example.model.EqualizerPreset
import com.example.model.EqualizerUiState
import com.example.model.EqBand
import com.example.model.TestTrack
import com.example.model.defaultBands
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel สำหรับจัดการสถานะและตรรกะการทำงานของ Equalizer
 * รองรับภาษาไทย 100% สอดคล้องกับข้อกำหนดการออกแบบ M3
 */
class EqualizerViewModel(application: Application) : AndroidViewModel(application) {

    private val audioEffectManager = AudioEffectManager(application.applicationContext)
    private val toneBeatGenerator = ToneBeatGenerator()

    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    private var previousVolumeBeforeMute: Int = 10

    init {
        loadInitialHardwareState()
    }

    /**
     * ดึงค่าเริ่มต้นจากฮาร์ดแวร์เสียงในระบบ
     */
    private fun loadInitialHardwareState() {
        viewModelScope.launch {
            val queriedBands = audioEffectManager.queryBands().ifEmpty { defaultBands() }
            val currentVol = audioEffectManager.getMasterVolume()
            val maxVol = audioEffectManager.getMaxMasterVolume()

            _uiState.update { current ->
                current.copy(
                    bands = queriedBands,
                    masterVolume = currentVol,
                    maxVolume = maxVol,
                    isEnabled = audioEffectManager.isEnabled(),
                    statusMessage = "เชื่อมต่อระบบเสียง Audio Session 0 สำเร็จ"
                )
            }
        }
    }

    /**
     * 1. สวิตช์ Toggle "เปิด/ปิด การใช้งาน EQ"
     */
    fun toggleEqualizer(enabled: Boolean) {
        audioEffectManager.setEnabled(enabled)
        _uiState.update { it.copy(isEnabled = enabled) }
    }

    /**
     * 2. ระบบ EQ Preset (โหมดสำเร็จรูป)
     * นำค่าระดับความถี่, Bass Boost และ Virtualizer ของ Preset มาใช้งาน
     */
    fun selectPreset(preset: EqualizerPreset) {
        if (preset == EqualizerPreset.CUSTOM) {
            _uiState.update { it.copy(currentPreset = EqualizerPreset.CUSTOM) }
            return
        }

        val updatedBands = _uiState.value.bands.mapIndexed { index, band ->
            val presetLevel = preset.defaultBandLevelsMb.getOrElse(index) { 0.toShort() }
            audioEffectManager.setBandLevel(band.index, presetLevel)
            band.copy(currentLevelMb = presetLevel)
        }

        // ปรับ Bass Boost ตามโหมดสำเร็จรูป
        audioEffectManager.setBassBoostStrength(preset.defaultBassBoost)
        // ปรับ Virtualizer ตามโหมดสำเร็จรูป
        audioEffectManager.setVirtualizerStrength(preset.defaultVirtualizer)

        _uiState.update {
            it.copy(
                currentPreset = preset,
                bands = updatedBands,
                bassBoostStrength = preset.defaultBassBoost,
                virtualizerStrength = preset.defaultVirtualizer
            )
        }
    }

    /**
     * 3. ระบบปรับแต่ง EQ แบบ Custom (สไลเดอร์ปรับย่านความถี่)
     * กฎสำคัญ: "เมื่อผู้ใช้เลื่อนสไลเดอร์ย่านความถี่ใดก็ตาม ให้ระบบเปลี่ยนโหมด Preset เป็น 'กำหนดเอง (Custom)' โดยอัตโนมัติ"
     */
    fun updateBandLevel(bandIndex: Short, newLevelMb: Short) {
        audioEffectManager.setBandLevel(bandIndex, newLevelMb)

        _uiState.update { state ->
            val updatedBands = state.bands.map { band ->
                if (band.index == bandIndex) {
                    band.copy(currentLevelMb = newLevelMb)
                } else {
                    band
                }
            }
            // เปลี่ยนโหมดเป็น CUSTOM โดยอัตโนมัติเมื่อผู้ใช้เลื่อนสไลเดอร์
            state.copy(
                bands = updatedBands,
                currentPreset = EqualizerPreset.CUSTOM
            )
        }
    }

    /**
     * สไลเดอร์เพิ่มเติม: "เพิ่มเบส (Bass Boost)" (0 - 1000)
     */
    fun updateBassBoost(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        audioEffectManager.setBassBoostStrength(clamped)
        _uiState.update {
            it.copy(
                bassBoostStrength = clamped,
                currentPreset = if (it.currentPreset != EqualizerPreset.BASS_BOOST) EqualizerPreset.CUSTOM else it.currentPreset
            )
        }
    }

    /**
     * สไลเดอร์เพิ่มเติม: "จำลองมิติเสียง (Virtualizer)" (0 - 1000)
     */
    fun updateVirtualizer(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        audioEffectManager.setVirtualizerStrength(clamped)
        _uiState.update {
            it.copy(
                virtualizerStrength = clamped,
                currentPreset = EqualizerPreset.CUSTOM
            )
        }
    }

    /**
     * Master Volume Slider สำหรับปรับความดังรวมของระบบ
     */
    fun updateMasterVolume(volume: Int) {
        audioEffectManager.setMasterVolume(volume)
        _uiState.update {
            it.copy(
                masterVolume = volume,
                isMuted = (volume == 0)
            )
        }
    }

    /**
     * สลับการปิด/เปิดเสียง (Mute / Unmute)
     */
    fun toggleMute() {
        val currentState = _uiState.value
        if (currentState.isMuted) {
            val restoreVol = if (previousVolumeBeforeMute > 0) previousVolumeBeforeMute else 5
            audioEffectManager.setMasterVolume(restoreVol)
            _uiState.update { it.copy(masterVolume = restoreVol, isMuted = false) }
        } else {
            previousVolumeBeforeMute = currentState.masterVolume
            audioEffectManager.setMasterVolume(0)
            _uiState.update { it.copy(masterVolume = 0, isMuted = true) }
        }
    }

    /**
     * 4. ปุ่ม "รีเซ็ตค่ามาตรฐาน" เพื่อดึงค่าเดิมกลับมา
     * รีเซ็ตย่านความถี่ทั้งหมดเป็น 0 dB, Bass Boost 0, Virtualizer 0 และกลับสู่โหมด "ทั่วไป (Normal / Flat)"
     */
    fun resetToDefaults() {
        val resetBands = _uiState.value.bands.map { band ->
            audioEffectManager.setBandLevel(band.index, 0)
            band.copy(currentLevelMb = 0)
        }
        audioEffectManager.setBassBoostStrength(0)
        audioEffectManager.setVirtualizerStrength(0)

        _uiState.update {
            it.copy(
                currentPreset = EqualizerPreset.NORMAL,
                bands = resetBands,
                bassBoostStrength = 0,
                virtualizerStrength = 0,
                statusMessage = "รีเซ็ตค่ามาตรฐานเรียบร้อยแล้ว"
            )
        }
    }

    /**
     * สลับการเล่นเพลงตัวอย่างเพื่อทดสอบเสียงจริง
     */
    fun toggleTestAudio() {
        val isCurrentlyPlaying = _uiState.value.isTestAudioPlaying
        if (isCurrentlyPlaying) {
            toneBeatGenerator.stopPlaying()
            // คืนค่ากลับไปที่ Audio Session 0 (Global Mix)
            audioEffectManager.initEffects(sessionId = 0)
            _uiState.update {
                it.copy(
                    isTestAudioPlaying = false,
                    audioSessionId = 0,
                    statusMessage = "หยุดเสียงทดสอบ กลับสู่ Audio Session 0"
                )
            }
        } else {
            toneBeatGenerator.startPlaying(_uiState.value.selectedTestTrack) { newSessionId ->
                // ผูก AudioEffectManager เข้ากับ AudioSession ของเครื่องเล่นเสียงทดสอบโดยตรง
                audioEffectManager.initEffects(sessionId = newSessionId)
                _uiState.update {
                    it.copy(
                        isTestAudioPlaying = true,
                        audioSessionId = newSessionId,
                        statusMessage = "กำลังเล่นเสียงทดสอบบน Audio Session: $newSessionId"
                    )
                }
            }
        }
    }

    /**
     * เปลี่ยนเพลงสำหรับทดสอบเสียง
     */
    fun selectTestTrack(track: TestTrack) {
        _uiState.update { it.copy(selectedTestTrack = track) }
        if (_uiState.value.isTestAudioPlaying) {
            toneBeatGenerator.startPlaying(track) { newSessionId ->
                audioEffectManager.initEffects(sessionId = newSessionId)
                _uiState.update { it.copy(audioSessionId = newSessionId) }
            }
        }
    }

    /**
     * ตั้งค่า Audio Session ID จากภายนอก (เมื่อเปิดจากเครื่องเล่นเพลงอื่น)
     */
    fun setExternalAudioSession(sessionId: Int) {
        if (sessionId != 0 && sessionId != _uiState.value.audioSessionId) {
            audioEffectManager.initEffects(sessionId)
            _uiState.update {
                it.copy(
                    audioSessionId = sessionId,
                    statusMessage = "เชื่อมต่อเพลงภายนอก (Session ID: $sessionId)"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        toneBeatGenerator.stopPlaying()
        audioEffectManager.releaseEffects()
    }
}
