package com.kjipo.timetracker.day

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.Instant

@Composable
fun DayScreen(dayModel: DayModel) {
    val uiState by dayModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Height for one hour
    val hourHeight = 60.dp
    val startHour = 0
    val endHour = 24
    val totalHeight = hourHeight * (endHour - startHour)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
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
                        .height(hourHeight)
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
                        .offset(y = hourHeight * (hour - startHour))
                        .background(Color.Gray.copy(alpha = 0.5f))
                )
            }

            val zoneId = ZoneId.systemDefault()
            val dayStart = uiState.date.atStartOfDay(zoneId).toInstant()

            uiState.tasks.forEach { task ->
                task.timeEntries.forEach { entry ->
                    val entryStart = entry.start
                    val entryEnd = entry.stop ?: Instant.now() // Handle ongoing tasks

                    // Check if the entry overlaps with the day
                    val overlaps = entryStart.isBefore(dayStart.plus(1, ChronoUnit.DAYS)) &&
                            entryEnd.isAfter(dayStart)

                    if (overlaps) {
                        // Clamp start and end times to the current day
                        val displayStart = if (entryStart.isBefore(dayStart)) dayStart else entryStart
                        val displayEnd = if (entryEnd.isAfter(dayStart.plus(1, ChronoUnit.DAYS))) dayStart.plus(1, ChronoUnit.DAYS) else entryEnd

                        // Calculate offset and height based on clamped times
                        val startMinutes = ChronoUnit.MINUTES.between(dayStart, displayStart).coerceAtLeast(0)
                        val durationMinutes = ChronoUnit.MINUTES.between(displayStart, displayEnd).coerceAtLeast(1)

                        // Convert minutes to dp
                        val offsetDp = (startMinutes / 60f).dp * 60 // 60dp per hour
                        val heightDp = (durationMinutes / 60f).dp * 60

                        Box(
                            modifier = Modifier
                                .offset(y = offsetDp)
                                .height(heightDp)
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

// Extension to make Dp math cleaner
private operator fun Dp.times(multiplier: Float): Dp = this * multiplier
private operator fun Float.times(dp: Dp): Dp = dp * this
