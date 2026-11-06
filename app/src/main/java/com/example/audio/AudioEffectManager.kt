package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.model.EqBand
import com.example.model.defaultBands

/**
 * ตัวจัดการเอฟเฟกต์เสียงระดับระบบ (AudioEffectManager)
 * ควบคุม Native AudioEffect API ของ Android:
 * 1. android.media.audiofx.Equalizer
 * 2. android.media.audiofx.BassBoost
 * 3. android.media.audiofx.Virtualizer
 * 4. android.media.AudioManager (Master Volume)
 */
class AudioEffectManager(private val context: Context) {
    private val tag = "AudioEffectManager"

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private var currentSessionId: Int = 0
    private var isEffectEnabled: Boolean = true

    // เก็บค่าแบนด์ปัจจุบันเพื่อใช้สำรองกรณีสลับ session
    private var cachedBandLevels = mutableMapOf<Short, Short>()
    private var cachedBassBoost: Short = 0
    private var cachedVirtualizer: Short = 0

    init {
        initEffects(sessionId = 0)
    }

    /**
     * เริ่มต้นระบบ AudioEffect ด้วย Audio Session ID ที่ระบุ
     * session 0 = Global system mix (มีผลกับเพลงและเสียงในเครื่องทั้งหมด)
     */
    fun initEffects(sessionId: Int) {
        releaseEffects()
        currentSessionId = sessionId

        try {
            // แจ้งระบบว่ามีการเปิดใช้งาน Audio Effect Control Session
            broadcastSessionOpen(sessionId)

            // 1. กำหนดค่า Equalizer
            try {
                equalizer = Equalizer(1000, sessionId).apply {
                    enabled = isEffectEnabled
                }
                Log.d(tag, "เปิดใช้งาน Equalizer สำหรับ Session ID: $sessionId สำเร็จ")
            } catch (e: Exception) {
                Log.w(tag, "ไม่สามารถสร้างฮาร์ดแวร์ Equalizer บน session $sessionId: ${e.message}")
            }

            // 2. กำหนดค่า Bass Boost
            try {
                bassBoost = BassBoost(1000, sessionId).apply {
                    enabled = isEffectEnabled
                    if (strengthSupported) {
                        setStrength(cachedBassBoost)
                    }
                }
                Log.d(tag, "เปิดใช้งาน BassBoost สำหรับ Session ID: $sessionId สำเร็จ")
            } catch (e: Exception) {
                Log.w(tag, "ไม่สามารถสร้างฮาร์ดแวร์ BassBoost บน session $sessionId: ${e.message}")
            }

            // 3. กำหนดค่า Virtualizer
            try {
                virtualizer = Virtualizer(1000, sessionId).apply {
                    enabled = isEffectEnabled
                    if (strengthSupported) {
                        setStrength(cachedVirtualizer)
                    }
                }
                Log.d(tag, "เปิดใช้งาน Virtualizer สำหรับ Session ID: $sessionId สำเร็จ")
            } catch (e: Exception) {
                Log.w(tag, "ไม่สามารถสร้างฮาร์ดแวร์ Virtualizer บน session $sessionId: ${e.message}")
            }

            // นำค่าที่บันทึกไว้กลับมาตั้งค่าใน instance ใหม่
            cachedBandLevels.forEach { (band, level) ->
                setBandLevel(band, level)
            }

        } catch (e: Exception) {
            Log.e(tag, "ข้อผิดพลาดในการเริ่มต้น Audio Effects: ${e.message}", e)
        }
    }

