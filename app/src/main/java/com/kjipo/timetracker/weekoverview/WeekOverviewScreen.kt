package com.kjipo.timetracker.weekoverview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kjipo.timetracker.weekDateFormatter
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun WeekOverviewScreen(viewModel: WeekOverviewViewModel) {
    val uiState = viewModel.uiState.collectAsState()
    val state = uiState.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = weekDateFormatter.format(state.startDate),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Week ${state.weekNumber}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = weekDateFormatter.format(state.endDate),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { viewModel.goToPreviousWeek() }) { Text("<") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { viewModel.goToNextWeek() }) { Text(">") }
        }

        WeekColumnsChart(state.days)
    }
}

@Composable
private fun WeekColumnsChart(days: List<OverviewDaySummary>) {
    if (days.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data")
        }
        return
    }

    val allSegments = days.flatMap { it.segments }
    val minDisplayMinutes = if (allSegments.isEmpty()) 480
        else minOf(480, allSegments.minOf { it.startMinutes })
    val maxDisplayMinutes = if (allSegments.isEmpty()) 1020
        else maxOf(1020, allSegments.maxOf { it.endMinutes })

    Row(modifier = Modifier.fillMaxSize()) {
        for (day in days) {
            DayColumn(
                day = day,
                minMinutes = minDisplayMinutes,
                maxMinutes = maxDisplayMinutes,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 1.dp)
            )
        }
    }
}

@Composable
private fun DayColumn(
    day: OverviewDaySummary,
    minMinutes: Int,
    maxMinutes: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            fontSize = 9.sp,
            maxLines = 1
        )
        Text(
            text = day.date.dayOfMonth.toString(),
            fontSize = 9.sp,
            maxLines = 1
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            drawDaySegments(day.segments, minMinutes, maxMinutes)
        }
    }
}

private fun DrawScope.drawDaySegments(
    segments: List<DayTimeSegment>,
    minMinutes: Int,
    maxMinutes: Int
) {
    val totalRange = (maxMinutes - minMinutes).toFloat()
    if (totalRange <= 0f) return

    var cursor = minMinutes

    for (segment in segments) {
        val segStart = segment.startMinutes.coerceAtLeast(minMinutes)
        val segEnd = segment.endMinutes.coerceAtMost(maxMinutes)
        if (segStart >= segEnd) continue

        if (segStart > cursor) {
            drawRect(
                color = Color.Gray,
                topLeft = Offset(0f, (cursor - minMinutes) / totalRange * size.height),
                size = Size(size.width, (segStart - cursor) / totalRange * size.height)
            )
        }

        val segColor = when {
            segment.tags.isEmpty() -> Color.Green.copy(alpha = 0.5f)
            segment.tags.size > 1 -> Color.Blue.copy(alpha = 0.5f)
            segment.tags[0].colour != null -> Color(segment.tags[0].colour!!.toArgb())
            else -> Color.Green.copy(alpha = 0.5f)
        }

        drawRect(
            color = segColor,
            topLeft = Offset(0f, (segStart - minMinutes) / totalRange * size.height),
            size = Size(size.width, (segEnd - segStart) / totalRange * size.height)
        )

        cursor = cursor.coerceAtLeast(segEnd)
    }

    if (cursor < maxMinutes) {
        drawRect(
            color = Color.Gray,
            topLeft = Offset(0f, (cursor - minMinutes) / totalRange * size.height),
            size = Size(size.width, (maxMinutes - cursor) / totalRange * size.height)
        )
    }
}
