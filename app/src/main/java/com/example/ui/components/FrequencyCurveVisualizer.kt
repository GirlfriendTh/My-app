package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqBand
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VioletNeon
import kotlin.math.sin

/**
 * กราฟิกแสดงเส้นตอบสนองความถี่ (Frequency Response Curve & Audio Visualizer)
 * วาดเส้นโค้ง Bezier ตามระดับ dB ของแต่ละย่านความถี่ พร้อมแอนิเมชันคลื่นเสียง
 */
@Composable
fun FrequencyCurveVisualizer(
    bands: List<EqBand>,
    isEnabled: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveTransition")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveOffsetAnim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("frequency_visualizer_card"),
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
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "กราฟแสดงผลการตอบสนองความถี่ (Frequency Response)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val midY = h / 2f

                    // 1. วาดเส้น Grid อ้างอิง (+15dB, 0dB, -15dB)
                    val gridColor = Color(0xFF262C36)
                    val zeroLineColor = Color(0xFF3B4354)

                    // เส้น +15dB (บนสุด)
                    drawLine(gridColor, Offset(0f, h * 0.15f), Offset(w, h * 0.15f), strokeWidth = 1.dp.toPx())
                    // เส้น 0dB (ตรงกลาง)
                    drawLine(zeroLineColor, Offset(0f, midY), Offset(w, midY), strokeWidth = 1.5.dp.toPx())
                    // เส้น -15dB (ล่างสุด)
                    drawLine(gridColor, Offset(0f, h * 0.85f), Offset(w, h * 0.85f), strokeWidth = 1.dp.toPx())

                    if (bands.isEmpty()) return@Canvas

                    // 2. คำนวณจุดพิกัดของแต่ละแบนด์
                    val bandPoints = bands.mapIndexed { idx, band ->
                        val x = if (bands.size > 1) {
                            (idx.toFloat() / (bands.size - 1)) * (w - 60f) + 30f
                        } else w / 2f

                        // คำนวณ y จาก currentLevelMb (-1500 ถึง +1500)
                        val level = if (isEnabled) band.currentLevelMb.toFloat() else 0f
                        val normalizedGain = (level / 1500f).coerceIn(-1f, 1f)
                        // +15dB อยู่ด้านบน (y = h * 0.15), 0dB อยู่กลาง (midY), -15dB อยู่ล่าง (y = h * 0.85)
                        var y = midY - (normalizedGain * (midY - h * 0.15f))

                        // หากมีการเล่นเสียงทดสอบ เพิ่มคลื่นแอนิเมชันขนาดเล็ก
                        if (isPlaying && isEnabled) {
                            val waveMod = sin(waveOffset + idx * 1.2f) * 6f
                            y += waveMod
                        }

                        Offset(x, y)
                    }

                    // 3. สร้างเส้น Smooth Cubic Spline
                    val path = Path()
                    val fillPath = Path()

                    val firstPt = bandPoints.first()
                    path.moveTo(0f, firstPt.y)
                    path.lineTo(firstPt.x, firstPt.y)

                    fillPath.moveTo(0f, h)
                    fillPath.lineTo(0f, firstPt.y)
                    fillPath.lineTo(firstPt.x, firstPt.y)

                    for (i in 0 until bandPoints.size - 1) {
                        val p0 = bandPoints[i]
                        val p1 = bandPoints[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }

                    val lastPt = bandPoints.last()
                    path.lineTo(w, lastPt.y)
                    fillPath.lineTo(w, lastPt.y)
                    fillPath.lineTo(w, h)
                    fillPath.close()

                    // วาดเงา Gradient เรืองแสงใต้เส้นกราฟ
                    val fillGradient = Brush.verticalGradient(
                        colors = if (isEnabled) listOf(
                            CyanNeon.copy(alpha = 0.35f),
                            VioletNeon.copy(alpha = 0.15f),
                            Color.Transparent
                        ) else listOf(
                            Color.Gray.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = h
                    )
                    drawPath(fillPath, brush = fillGradient)

                    // วาดเส้นกราฟหลัก
                    val strokeBrush = Brush.horizontalGradient(
                        colors = if (isEnabled) listOf(CyanNeon, VioletNeon) else listOf(Color.Gray, Color.DarkGray)
                    )
                    drawPath(
                        path,
                        brush = strokeBrush,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 4. วาดจุดบนย่านความถี่
                    bandPoints.forEach { pt ->
                        drawCircle(
                            color = if (isEnabled) CyanNeon else Color.Gray,
                            radius = 4.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color(0xFF161B22),
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            // แถบแสดงค่า dB ระดับอ้างอิง
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+15 dB",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "0 dB (ระดับมาตรฐาน)",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                )
                Text(
                    text = "-15 dB",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}
