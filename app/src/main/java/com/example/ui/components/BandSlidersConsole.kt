package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqBand
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletNeon

/**
 * 3. ระบบปรับแต่ง EQ แบบ Custom (สไลเดอร์ปรับย่านความถี่ 5 แบนด์มาตรฐาน)
 * สไลเดอร์สไตล์มิกเซอร์สตูดิโอ (Vertical Fader) พร้อมป้ายแสดงความถี่และระดับ dB
 * เมื่อผู้ใช้ปรับระดับแบนด์ใดก็ตาม จะเปลี่ยนเป็นโหมด "กำหนดเอง (Custom)" อัตโนมัติ
 */
@Composable
fun BandSlidersConsole(
    bands: List<EqBand>,
    isEnabled: Boolean,
    onBandLevelChange: (bandIndex: Short, newLevelMb: Short) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("band_sliders_console_card"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFF30363D)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ปรับแต่งย่านความถี่ (Custom EQ)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "ลากแถบขึ้น/ลงเพื่อปรับระดับเสียง (-15 dB ถึง +15 dB)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // คอนโซล 5 แบนด์
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bands.forEach { band ->
                    SingleBandFaderColumn(
                        band = band,
                        isEnabled = isEnabled,
                        onLevelChange = { newLevelMb ->
                            onBandLevelChange(band.index, newLevelMb)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * เสาควบคุม Fader เดี่ยวของแต่ละย่านความถี่
 */
@Composable
private fun SingleBandFaderColumn(
    band: EqBand,
    isEnabled: Boolean,
    onLevelChange: (Short) -> Unit,
    modifier: Modifier = Modifier
) {
    val minLevel = band.minLevelMb.toFloat() // -1500
    val maxLevel = band.maxLevelMb.toFloat() // +1500

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. ค่า dB ปัจจุบัน
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isEnabled) {
                when {
                    band.currentLevelMb > 0 -> CyanNeon.copy(alpha = 0.2f)
                    band.currentLevelMb < 0 -> VioletNeon.copy(alpha = 0.2f)
                    else -> Color(0xFF21262D)
                }
            } else Color(0xFF1E232B),
            border = BorderStroke(
                0.8.dp,
                if (isEnabled && band.currentLevelMb != 0.toShort()) CyanNeon.copy(alpha = 0.5f) else Color.Transparent
            )
        ) {
            Text(
                text = band.formattedDb,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isEnabled) {
                        if (band.currentLevelMb > 0) CyanNeon else TextPrimary
                    } else TextMuted,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. รางสไลเดอร์แนวตั้งพร้อมปุ่ม Knob
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .width(48.dp) // Accessibility min 48dp touch target
                .testTag("eq_band_slider_${band.index}"),
            contentAlignment = Alignment.Center
        ) {
            val totalHeightPx = constraints.maxHeight.toFloat()
            val knobRadiusPx = 14.dp.value * 2.5f

            // ค่าตำแหน่ง Y ของ Knob (0 = บนสุด = maxLevel, totalHeight = ล่างสุด = minLevel)
            val currentNormalized = ((band.currentLevelMb - minLevel) / (maxLevel - minLevel)).coerceIn(0f, 1f)
            val knobCenterY = (1f - currentNormalized) * totalHeightPx

            // กล่องรางเลื่อน (Track)
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF21262D))
            ) {
                // เส้นจุด 0 dB ตรงกลาง
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.Center)
                        .background(Color(0xFF485160))
                )

                // แถบเรืองแสง Active Fill ตามระดับเสียง
                if (isEnabled && band.currentLevelMb != 0.toShort()) {
                    val isPositive = band.currentLevelMb > 0
                    val fillHeight = (Math.abs(band.currentLevelMb.toFloat()) / maxLevel) * 0.5f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fillHeight)
                            .align(if (isPositive) Alignment.Center else Alignment.Center)
                            .background(
                                Brush.verticalGradient(
                                    if (isPositive) listOf(CyanNeon, CyanNeon.copy(alpha = 0.5f))
                                    else listOf(VioletNeon.copy(alpha = 0.5f), VioletNeon)
                                )
                            )
                    )
                }
            }

            // ปุ่ม Knob ปรับระดับ (Fader Cap)
            val knobColor = if (isEnabled) CyanNeon else Color.Gray
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 22.dp)
                    .align(Alignment.TopCenter)
                    // เลื่อน Knob ตามระดับ
                    .padding(top = ((1f - currentNormalized) * (maxHeight.value - 22.dp.value)).coerceAtLeast(0f).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF384353), Color(0xFF1E2530))
                        )
                    )
                    .border(
                        BorderStroke(1.dp, if (isEnabled) knobColor else Color(0xFF485160)),
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // เส้นบากบนปุ่ม Fader
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(2.dp)
                        .background(if (isEnabled) CyanNeon else Color.DarkGray)
                )
            }

            // ตัวจับเหตุการณ์สัมผัสและลาก (Touch & Drag Gesture Handler)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isEnabled, totalHeightPx) {
                        if (!isEnabled) return@pointerInput

                        detectTapGestures(
                            onDoubleTap = {
                                // ดับเบิลคลิกเพื่อรีเซ็ตแบนด์นี้เป็น 0 dB ทันที
                                onLevelChange(0)
                            },
                            onTap = { offset ->
                                val newNormalized = (1f - (offset.y / totalHeightPx)).coerceIn(0f, 1f)
                                val calculatedMb = (minLevel + newNormalized * (maxLevel - minLevel)).toInt().toShort()
                                onLevelChange(calculatedMb)
                            }
                        )
                    }
                    .pointerInput(isEnabled, totalHeightPx) {
                        if (!isEnabled) return@pointerInput

                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val currentY = (1f - currentNormalized) * totalHeightPx
                            val newY = (currentY + dragAmount.y).coerceIn(0f, totalHeightPx)
                            val newNormalized = (1f - (newY / totalHeightPx)).coerceIn(0f, 1f)
                            val calculatedMb = (minLevel + newNormalized * (maxLevel - minLevel)).toInt().toShort()
                            onLevelChange(calculatedMb)
                        }
                    }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. ป้ายความถี่ (เช่น 60 Hz, 230 Hz, 910 Hz, 3.6 kHz, 14 kHz)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF161B22)
        ) {
            Text(
                text = band.formattedFrequency,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = if (isEnabled) TextPrimary else TextMuted,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
    }
}
