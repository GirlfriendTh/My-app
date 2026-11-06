package com.example.model

/**
 * รูปแบบ Preset โหมดเสียงสำเร็จรูปภาษาไทย
 * กำหนดค่า dB สำหรับ 5 แบนด์หลัก, ค่า Bass Boost และค่า Virtualizer
 */
enum class EqualizerPreset(
    val titleThai: String,
    val subtitleThai: String,
    val defaultBandLevelsMb: List<Short>,
    val defaultBassBoost: Int,
    val defaultVirtualizer: Int
) {
    NORMAL(
        titleThai = "ทั่วไป (Normal / Flat)",
        subtitleThai = "เสียงสมดุลทุกย่านความถี่",
        defaultBandLevelsMb = listOf(0, 0, 0, 0, 0),
        defaultBassBoost = 0,
        defaultVirtualizer = 0
    ),
    POP(
        titleThai = "ป๊อป (Pop)",
        subtitleThai = "เน้นเสียงร้องใส ดนตรีฟังสบาย",
        defaultBandLevelsMb = listOf(-100, 200, 450, 300, -100),
        defaultBassBoost = 250,
        defaultVirtualizer = 200
    ),
    ROCK(
        titleThai = "ร็อค (Rock)",
        subtitleThai = "เบสหนัก แหลมคมชัด กีตาร์ทรงพลัง",
        defaultBandLevelsMb = listOf(500, 300, -100, 400, 600),
        defaultBassBoost = 500,
        defaultVirtualizer = 350
    ),
    CLASSIC(
        titleThai = "คลาสสิก (Classic)",
        subtitleThai = "เสียงกังวาน มิติเครื่องสายและเปียโน",
        defaultBandLevelsMb = listOf(400, 250, -150, 300, 450),
        defaultBassBoost = 150,
        defaultVirtualizer = 400
    ),
    BASS_BOOST(
        titleThai = "เบสหนัก (Bass Boost Focus)",
        subtitleThai = "เน้นย่านความถี่ต่ำ กระหึ่ม หนักแน่น",
        defaultBandLevelsMb = listOf(800, 600, 150, 0, -200),
        defaultBassBoost = 850,
        defaultVirtualizer = 250
    ),
    VOCAL(
        titleThai = "เสียงร้องชัด (Vocal Focus)",
        subtitleThai = "เน้นเสียงกลาง ชัดเจน ตัดเสียงรบกวน",
        defaultBandLevelsMb = listOf(-300, 100, 750, 450, 100),
        defaultBassBoost = 50,
        defaultVirtualizer = 150
    ),
    CUSTOM(
        titleThai = "กำหนดเอง (Custom)",
        subtitleThai = "ปรับแต่งความถี่ตามความชอบของคุณ",
        defaultBandLevelsMb = listOf(0, 0, 0, 0, 0),
        defaultBassBoost = 0,
        defaultVirtualizer = 0
    );

    companion object {
        val selectablePresets: List<EqualizerPreset> = entries
    }
}

/**
 * ข้อมูลย่านความถี่เดี่ยว (EQ Band)
 * @param index หมายเลขแบนด์ (0..4)
 * @param centerFreqHz ความถี่กึ่งกลางในหน่วย Hz (เช่น 60, 230, 910, 3600, 14000)
 * @param minLevelMb ระดับต่ำสุดในหน่วย mB (มิลลิเบล เช่น -1500 mB = -15 dB)
 * @param maxLevelMb ระดับสูงสุดในหน่วย mB (เช่น +1500 mB = +15 dB)
 * @param currentLevelMb ระดับปัจจุบันในหน่วย mB
 */
data class EqBand(
    val index: Short,
    val centerFreqHz: Int,
    val minLevelMb: Short = -1500,
    val maxLevelMb: Short = 1500,
    val currentLevelMb: Short = 0
) {
    /**
     * แปลงความถี่ให้อ่านง่าย เช่น 60 Hz หรือ 3.6 kHz
     */
    val formattedFrequency: String
        get() = if (centerFreqHz >= 1000) {
            val kHz = centerFreqHz / 1000.0
            if (kHz % 1.0 == 0.0) "${kHz.toInt()} kHz" else String.format("%.1f kHz", kHz)
        } else {
            "$centerFreqHz Hz"
        }

    /**
     * แปลงระดับ mB ให้เป็น dB แสดงผล เช่น "+3 dB", "-2 dB", "0 dB"
     */
    val formattedDb: String
        get() {
            val db = currentLevelMb / 100
            return if (db > 0) "+$db dB" else "$db dB"
        }
}

/**
 * เพลงตัวอย่างสำหรับทดสอบเสียงในแอปพลิเคชัน
 */
enum class TestTrack(
    val titleThai: String,
    val genreThai: String,
    val descriptionThai: String
) {
    BASS_BEAT(
        titleThai = "จังหวะเบสหนัก (Bass Groove)",
        genreThai = "Electronic / EDM",
        descriptionThai = "เหมาะสำหรับทดสอบย่าน 60Hz และ Bass Boost"
    ),
    SYNTHWAVE(
        titleThai = "ดนตรีอิเล็กทรอนิกส์ (Synthwave)",
        genreThai = "Retro Synth",
        descriptionThai = "เหมาะสำหรับทดสอบย่านกลางและมิติเสียง Virtualizer"
    ),
    ACOUSTIC(
        titleThai = "อะคูสติกสดใส (Acoustic Harmony)",
        genreThai = "Acoustic / Melodic",
        descriptionThai = "เหมาะสำหรับทดสอบเสียงร้องและย่านแหลม 14kHz"
    )
}

/**
 * สถานะทั้งหมดของหน้าจอ Equalizer
 */
data class EqualizerUiState(
    val isEnabled: Boolean = true,
    val currentPreset: EqualizerPreset = EqualizerPreset.NORMAL,
    val bands: List<EqBand> = defaultBands(),
    val bassBoostStrength: Int = 0, // 0 - 1000
    val virtualizerStrength: Int = 0, // 0 - 1000
    val masterVolume: Int = 10,
    val maxVolume: Int = 15,
    val isMuted: Boolean = false,
    val audioSessionId: Int = 0,
    val isTestAudioPlaying: Boolean = false,
    val selectedTestTrack: TestTrack = TestTrack.BASS_BEAT,
    val isHardwareEqualizerSupported: Boolean = true,
    val statusMessage: String? = null
)

/**
 * ค่าเริ่มต้น 5 แบนด์มาตรฐาน (60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz)
 */
fun defaultBands(): List<EqBand> = listOf(
    EqBand(index = 0, centerFreqHz = 60, minLevelMb = -1500, maxLevelMb = 1500, currentLevelMb = 0),
    EqBand(index = 1, centerFreqHz = 230, minLevelMb = -1500, maxLevelMb = 1500, currentLevelMb = 0),
    EqBand(index = 2, centerFreqHz = 910, minLevelMb = -1500, maxLevelMb = 1500, currentLevelMb = 0),
    EqBand(index = 3, centerFreqHz = 3600, minLevelMb = -1500, maxLevelMb = 1500, currentLevelMb = 0),
    EqBand(index = 4, centerFreqHz = 14000, minLevelMb = -1500, maxLevelMb = 1500, currentLevelMb = 0)
)
