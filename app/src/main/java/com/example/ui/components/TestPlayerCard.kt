package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TestTrack
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletNeon
import kotlin.math.abs
import kotlin.math.sin

/**
 * เครื่องเล่นเสียงตัวอย่าง (Sample Audio Preview Player)
 * ให้ผู้ใช้สามารถฟังผลลัพธ์ของ EQ และเอฟเฟกต์ทั้งหมดได้ทันที
 */
@Composable
fun TestPlayerCard(
    isPlaying: Boolean,
    selectedTrack: TestTrack,
    audioSessionId: Int,
    onTogglePlay: () -> Unit,
    onTrackSelect: (TestTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("test_player_card"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.2.dp, if (isPlaying) EmeraldNeon.copy(alpha = 0.6f) else Color(0xFF30363D)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131821)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = EmeraldNeon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "เครื่องเล่นทดสอบเสียง (Audio Preview)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "ฟังผลการปรับแต่ง EQ และ Bass Boost ได้ยินสดทันที",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                if (isPlaying) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldNeon.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        AnimatedEqualizerBars()
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "กำลังเล่น",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EmeraldNeon,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // เลือกประเภทแทร็กทดสอบเสียง
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TestTrack.entries.forEach { track ->
                    val isSelected = (selectedTrack == track)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTrackSelect(track) },
                        label = {
                            Text(
                                text = track.titleThai,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF1E2532),
                            selectedContainerColor = EmeraldNeon.copy(alpha = 0.2f),
                            labelColor = TextSecondary,
                            selectedLabelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color(0xFF333E50),
                            selectedBorderColor = EmeraldNeon,
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ปุ่มกด เล่น / หยุด ขนาดใหญ่ เข้าถึงง่าย
            Button(
                onClick = onTogglePlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("test_audio_play_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) Color(0xFFDC2626) else EmeraldNeon,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "หยุดเล่นเสียงทดสอบ" else "เริ่มเล่นเสียงทดสอบ",
                    tint = if (isPlaying) Color.White else Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPlaying) "หยุดเสียงทดสอบ (Stop Preview)" else "เล่นเสียงทดสอบเพื่อฟัง EQ ทันที (Play Beat)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) Color.White else Color.Black
                    )
                )
            }
        }
    }
}

/**
 * แอนิเมชันแท่งบาร์ EQ เต้นตามจังหวะเพลง
 */
@Composable
private fun AnimatedEqualizerBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "EqBarsAnim")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "Bar1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse),
        label = "Bar2"
    )
    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "Bar3"
    )

    Row(
        modifier = Modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(0.3f + phase1 * 0.7f)
                .background(EmeraldNeon, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(0.2f + phase2 * 0.8f)
                .background(EmeraldNeon, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(0.4f + phase3 * 0.6f)
                .background(EmeraldNeon, RoundedCornerShape(1.dp))
        )
    }
}