    /**
     * สลับการเปิด/ปิด เอฟเฟกต์ทั้งหมด (Master Toggle)
     */
    fun setEnabled(enabled: Boolean) {
        isEffectEnabled = enabled
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
        } catch (e: Exception) {
            Log.e(tag, "ข้อผิดพลาดในการตั้งค่าสถานะเปิด/ปิด EQ: ${e.message}", e)
        }
    }

    /**
     * ตรวจสอบว่าเอฟเฟกต์เปิดอยู่หรือไม่
     */
    fun isEnabled(): Boolean = isEffectEnabled

    /**
     * ปรับระดับเสียงในย่านความถี่ที่กำหนด (หน่วย mB)
     */
    fun setBandLevel(band: Short, levelMb: Short) {
        cachedBandLevels[band] = levelMb
        try {
            equalizer?.let { eq ->
                val range = eq.bandLevelRange
                val clamped = levelMb.coerceIn(range[0], range[1])
                eq.setBandLevel(band, clamped)
            }
        } catch (e: Exception) {
            Log.e(tag, "ข้อผิดพลาดในการปรับระดับแบนด์ $band: ${e.message}", e)
        }
    }

    /**
     * ปรับความแรงของ Bass Boost (0 ถึง 1000)
     */
    fun setBassBoostStrength(strength: Int) {
        val s = strength.coerceIn(0, 1000).toShort()
        cachedBassBoost = s
        try {
            bassBoost?.let { bb ->
                if (bb.strengthSupported) {
                    bb.setStrength(s)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "ข้อผิดพลาดในการปรับ BassBoost: ${e.message}", e)
        }
    }

    /**
     * ปรับความกว้างของมิติเสียง Virtualizer (0 ถึง 1000)
     */
    fun setVirtualizerStrength(strength: Int) {
        val s = strength.coerceIn(0, 1000).toShort()
        cachedVirtualizer = s
        try {
            virtualizer?.let { v ->
                if (v.strengthSupported) {
                    v.setStrength(s)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "ข้อผิดพลาดในการปรับ Virtualizer: ${e.message}", e)
        }
    }

    /**
     * ดึงข้อมูลแบนด์ความถี่จาก Hardware หรือใช้แบนด์มาตรฐานหากฮาร์ดแวร์ไม่รองรับ
     */
    fun queryBands(): List<EqBand> {
        val eq = equalizer ?: return defaultBands()
        return try {
            val numBands = eq.numberOfBands
            val range = eq.bandLevelRange
            val minMb = range[0]
            val maxMb = range[1]

            (0 until numBands).map { i ->
                val bandIdx = i.toShort()
                // getCenterFreq คืนค่าเป็น mHz (มิลลิเฮิรตซ์)
                val centerHz = (eq.getCenterFreq(bandIdx) / 1000).coerceAtLeast(20)
                val currentMb = try {
                    eq.getBandLevel(bandIdx)
                } catch (e: Exception) {
                    cachedBandLevels[bandIdx] ?: 0
                }
                EqBand(
                    index = bandIdx,
                    centerFreqHz = centerHz,
                    minLevelMb = minMb,
                    maxLevelMb = maxMb,
                    currentLevelMb = currentMb
                )
            }
        } catch (e: Exception) {
            Log.w(tag, "ไม่สามารถดึงข้อมูลแบนด์จากฮาร์ดแวร์ได้ ใช้ค่ามาตรฐาน 5 แบนด์แทน: ${e.message}")
            defaultBands()
        }
    }

    /**
     * จัดการ Master Volume ผ่าน AudioManager
     */
    fun getMasterVolume(): Int {
        return try {
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        } catch (e: Exception) {
            10
        }
    }

    fun getMaxMasterVolume(): Int {
        return try {
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        } catch (e: Exception) {
            15
        }
    }

    fun setMasterVolume(volume: Int) {
        try {
            val max = getMaxMasterVolume()
            val target = volume.coerceIn(0, max)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        } catch (e: Exception) {
            Log.e(tag, "ข้อผิดพลาดในการตั้งระดับ Master Volume: ${e.message}", e)
        }
    }

    /**
     * ส่ง Broadcast เปิด Control Session เพื่อให้เครื่องเล่นเพลงอื่นๆ เชื่อมต่อเอฟเฟกต์
     */
    private fun broadcastSessionOpen(sessionId: Int) {
        try {
            val intent = Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.w(tag, "ไม่สามารถส่ง broadcast session open: ${e.message}")
        }
    }

    /**
     * ส่ง Broadcast ปิด Control Session
     */
    private fun broadcastSessionClose(sessionId: Int) {
        try {
            val intent = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.w(tag, "ไม่สามารถส่ง broadcast session close: ${e.message}")
        }
    }

    /**
     * คืนทรัพยากรเมื่อแอปพลิเคชันปิด
     */
    fun releaseEffects() {
        broadcastSessionClose(currentSessionId)
        try {
            equalizer?.release()
        } catch (e: Exception) {
            Log.w(tag, "ข้อผิดพลาดในการ release Equalizer: ${e.message}")
        } finally {
            equalizer = null
        }

        try {
            bassBoost?.release()
        } catch (e: Exception) {
            Log.w(tag, "ข้อผิดพลาดในการ release BassBoost: ${e.message}")
        } finally {
            bassBoost = null
        }

        try {
            virtualizer?.release()
        } catch (e: Exception) {
            Log.w(tag, "ข้อผิดพลาดในการ release Virtualizer: ${e.message}")
        } finally {
            virtualizer = null
        }
    }
}
