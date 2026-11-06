package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BandSlidersConsole
import com.example.ui.components.FrequencyCurveVisualizer
import com.example.ui.components.MasterVolumeCard
import com.example.ui.components.PowerToggleCard
import com.example.ui.components.PresetSelectorCard
import com.example.ui.components.ResetConfirmDialog
import com.example.ui.components.SpecialEffectsCard
import com.example.ui.components.TestPlayerCard
import com.example.ui.components.TopHeaderBar
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.TextMuted
import com.example.viewmodel.EqualizerViewModel

/**
 * หน้าจอหลักของแอปพลิเคชันปรับแต่งเสียง (Audio Equalizer Screen)
 * รองรับภาษาไทย 100% ใช้งานง่าย มีโครงสร้างชัดเจนตามหลักการ Clean Architecture
 */
@Composable
fun EqualizerScreen(
    viewModel: EqualizerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showResetDialog by remember { mutableStateOf(false) }

    // แสดงข้อความแจ้งเตือนสถานะเมื่อมีการเปลี่ยนแปลง
    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("equalizer_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopHeaderBar(
                audioSessionId = uiState.audioSessionId,
                isEffectEnabled = uiState.isEnabled,
                onResetClick = { showResetDialog = true }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp) // ควบคุมความกว้างให้สวยงามทั้งบนมือถือและแท็บเล็ต
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. สวิตช์ Toggle "เปิด/ปิด การใช้งาน EQ" อยู่ด้านบนสุด
                item {
                    PowerToggleCard(
                        isEnabled = uiState.isEnabled,
                        onToggle = { viewModel.toggleEqualizer(it) }
                    )
                }

                // 2. Master Volume Slider สำหรับปรับความดังรวม
                item {
                    MasterVolumeCard(
                        currentVolume = uiState.masterVolume,
                        maxVolume = uiState.maxVolume,
                        isMuted = uiState.isMuted,
                        onVolumeChange = { viewModel.updateMasterVolume(it) },
                        onToggleMute = { viewModel.toggleMute() }
                    )
                }

                // 3. เครื่องเล่นทดสอบเสียง (Audio Preview) ฟังผล EQ ได้ทันที
                item {
                    TestPlayerCard(
                        isPlaying = uiState.isTestAudioPlaying,
                        selectedTrack = uiState.selectedTestTrack,
                        audioSessionId = uiState.audioSessionId,
                        onTogglePlay = { viewModel.toggleTestAudio() },
                        onTrackSelect = { viewModel.selectTestTrack(it) }
                    )
                }

                // 4. กราฟิกแสดงเส้นตอบสนองความถี่ (Frequency Response Curve)
                item {
                    FrequencyCurveVisualizer(
                        bands = uiState.bands,
                        isEnabled = uiState.isEnabled,
                        isPlaying = uiState.isTestAudioPlaying
                    )
                }

                // 5. ระบบ EQ Preset (โหมดสำเร็จรูปภาษาไทย)
                item {
                    PresetSelectorCard(
                        currentPreset = uiState.currentPreset,
                        isEnabled = uiState.isEnabled,
                        onPresetSelect = { viewModel.selectPreset(it) }
                    )
                }

                // 6. ระบบปรับแต่ง EQ แบบ Custom (สไลเดอร์ 5 แบนด์มาตรฐาน)
                item {
                    BandSlidersConsole(
                        bands = uiState.bands,
                        isEnabled = uiState.isEnabled,
                        onBandLevelChange = { bandIndex, newLevelMb ->
                            viewModel.updateBandLevel(bandIndex, newLevelMb)
                        }
                    )
                }

                // 7. สไลเดอร์เพิ่มเติม "เพิ่มเบส (Bass Boost)" และ "จำลองมิติเสียง (Virtualizer)"
                item {
                    SpecialEffectsCard(
                        bassBoostStrength = uiState.bassBoostStrength,
                        virtualizerStrength = uiState.virtualizerStrength,
                        isEnabled = uiState.isEnabled,
                        onBassBoostChange = { viewModel.updateBassBoost(it) },
                        onVirtualizerChange = { viewModel.updateVirtualizer(it) }
                    )
                }

                // 8. ปุ่ม "รีเซ็ตค่ามาตรฐาน" เพื่อดึงค่าเดิมกลับมา
                item {
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_defaults_bottom_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CyanNeon
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF384353))
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = CyanNeon
                        )
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text(
                            text = "รีเซ็ตค่ามาตรฐาน (Reset Defaults)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }

                // ข้อความท้ายหน้าจอ
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "รองรับ Audio Effect Session 0 และแอพพลิเคชันเล่นเพลงทั้งหมดในเครื่อง",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }

    // หน้าต่างยืนยันการรีเซ็ต
    if (showResetDialog) {
        ResetConfirmDialog(
            onConfirm = {
                viewModel.resetToDefaults()
                showResetDialog = false
            },
            onDismiss = {
                showResetDialog = false
            }
        )
    }
}
