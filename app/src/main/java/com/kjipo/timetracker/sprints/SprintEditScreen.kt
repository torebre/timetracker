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
    val days = remember(startDate, endDate) {
        val list = mutableListOf<LocalDate>()
        if (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
            var current = startDate
            while (!current.isAfter(endDate)) {
                list.add(current)
                current = current.plusDays(1)
            }
        }
        list
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 80.dp),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(days) { date ->
            val isSpecial = sprintDays.containsKey(date) || customDays.containsKey(date)
            val backgroundColor = if (isSpecial) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            val borderColor = if (isSpecial) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onDayClick(date) },
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(text = date.month.name.take(3), style = MaterialTheme.typography.labelSmall)
                    if (isSpecial) {
                        val hours = if (customDays.containsKey(date)) {
                            customDays[date]
                        } else {
                            dayTypes.find { it.dayTypeId == sprintDays[date] }?.workingHours
                        }
                        Text(text = "${hours}h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
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
