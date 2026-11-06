package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * 1. สวิตช์ Toggle "เปิด/ปิด การใช้งาน EQ" อยู่ด้านบนสุด
 * ออกแบบให้มองเห็นสถานะเด่นชัด พร้อมเอฟเฟกต์สีสว่างเมื่อเปิดใช้งาน
 */
@Composable
fun PowerToggleCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isEnabled) CyanNeon.copy(alpha = 0.6f) else Color(0xFF30363D),
        label = "PowerBorderAnim"
    )

    val iconBgColor by animateColorAsState(
        targetValue = if (isEnabled) CyanNeon.copy(alpha = 0.2f) else Color(0xFF21262D),
        label = "IconBgAnim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("power_toggle_card"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, animatedBorderColor),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "สวิตช์เปิดปิด EQ",
                        tint = if (isEnabled) CyanNeon else TextMuted,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "เปิด/ปิด การใช้งาน EQ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isEnabled) EmeraldNeon else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnabled) "กำลังทำงาน (Active)" else "ปิดใช้งานชั่วคราว (Bypass)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isEnabled) EmeraldNeon else TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("eq_power_switch"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = CyanNeon,
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color(0xFF2A313C)
                )
            )
        }
    }
}
