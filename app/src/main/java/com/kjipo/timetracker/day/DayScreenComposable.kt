package com.kjipo.timetracker.day

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun DayScreen(dayModel: DayModel) {
    val uiState by dayModel.uiState.collectAsStateWithLifecycle()

    // Height for one hour (in dp value as Int)
    val baseHourHeightValue = 60
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var scrollOffset by remember { mutableFloatStateOf(0f) }

    val startHour = 0
    val endHour = 24
    val hourHeightDp = (baseHourHeightValue * zoomScale).dp
    val totalHeight = hourHeightDp * (endHour - startHour)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    // Handle zoom (pinch gesture)
                    if (zoom != 1f) {
                        zoomScale = (zoomScale * zoom).coerceIn(0.5f, 3f)
                    }
                    // Handle scroll (pan gesture) - reversed direction
                    scrollOffset = (scrollOffset + pan.y).coerceIn(
                        -(totalHeight.value * density - size.height),
                        0f
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = scrollOffset.dp)
        ) {
            // Time Column
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .height(totalHeight)
            ) {
                for (hour in startHour until endHour) {
                    Box(
                        modifier = Modifier
                            .height(hourHeightDp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = LocalTime.of(hour, 0).format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Tasks Column
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(totalHeight)
                    .background(Color.LightGray.copy(alpha = 0.2f))
            ) {
                // Draw grid lines
                for (hour in startHour until endHour) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .offset(y = hourHeightDp * (hour - startHour))
                            .background(Color.Gray.copy(alpha = 0.5f))
                    )
                }

                val zoneId = ZoneId.systemDefault()
                val dayStart = uiState.date.atStartOfDay(zoneId).toInstant()

                uiState.tasks.forEach { task ->
                    task.timeEntries.forEach { entry ->
                        val entryStart = entry.start
                        val entryEnd = entry.stop ?: Instant.now()

                        val overlaps = entryStart.isBefore(dayStart.plus(1, ChronoUnit.DAYS)) &&
                                entryEnd.isAfter(dayStart)

                        if (overlaps) {
                            val displayStart = if (entryStart.isBefore(dayStart)) dayStart else entryStart
                            val displayEnd = if (entryEnd.isAfter(dayStart.plus(1, ChronoUnit.DAYS))) dayStart.plus(1, ChronoUnit.DAYS) else entryEnd

                            val startMinutes = ChronoUnit.MINUTES.between(dayStart, displayStart).coerceAtLeast(0)
                            val durationMinutes = ChronoUnit.MINUTES.between(displayStart, displayEnd).coerceAtLeast(1)

                            val offsetDp = (startMinutes / 60f) * baseHourHeightValue * zoomScale
                            val heightDp = (durationMinutes / 60f) * baseHourHeightValue * zoomScale

                            Box(
                                modifier = Modifier
                                    .offset(y = offsetDp.dp)
                                    .height(heightDp.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .clickable { /* TODO */ }
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = task.task.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Extension to make Dp math cleaner
private operator fun Dp.times(multiplier: Int): Dp = this * multiplier.toFloat()
private operator fun Float.times(value: Int): Float = this * value
