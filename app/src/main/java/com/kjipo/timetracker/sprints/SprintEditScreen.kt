package com.kjipo.timetracker.sprints

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kjipo.timetracker.database.DayType
import com.kjipo.timetracker.dateFormatter
import java.time.Instant
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SprintEditScreen(
    viewModel: SprintsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val draft = uiState.draftSprint
    if (draft == null) {
        // If we are on this screen and draft is null, it means we either just saved
        // or something went wrong. The LaunchedEffect for events will handle
        // navigation on SaveSuccess.
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = draft.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = draft.endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    // Update picker states when draft dates change
    LaunchedEffect(draft.startDate) {
        val millis = draft.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (startDatePickerState.selectedDateMillis != millis) {
            startDatePickerState.selectedDateMillis = millis
        }
    }
    LaunchedEffect(draft.endDate) {
        val millis = draft.endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (endDatePickerState.selectedDateMillis != millis) {
            endDatePickerState.selectedDateMillis = millis
        }
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var selectedDateForModal by remember { mutableStateOf<LocalDate?>(null) }
    
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            if (event is SprintsViewModel.SprintEvent.SaveSuccess) {
                onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (draft.sprintId == 0L) "Create Sprint" else "Edit Sprint") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cancelDraft()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = {
                        viewModel.saveDraftSprint()
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            TextField(
                value = draft.title,
                onValueChange = { viewModel.updateDraftSprint(it, draft.startDate, draft.endDate) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Start Date", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = draft.startDate.toString(),
                        modifier = Modifier
                            .clickable { showStartDatePicker = true }
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("End Date", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = draft.endDate.toString(),
                        modifier = Modifier
                            .clickable { showEndDatePicker = true }
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Calendar", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            SprintCalendar(
                startDate = draft.startDate,
                endDate = draft.endDate,
                sprintDays = uiState.draftSprintDays,
                customDays = uiState.draftCustomDays,
                dayTypes = uiState.dayTypes,
                onDayClick = { selectedDateForModal = it }
            )
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.updateDraftSprint(draft.title, date, draft.endDate)
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.updateDraftSprint(draft.title, draft.startDate, date)
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }

    selectedDateForModal?.let { date ->
        DayConfigModal(
            date = date,
            dayTypes = uiState.dayTypes,
            currentDayTypeId = uiState.draftSprintDays[date],
            currentCustomHours = uiState.draftCustomDays[date],
            onDismiss = { selectedDateForModal = null },
            onConfigApplied = { dayTypeId, customHours ->
                if (dayTypeId == -1L) { // Custom
                    viewModel.setDraftCustomDay(date, customHours)
                } else if (dayTypeId == 0L) { // Normal
                    viewModel.setDraftSprintDay(date, null)
                    viewModel.setDraftCustomDay(date, null)
                } else {
                    viewModel.setDraftSprintDay(date, dayTypeId)
                }
                selectedDateForModal = null
            }
        )
    }
}

@Composable
fun SprintCalendar(
    startDate: LocalDate,
    endDate: LocalDate,
    sprintDays: Map<LocalDate, Long>,
    customDays: Map<LocalDate, Double>,
    dayTypes: List<DayType>,
    onDayClick: (LocalDate) -> Unit
) {
    val dayHeaderLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val gridItems: List<CalendarGridItem> = remember(startDate, endDate) {
        val items = mutableListOf<CalendarGridItem>()
        dayHeaderLabels.forEach { items.add(CalendarGridItem.DayHeader(it)) }

        if (!startDate.isAfter(endDate)) {
            val startPadding = startDate.dayOfWeek.value - 1
            repeat(startPadding) { i ->
                items.add(CalendarGridItem.OutsideDay(startDate.minusDays((startPadding - i).toLong())))
            }

            var current = startDate
            while (!current.isAfter(endDate)) {
                items.add(CalendarGridItem.SprintDay(current))
                current = current.plusDays(1)
            }

            val endPadding = (7 - endDate.dayOfWeek.value) % 7
            repeat(endPadding) { i ->
                items.add(CalendarGridItem.OutsideDay(endDate.plusDays((i + 1).toLong())))
            }
        }

        items
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(gridItems, key = { item ->
            when (item) {
                is CalendarGridItem.DayHeader -> "header_${item.label}"
                is CalendarGridItem.OutsideDay -> "outside_${item.date}"
                is CalendarGridItem.SprintDay -> "sprint_${item.date}"
            }
        }) { item ->
            when (item) {
                is CalendarGridItem.DayHeader -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (item.label == "Sun") Color.Red
                                    else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is CalendarGridItem.OutsideDay -> {
                    Card(
                        modifier = Modifier.aspectRatio(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                            )
                        }
                    }
                }
                is CalendarGridItem.SprintDay -> {
                    val date = item.date
                    val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
                    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || isSunday
                    val isSpecial = sprintDays.containsKey(date) || customDays.containsKey(date)

                    val backgroundColor = when {
                        isSpecial -> MaterialTheme.colorScheme.primaryContainer
                        isWeekend -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val borderColor = if (isSpecial) MaterialTheme.colorScheme.primary
                                      else MaterialTheme.colorScheme.outlineVariant

                    val hoursToShow: Double? = when {
                        customDays.containsKey(date) -> customDays[date]
                        sprintDays.containsKey(date) -> dayTypes.find { it.dayTypeId == sprintDays[date] }?.workingHours
                        isWeekend -> 0.0
                        else -> null
                    }

                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onDayClick(date) },
                        colors = CardDefaults.cardColors(containerColor = backgroundColor),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSunday) Color.Red
                                        else MaterialTheme.colorScheme.onSurface
                            )
                            if (hoursToShow != null) {
                                val hoursStr = if (hoursToShow == hoursToShow.toLong().toDouble())
                                    "${hoursToShow.toLong()}h"
                                else
                                    "${hoursToShow}h"
                                Text(
                                    text = hoursStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSpecial) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayConfigModal(
    date: LocalDate,
    dayTypes: List<DayType>,
    currentDayTypeId: Long?,
    currentCustomHours: Double?,
    onDismiss: () -> Unit,
    onConfigApplied: (Long, Double?) -> Unit
) {
    var selectedTypeId by remember { mutableStateOf(currentDayTypeId ?: if (currentCustomHours != null) -1L else 0L) }
    var customHoursInput by remember { mutableStateOf(currentCustomHours?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Configure ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedTypeId == 0L, onClick = { selectedTypeId = 0L })
                        Text("Normal working day")
                    }
                    dayTypes.forEach { type ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedTypeId == type.dayTypeId, onClick = { selectedTypeId = type.dayTypeId })
                            Text("${type.title} (${type.workingHours}h)")
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedTypeId == -1L, onClick = { selectedTypeId = -1L })
                        Text("Custom")
                    }
                }

                if (selectedTypeId == -1L) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customHoursInput,
                        onValueChange = { customHoursInput = it },
                        label = { Text("Working hours") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else if (selectedTypeId > 0L) {
                    val hours = dayTypes.find { it.dayTypeId == selectedTypeId }?.workingHours ?: 0.0
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = hours.toString(),
                        onValueChange = {},
                        label = { Text("Working hours") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val hours = if (selectedTypeId == -1L) customHoursInput.toDoubleOrNull() else null
                            onConfigApplied(selectedTypeId, hours)
                        },
                        enabled = selectedTypeId != -1L || customHoursInput.toDoubleOrNull() != null
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

private sealed class CalendarGridItem {
    data class DayHeader(val label: String) : CalendarGridItem()
    data class OutsideDay(val date: LocalDate) : CalendarGridItem()
    data class SprintDay(val date: LocalDate) : CalendarGridItem()
}
