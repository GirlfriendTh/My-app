package com.example

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.EqualizerScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EqualizerViewModel

/**
 * MainActivity สำหรับแอปพลิเคชัน Audio Equalizer (ภาษาไทย)
 * รองรับการรับค่า Audio Session ID จาก Intent ของระบบ หรือเปิดทำงานเป็นแอปปรับแต่งเสียงหลัก
 */
class MainActivity : ComponentActivity() {

    private val viewModel: EqualizerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ตรวจสอบว่าเปิดมาจากโปรแกรมเล่นเพลงอื่นที่ส่ง Audio Session ID มาหรือไม่
        handleIntentAudioSession(intent)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    EqualizerScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentAudioSession(intent)
    }

    /**
     * ดึงค่า Audio Session ID จาก Intent (เมื่อเครื่องเล่นเพลงเรียกใช้งาน Control Panel)
     */
    private fun handleIntentAudioSession(intent: Intent?) {
        if (intent == null) return
        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, -1)
        if (sessionId != -1 && sessionId != 0) {
            viewModel.setExternalAudioSession(sessionId)
        }
    }
}
