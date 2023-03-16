package com.kjipo.timetracker.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.modifierElementOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview(showBackground = true)
@Composable
fun PieChartReport() {

    Canvas(modifier = Modifier.size(100.dp)) {
        scale(scaleX = 1f, scaleY = 1f) {
            drawArc(color = Color.Green,
                startAngle = 0f,
                sweepAngle = 90f,
            useCenter = true)

            drawCircle(color = Color.White,
                radius = 20.dp.toPx())
        }



    }






}