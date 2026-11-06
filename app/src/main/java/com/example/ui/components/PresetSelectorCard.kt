package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.model.EqualizerPreset
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletNeon

/**
 * 2. ระบบ EQ Preset (โหมดสำเร็จรูปภาษาไทย)
 * แสดงชิปตัวเลือกโหมดเสียงสำเร็จรูปทั้งหมด พร้อมคำอธิบายคุณลักษณะของเสียง
 */
@Composable
fun PresetSelectorCard(
    currentPreset: EqualizerPreset,
    isEnabled: Boolean,
    onPresetSelect: (EqualizerPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("preset_selector_card"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFF30363D)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = VioletNeon,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "โหมดเสียงสำเร็จรูป (EQ Presets)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // แถวชิปเลือกโหมดสำเร็จรูปที่เลื่อนได้
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EqualizerPreset.selectablePresets.forEach { preset ->
                    val isSelected = (currentPreset == preset)

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isEnabled) {
                                onPresetSelect(preset)
                            }
                        },
                        enabled = isEnabled,
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = CyanNeon
                                )
                            }
                        } else null,
                        label = {
                            Text(
                                text = preset.titleThai,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF1C2128),
                            selectedContainerColor = VioletNeon.copy(alpha = 0.25f),
                            labelColor = TextSecondary,
                            selectedLabelColor = TextPrimary,
                            disabledContainerColor = Color(0xFF161B22),
                            disabledLabelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = isEnabled,
                            selected = isSelected,
                            borderColor = Color(0xFF30363D),
                            selectedBorderColor = VioletNeon,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.5.dp
                        ),
                        modifier = Modifier.testTag("preset_chip_${preset.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // คำอธิบายแนวเสียงของโหมดปัจจุบัน
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF21262D)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "รายละเอียดเสียง: ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = currentPreset.subtitleThai,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyanNeon,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}
