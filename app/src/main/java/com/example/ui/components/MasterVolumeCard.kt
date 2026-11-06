package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SliderTrackInactive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * 1. Master Volume Slider สำหรับปรับความดังรวม
 * ควบคุมระดับเสียงของอุปกรณ์ (Media Stream) พร้อมปุ่มปิด/เปิดเสียงด่วน
 */
@Composable
fun MasterVolumeCard(
    currentVolume: Int,
    maxVolume: Int,
    isMuted: Boolean,
    onVolumeChange: (Int) -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val volumePercent = if (maxVolume > 0) {
        ((currentVolume.toFloat() / maxVolume.toFloat()) * 100).toInt()
    } else 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("master_volume_card"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFF30363D)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("master_volume_mute_button")
                    ) {
                        Icon(
                            imageVector = when {
                                isMuted || currentVolume == 0 -> Icons.AutoMirrored.Filled.VolumeMute
                                volumePercent > 50 -> Icons.AutoMirrored.Filled.VolumeUp
                                else -> Icons.AutoMirrored.Filled.VolumeDown
                            },
                            contentDescription = if (isMuted) "เปิดเสียง" else "ปิดเสียง",
                            tint = if (isMuted) TextMuted else CyanNeon,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "ระดับเสียงรวม (Master Volume)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = if (isMuted) "ปิดเสียงอยู่ (Muted)" else "ระดับเสียงระบบ",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF21262D)
                ) {
                    Text(
                        text = if (isMuted) "0%" else "$volumePercent%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Slider(
                value = if (isMuted) 0f else currentVolume.toFloat(),
                onValueChange = { onVolumeChange(it.toInt()) },
                valueRange = 0f..maxVolume.toFloat().coerceAtLeast(1f),
                steps = maxVolume - 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .testTag("master_volume_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = CyanNeon,
                    activeTrackColor = CyanNeon,
                    inactiveTrackColor = SliderTrackInactive
                )
            )
        }
    }
}
