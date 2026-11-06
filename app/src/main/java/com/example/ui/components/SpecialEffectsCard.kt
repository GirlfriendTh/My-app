package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberNeon
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SliderTrackInactive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletNeon

/**
 * 3. สไลเดอร์เพิ่มเติมสำหรับ "เพิ่มเบส (Bass Boost)" และ "จำลองมิติเสียง (Virtualizer)"
 */
@Composable
fun SpecialEffectsCard(
    bassBoostStrength: Int, // 0 - 1000
    virtualizerStrength: Int, // 0 - 1000
    isEnabled: Boolean,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("special_effects_card"),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Hearing,
                    contentDescription = null,
                    tint = AmberNeon,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "เอฟเฟกต์เสียงพิเศษ (Audio Enhancements)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "เพิ่มความหนักแน่นและขยายมิติเสียงรอบทิศทาง",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. เพิ่มเบส (Bass Boost)
            EffectSliderRow(
                title = "เพิ่มเบส (Bass Boost)",
                subtitle = "ขับเน้นพลังเสียงย่านความถี่ต่ำให้กระหึ่ม",
                icon = Icons.Default.VolumeUp,
                accentColor = AmberNeon,
                value = bassBoostStrength,
                maxValue = 1000,
                isEnabled = isEnabled,
                onValueChange = onBassBoostChange,
                testTag = "bass_boost_slider"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. จำลองมิติเสียง (Virtualizer)
            EffectSliderRow(
                title = "จำลองมิติเสียง (Virtualizer)",
                subtitle = "สร้างมิติเสียงรอบทิศทาง 3D เสมือนจริง",
                icon = Icons.Default.SurroundSound,
                accentColor = VioletNeon,
                value = virtualizerStrength,
                maxValue = 1000,
                isEnabled = isEnabled,
                onValueChange = onVirtualizerChange,
                testTag = "virtualizer_slider"
            )
        }
    }
}

@Composable
private fun EffectSliderRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    value: Int,
    maxValue: Int,
    isEnabled: Boolean,
    onValueChange: (Int) -> Unit,
    testTag: String
) {
    val percent = ((value.toFloat() / maxValue.toFloat()) * 100).toInt()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1A202A),
        border = BorderStroke(0.8.dp, Color(0xFF262E3B))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = if (isEnabled) 0.15f else 0.05f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isEnabled) accentColor else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isEnabled) TextPrimary else TextMuted,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF212835)
                ) {
                    Text(
                        text = if (isEnabled) "$percent%" else "0%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled) accentColor else TextMuted
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Slider(
                value = if (isEnabled) value.toFloat() else 0f,
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..maxValue.toFloat(),
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = SliderTrackInactive,
                    disabledThumbColor = Color.Gray,
                    disabledActiveTrackColor = Color.DarkGray,
                    disabledInactiveTrackColor = SliderTrackInactive
                )
            )
        }
    }
}
