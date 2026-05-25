package com.kjipo.timetracker.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kjipo.timetracker.database.Sprint
import com.kjipo.timetracker.formatDuration
import com.kjipo.timetracker.tasklist.TaskRow
import com.kjipo.timetracker.tasklist.TaskUi
import java.time.LocalDateTime
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
            SprintSelectionSection(
                selectedSprintId = uiState.selectedSprintId,
                availableSprints = uiState.availableSprints,
                onSprintSelected = { model.selectSprint(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DateDisplaySection(
                startDate = uiState.startDate,
                endDate = uiState.endDate
            )

            Spacer(modifier = Modifier.height(16.dp))

            SummarySection(
                plannedDuration = uiState.plannedDuration,
                unplannedDuration = uiState.unplannedDuration,
                totalAvailableDuration = uiState.totalAvailableDuration
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

@Composable
fun SprintSelectionSection(
    selectedSprintId: Long?,
    availableSprints: List<Sprint>,
    onSprintSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedSprint = availableSprints.find { it.sprintId == selectedSprintId }

    Column {
        Text(
            text = "Select Sprint",
            style = MaterialTheme.typography.labelMedium
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedSprint?.title ?: "No sprint selected",
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select sprint")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                availableSprints.forEach { sprint ->
                    DropdownMenuItem(
                        text = { Text(sprint.title) },
                        onClick = {
                            onSprintSelected(sprint.sprintId)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DateDisplaySection(
    startDate: LocalDateTime,
    endDate: LocalDateTime
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Start Date", style = MaterialTheme.typography.labelMedium)
            Text(startDate.format(formatter), style = MaterialTheme.typography.bodyLarge)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("End Date", style = MaterialTheme.typography.labelMedium)
            Text(endDate.format(formatter), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun SummarySection(
    plannedDuration: java.time.Duration,
    unplannedDuration: java.time.Duration,
    totalAvailableDuration: java.time.Duration
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total available:")
                Text(formatDuration(totalAvailableDuration))
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
