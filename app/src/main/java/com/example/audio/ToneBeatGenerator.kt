package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.model.TestTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * เครื่องกำเนิดเสียงดนตรีทดสอบแบบเรียลไทม์ (Realtime Audio Synthesizer)
 * ใช้ AudioTrack เพื่อสร้างเสียงจังหวะดนตรีสด ทำให้ผู้ใช้สามารถทดสอบผลลัพธ์
 * ของ EQ, Bass Boost และ Virtualizer ได้ยินชัดเจนทันทีในแอปพลิเคชัน
 */
class ToneBeatGenerator {
    private val tag = "ToneBeatGenerator"
    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    var currentTrack: TestTrack = TestTrack.BASS_BEAT
    var isPlaying: Boolean = false
        private set

    val audioSessionId: Int
        get() = audioTrack?.audioSessionId ?: 0

    /**
     * เริ่มเล่นเสียงทดสอบตามแทร็กที่เลือก
     */
    fun startPlaying(track: TestTrack = currentTrack, onSessionReady: (Int) -> Unit) {
        if (isPlaying) {
            stopPlaying()
        }
        currentTrack = track

        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = minBufferSize.coerceAtLeast(sampleRate / 4 * 4)

            val trackInstance = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = trackInstance
            trackInstance.play()
            isPlaying = true
            onSessionReady(trackInstance.audioSessionId)

            playbackJob = scope.launch {
                generateAudioLoop(trackInstance)
            }
        } catch (e: Exception) {
            Log.e(tag, "ข้อผิดพลาดในการเริ่มเล่นเสียงทดสอบ: ${e.message}", e)
            isPlaying = false
        }
    }

    /**
     * หยุดการเล่นเสียงทดสอบ
     */
    fun stopPlaying() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e(tag, "ข้อผิดพลาดในการหยุดเสียง: ${e.message}", e)
        } finally {
            audioTrack = null
        }
    }

    /**
     * สร้างสัญญาณเสียงสเตอริโอแบบสังเคราะห์ต่อเนื่อง
     * มีทั้งย่านความถี่ต่ำ (Sub-bass / Kick 60Hz), ย่านกลาง (Chords 400-1000Hz) และย่านแหลม (Hi-hats / Arp 3-14kHz)
     */
    private fun generateAudioLoop(track: AudioTrack) {
        val chunkSize = 2048
        val buffer = ShortArray(chunkSize * 2) // Stereo: L, R
        var sampleIndex = 0L
        val bpm = when (currentTrack) {
            TestTrack.BASS_BEAT -> 120.0
            TestTrack.SYNTHWAVE -> 110.0
            TestTrack.ACOUSTIC -> 95.0
        }
        val beatSamples = (sampleRate * 60.0 / bpm).toLong()

        while (isPlaying && scope.coroutineContext.isActive) {
            for (i in 0 until chunkSize) {
                val t = sampleIndex.toDouble() / sampleRate
                val beatPosition = (sampleIndex % beatSamples).toDouble() / beatSamples
                val currentBeat = ((sampleIndex / beatSamples) % 4).toInt()

                var leftSample = 0.0
                var rightSample = 0.0

                when (currentTrack) {
                    TestTrack.BASS_BEAT -> {
                        // 1. กระเดื่อง Kick Drum หนักแน่น (60Hz Sub-bass drop)
                        if (currentBeat == 0 || currentBeat == 2) {
                            val kickEnv = (1.0 - beatPosition).coerceAtLeast(0.0)
                            val kickFreq = 55.0 + 80.0 * (kickEnv * kickEnv)
                            val kick = sin(2.0 * PI * kickFreq * t) * kickEnv * 0.7
                            leftSample += kick
                            rightSample += kick
                        }

                        // 2. เบสไลน์ Synth Bass (ย่าน 55Hz - 110Hz)
                        val bassFreq = when (currentBeat) {
                            0 -> 55.0  // A1
                            1 -> 65.4  // C2
                            2 -> 73.4  // D2
                            else -> 49.0 // G1
                        }
                        val bassTone = (sin(2.0 * PI * bassFreq * t) +
                                0.3 * sin(2.0 * PI * bassFreq * 2 * t)) * 0.4
                        leftSample += bassTone
                        rightSample += bassTone

                        // 3. ไฮแฮทและเครื่องกระทบย่านแหลม (8kHz - 14kHz)
                        if (beatPosition in 0.48..0.55 || beatPosition in 0.95..0.99) {
                            val noise = ((sin(2.0 * PI * 9200.0 * t) + sin(2.0 * PI * 14000.0 * t)) * 0.25)
                            leftSample += noise * 0.8
                            rightSample += noise * 1.1 // ให้มิติต่างกันซ้ายขวา
                        }
                    }

                    TestTrack.SYNTHWAVE -> {
                        // คอร์ด Synthwave นุ่มนวล มีมิติเสียงกว้าง
                        val chordNotes = when (currentBeat) {
                            0 -> listOf(220.0, 261.6, 329.6) // Am
                            1 -> listOf(174.6, 220.0, 261.6) // F
                            2 -> listOf(261.6, 329.6, 392.0) // C
                            else -> listOf(196.0, 246.9, 293.7) // G
                        }
                        for ((idx, note) in chordNotes.withIndex()) {
                            val osc = sin(2.0 * PI * note * t) * 0.18
                            if (idx % 2 == 0) leftSample += osc else rightSample += osc
                        }
                        // เบสอุ่นๆ 65Hz
                        leftSample += sin(2.0 * PI * 65.4 * t) * 0.3
                        rightSample += sin(2.0 * PI * 65.4 * t) * 0.3

                        // Arpeggio ย่าน 1.5kHz - 3.5kHz
                        val arpPhase = ((sampleIndex / (beatSamples / 4)) % 4).toInt()
                        val arpFreq = 880.0 * (1.0 + arpPhase * 0.25)
                        val arp = sin(2.0 * PI * arpFreq * t) * 0.15
                        leftSample += arp * 1.2
                        rightSample += arp * 0.8
                    }

                    TestTrack.ACOUSTIC -> {
                        // อะคูสติกสดใส เมโลดี้กลาง-แหลมชัดเจน
                        val rootFreq = when (currentBeat) {
                            0 -> 261.6 // C4
                            1 -> 329.6 // E4
                            2 -> 392.0 // G4
                            else -> 523.3 // C5
                        }
                        val stringSound = (sin(2.0 * PI * rootFreq * t) +
                                0.5 * sin(2.0 * PI * (rootFreq * 2) * t) +
                                0.25 * sin(2.0 * PI * (rootFreq * 3) * t)) * 0.3
                        leftSample += stringSound
                        rightSample += stringSound

                        // แตะสายย่าน 4kHz
                        if (beatPosition in 0.0..0.1 || beatPosition in 0.5..0.6) {
                            val pluck = sin(2.0 * PI * 4200.0 * t) * (1.0 - (beatPosition % 0.5) * 10.0).coerceAtLeast(0.0) * 0.2
                            leftSample += pluck
                            rightSample += pluck * 0.9
                        }
                    }
                }

                // ป้องกันเสียงแตก (Soft Limiting)
                val clampedL = (leftSample.coerceIn(-0.95, 0.95) * Short.MAX_VALUE).toInt().toShort()
                val clampedR = (rightSample.coerceIn(-0.95, 0.95) * Short.MAX_VALUE).toInt().toShort()

                buffer[i * 2] = clampedL
                buffer[i * 2 + 1] = clampedR
                sampleIndex++
            }

            if (isPlaying && track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.write(buffer, 0, buffer.size)
            }
        }
    }
}
