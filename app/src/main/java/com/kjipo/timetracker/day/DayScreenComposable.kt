package com.kjipo.timetracker.day

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kjipo.timetracker.database.Task
import com.kjipo.timetracker.database.TaskWithTimeEntries
import timber.log.Timber
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun DayScreen(dayModel: DayModel, onTaskClick: (Long) -> Unit) {
    val uiState by dayModel.uiState.collectAsStateWithLifecycle()

    // Height for one hour (in dp value as Int)
    val baseHourHeightValue = 60
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var scrollOffset by remember { mutableFloatStateOf(0f) }

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectionStartMinutes by remember { mutableStateOf<Float?>(null) }
    var selectionEndMinutes by remember { mutableStateOf<Float?>(null) }
    var showModal by remember { mutableStateOf(false) }

    val startHour = 0
    val endHour = 24
    val hourHeightDp = (baseHourHeightValue * zoomScale).dp
    val totalHeight = hourHeightDp * (endHour - startHour)
    val density = LocalDensity.current

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isSelectionMode, totalHeight) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Handle zoom (pinch gesture)
                    if (zoom != 1f) {
                        zoomScale = (zoomScale * zoom).coerceIn(0.5f, 3f)
                    }
                    // Handle scroll (pan gesture) - reversed direction
                    // Only scroll if NOT in selection mode
                    if (!isSelectionMode && pan.y != 0f) {
                        val maxScroll = -(totalHeight.value - size.height / density.density).coerceAtLeast(0f)
                        scrollOffset = (scrollOffset + pan.y / density.density)
                            .coerceIn(maxScroll, 0f)
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val totalHeightPx = (totalHeight.value * density.density).toInt()
                    // Measure the Row with its required total height (24 hours)
                    val placeable = measurable.measure(
                        constraints.copy(
                            minHeight = totalHeightPx,
                            maxHeight = totalHeightPx
                        )
                    )
                    // Report the layout height as the screen height (constraints.maxHeight)
                    // to prevent the parent Box from centering the oversized child.
                    val layoutHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else totalHeightPx
                    layout(placeable.width, layoutHeight) {
                        placeable.placeRelative(0, (scrollOffset * density.density).toInt())
                    }
                }
        ) {
            // Time Column
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .requiredHeight(totalHeight)
            ) {
                for (hour in startHour until endHour) {
                    Box(
                        modifier = Modifier
                            .height(hourHeightDp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
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
                    .requiredHeight(totalHeight)
                    .background(Color.LightGray.copy(alpha = 0.2f))
                    .then(
                        if (isSelectionMode) {
                            Modifier.pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        selectionStartMinutes = (offset.y / density.density / (baseHourHeightValue * zoomScale)) * 60f
                                        selectionEndMinutes = selectionStartMinutes
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        selectionEndMinutes = (selectionEndMinutes ?: 0f) + (dragAmount.y / density.density / (baseHourHeightValue * zoomScale)) * 60f
                                    }
                                )
                            }
                        } else Modifier
                    )
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
                                    .clickable { onTaskClick(task.task.taskId) }
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

                if (isSelectionMode && selectionStartMinutes != null && selectionEndMinutes != null) {
                    val sMin = minOf(selectionStartMinutes!!, selectionEndMinutes!!).coerceAtLeast(0f)
                    val eMin = maxOf(selectionStartMinutes!!, selectionEndMinutes!!).coerceAtMost(24f * 60f)
                    val duration = (eMin - sMin).coerceAtLeast(1f)

                    val offsetDp = (sMin / 60f) * baseHourHeightValue * zoomScale
                    val heightDp = (duration / 60f) * baseHourHeightValue * zoomScale

                    Box(
                        modifier = Modifier
                            .offset(y = offsetDp.dp)
                            .height(heightDp.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .background(
                                Color(0xFFE2F3E2), // Light green color
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = {
                if (isSelectionMode) {
                    if (selectionStartMinutes != null && selectionEndMinutes != null) {
                        showModal = true
                    } else {
                        isSelectionMode = false
                    }
                } else {
                    isSelectionMode = true
                    selectionStartMinutes = null
                    selectionEndMinutes = null
                }
            }
        ) {
            Icon(
                imageVector = if (isSelectionMode && selectionStartMinutes != null && selectionEndMinutes != null) Icons.Default.Check else Icons.Default.Add,
                contentDescription = if (isSelectionMode) "Confirm selection" else "Add time entry"
            )
        }

        if (showModal) {
            val sMin = minOf(selectionStartMinutes!!, selectionEndMinutes!!).coerceAtLeast(0f)
            val eMin = maxOf(selectionStartMinutes!!, selectionEndMinutes!!).coerceAtMost(24f * 60f)

            val zoneId = ZoneId.systemDefault()
            val startTime = uiState.date.atStartOfDay(zoneId).plusMinutes(sMin.toLong()).toInstant()
            val stopTime = uiState.date.atStartOfDay(zoneId).plusMinutes(eMin.toLong()).toInstant()

            AddTaskModal(
                startTime = startTime,
                stopTime = stopTime,
                availableTasks = uiState.allTasks,
                onDismiss = {
                    showModal = false
                    isSelectionMode = false
                    selectionStartMinutes = null
                    selectionEndMinutes = null
                },
                onTaskSelected = { taskId ->
                    dayModel.addTimeEntry(taskId, startTime, stopTime)
                    showModal = false
                    isSelectionMode = false
                    selectionStartMinutes = null
                    selectionEndMinutes = null
                }
            )
        }
    }
}

@Composable
fun AddTaskModal(
    startTime: Instant,
    stopTime: Instant,
    availableTasks: List<Task>,
    onDismiss: () -> Unit,
    onTaskSelected: (Long) -> Unit
) {
    var selectedTaskId by remember { mutableStateOf<Long?>(null) }
    val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Time Entry",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${formatter.format(startTime)} - ${formatter.format(stopTime)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Task",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )

                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .fillMaxWidth()
                ) {
                    items(availableTasks) { task ->
                        val isSelected = selectedTaskId == task.taskId
                        Text(
                            text = task.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable { selectedTaskId = task.taskId }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { selectedTaskId?.let { onTaskSelected(it) } },
                        enabled = selectedTaskId != null
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

// Extension to make Dp math cleaner
private operator fun Dp.times(multiplier: Int): Dp = this * multiplier.toFloat()
private operator fun Float.times(value: Int): Float = this * value
