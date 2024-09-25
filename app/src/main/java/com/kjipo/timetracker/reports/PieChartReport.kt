package com.kjipo.timetracker.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp


@Composable
fun PieChartReport(reportsModel: ReportsModel) {
    val uiState = reportsModel.uiState.collectAsState()

    PieChartReport(uiState.value.pieChartData)
}


class PieChartReportParameterProvider : PreviewParameterProvider<ReportsUiState> {
    override val values = sequenceOf(ReportsUiState(pieChartData = PieChartData(
        listOf(
            PieChartEntry(1, 20, Color.Red),
            PieChartEntry(2, 30, Color.Green),
            PieChartEntry(3, 25, Color.Yellow))
    )))

}

@Composable
fun PieChartReport(pieChartData: PieChartData?) {
    val backgroundColour = MaterialTheme.colorScheme.background

    Canvas(modifier = Modifier.size(100.dp)) {
        scale(scaleX = 1f, scaleY = 1f) {
            var startAngle = 0f
            pieChartData?.pieChartEntries?.forEach {pieChartEntry ->
                val sweepAngle = pieChartEntry.percentage.times(3.6f)
                drawArc(color = pieChartEntry.colour,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true)
                startAngle += sweepAngle
            }

            drawCircle(color = backgroundColour,
                radius = 20.dp.toPx())
        }

    }

}