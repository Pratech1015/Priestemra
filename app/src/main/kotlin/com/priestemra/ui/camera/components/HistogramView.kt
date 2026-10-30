package com.priestemra.ui.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priestemra.ui.theme.SamsungOverlay
import com.priestemra.ui.theme.SamsungWhite40
import com.priestemra.ui.theme.SamsungYellow

@Composable
fun HistogramView(
    data: FloatArray,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 110.dp, height = 55.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SamsungOverlay)
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val step = size.width / data.size
            val path = Path()
            path.moveTo(0f, size.height)

            for (i in data.indices) {
                val x = i * step
                val y = size.height - (data[i] * size.height).coerceIn(0f, size.height)
                path.lineTo(x, y)
            }
            path.lineTo(size.width, size.height)
            path.close()

            // Fill area
            drawPath(path, color = Color(0x66FFD500))

            // Outline
            drawPath(path, color = SamsungYellow, style = Stroke(width = 1.5.dp.toPx()))

            // Baseline
            drawLine(
                color = SamsungWhite40,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
        Text(
            text = "HIST",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 9.sp,
            modifier = Modifier.align(Alignment.TopStart).padding(2.dp)
        )
    }
}
