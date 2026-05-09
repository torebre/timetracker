package com.kjipo.timetracker.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kjipo.timetracker.formatDuration
import com.kjipo.timetracker.tasklist.TaskRow
import com.kjipo.timetracker.tasklist.TaskUi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SprintReportScreen(
    model: SprintReportModel,
    navigateToTaskScreen: (Long) -> Unit
) {
    val uiState by model.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sprint report") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            DateSelectionSection(
                startDate = uiState.startDate,
                endDate = uiState.endDate,
                onStartDateSelected = { model.setStartDate(it) },
                onEndDateSelected = { model.setEndDate(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SummarySection(
                plannedDuration = uiState.plannedDuration,
                unplannedDuration = uiState.unplannedDuration
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tasks",
                style = MaterialTheme.typography.headlineSmall
            )

            TaskListSection(
                tasks = uiState.tasks,
                navigateToTaskScreen = navigateToTaskScreen
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionSection(
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    onStartDateSelected: (LocalDateTime) -> Unit,
    onEndDateSelected: (LocalDateTime) -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Start Date")
            Button(onClick = { showStartDatePicker = true }) {
                Text(startDate.format(formatter))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("End Date")
            Button(onClick = { showEndDatePicker = true }) {
                Text(endDate.format(formatter))
            }
        }
    }

    if (showStartDatePicker) {
        DatePickerModal(
            initialDate = startDate,
            onDateSelected = {
                it?.let { onStartDateSelected(it) }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerModal(
            initialDate = endDate,
            onDateSelected = {
                it?.let { onEndDateSelected(it) }
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    initialDate: LocalDateTime,
    onDateSelected: (LocalDateTime?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(
                    datePickerState.selectedDateMillis?.let {
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
                    }
                )
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun SummarySection(
    plannedDuration: java.time.Duration,
    unplannedDuration: java.time.Duration
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Planned",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Planned:")
                Text(formatDuration(plannedDuration))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Unplanned:")
                Text(formatDuration(unplannedDuration))
            }
        }
    }
}

@Composable
fun TaskListSection(
    tasks: List<TaskUi>,
    navigateToTaskScreen: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(tasks) { task ->
            TaskRow(
                task = task,
                navigateToTaskScreen = navigateToTaskScreen,
                toggleStartStop = null,
                showAsActive = false
            )
        }
    }
}
