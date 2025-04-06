package com.kjipo.timetracker.reports

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kjipo.timetracker.database.Project
import com.kjipo.timetracker.database.Tag
import com.kjipo.timetracker.formatDuration
import com.kjipo.timetracker.tasklist.Tag
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


@Composable
fun ReportScreen(reportsModel: ReportsModel) {
    val uiState = reportsModel.uiState.collectAsState()

    ReportScreen(
        uiState, { selectedTimeRange ->
        reportsModel.setSelectedTimeRange(selectedTimeRange)
    }, { start, stop ->
        reportsModel.setCustomDateRange(start, stop)
    },
        { weekChange: Int ->
            reportsModel.changeSelectedWeek(weekChange)

        })
}

@Composable
fun ReportScreen(
    uiState: State<ReportsUiState>,
    onTabSelected: (SelectedTimeRange) -> Unit,
    customDateRangeChanged: (start: LocalDateTime, stop: LocalDateTime) -> Unit,
    selectedWeekChanged: (weekChange: Int) -> Unit
) {
    val uiStateValue = uiState.value
    ReportScreen(uiStateValue, onTabSelected, customDateRangeChanged, selectedWeekChanged)
}

@Composable
fun ReportScreen(
    uiState: ReportsUiState,
    onTabSelected: (SelectedTimeRange) -> Unit,
    customDateRangeChanged: (start: LocalDateTime, stop: LocalDateTime) -> Unit,
    selectedWeekChanged: (weekChange: Int) -> Unit
) {
    val selectedTab = remember { mutableStateOf(SelectedTimeRange.DAY) }
    val showDialog = remember { mutableStateOf(false) }

    Column {
        ScrollableTabRow(
            selectedTabIndex = selectedTab.value.ordinal,
            modifier = Modifier.height(50.dp)
        ) {
            SelectedTimeRange.entries.forEachIndexed { index, timeRange ->
                Tab(
                    selected = index == selectedTab.value.ordinal,
                    onClick = {
                        onTabSelected(timeRange)
                        selectedTab.value = timeRange
                    }) {
                    Text(text = timeRange.name)
                }
            }
        }

        if (selectedTab.value == SelectedTimeRange.CUSTOM) {
            Button(onClick = {
                showDialog.value = true
            }) {
                Text("Select range")
            }

        } else if (selectedTab.value == SelectedTimeRange.WEEK) {
            Row {
                Button(onClick = {
                    selectedWeekChanged(-1)
                }) {
                    Text("<")
                }
                Text(
                    modifier = Modifier.padding(start = 5.dp, end = 5.dp),
                    text = "${uiState.weekNumber}"
                )
                Button(onClick = {
                    selectedWeekChanged(1)
                }) {
                    Text(">")
                }
            }
        }

        Text("Start: ${uiState.startAndStopTime.startTime} - ${uiState.startAndStopTime.stopTime}")

        if (showDialog.value) {
            DateRangeModal(setShowDialog = { input ->
                showDialog.value = input
            }, { start, stop ->
                if (start != null && stop != null) {
                    val startInstant = Instant.ofEpochMilli(start)
                    val stopInstant = Instant.ofEpochMilli(stop)
                    customDateRangeChanged(
                        LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault()),
                        LocalDateTime.ofInstant(stopInstant, ZoneId.systemDefault())
                    )
                }
            })
        }

//        Column {
//        ProjectSummaryList(uiState)

        Timber.tag("Report").d("Number of tasks summaries: ${uiState.taskSummaries.size}")

        TaskSummaryList(uiState)

        TaskSummarySum(uiState)


//        PieChartReport(uiState.pieChartData)

//            Column(modifier = Modifier.fillMaxWidth()) {
//        DateRangePicker(state = state, modifier = Modifier.weight(1f))

        // TODO Get file to export to

//            }

//        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeModal(
    setShowDialog: (Boolean) -> Unit,
    setDateRange: (start: Long?, stop: Long?) -> Unit
) {
    val state = rememberDateRangePickerState()
    DateRangePicker(state = state)

    Dialog(onDismissRequest = { setShowDialog(false) }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                Row {
                    Button(onClick = {
                        setDateRange(state.selectedStartDateMillis, state.selectedEndDateMillis)
                        setShowDialog(false)
                    }) {
                        Text("Save")
                    }

                    Button(onClick = {
                        setShowDialog(false)
                    }) {
                        Text("Cancel")
                    }

                }
                DateRangePicker(state = state)
            }
        }
    }

}


@Composable
fun ProjectSummaryList(uiState: ReportsUiState) {
    val state = rememberLazyListState()
    LazyColumn(state = state) {
        for (projectSummary in uiState.projectSummaries) {
            item {
                ProjectSummaryScreen(projectSummary)
            }
        }
    }
}

@Composable
fun ProjectSummaryScreen(projectSummary: ProjectSummary) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            style = MaterialTheme.typography.headlineSmall,
            text = projectSummary.title
        )

        Spacer(modifier = Modifier.weight(0.1f))

        Text(formatDuration(projectSummary.duration))
        Text(modifier = Modifier.padding(start = 5.dp), text = "(${projectSummary.percentage} %)")
    }
}


@Composable
fun TaskSummaryList(uiState: ReportsUiState) {
    val state = rememberLazyListState()

    LazyColumn(
        state = state
    ) {
        for (taskSummary in uiState.taskSummaries) {
            item {
                TaskSummaryRow(taskSummary)
            }
        }
    }
}


@Composable
fun TaskSummaryRow(taskSummary: TaskSummary) {
    TaskSummaryRow(
        taskSummary.title,
        taskSummary.duration,
        taskSummary.project,
        taskSummary.tags
    )
}


@Composable
fun TaskSummaryRow(
    title: String,
    duration: Duration,
    project: Project?,
    tags: List<Tag>
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.headlineSmall,
            text = title,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.weight(0.1f))

        Text(
            modifier = Modifier.weight(0.2f),
            text = formatDuration(duration)
        )
    }

    if (tags.isNotEmpty()) {
        Row {
            tags.forEachIndexed { index, tagUi ->
                val modifier = if (index == 0) {
                    Modifier.padding()
                } else {
                    Modifier.padding(start = 5.dp)
                }
                val colour = tagUi.colour?.let {
                    Color(it.red(), it.green(), it.blue())
                }
                Tag(tagUi.title, colour, modifier)
            }
        }
    }

    project?.let { selectedProject ->
        Row(modifier = Modifier.padding(top = 5.dp)) {
            val colour = selectedProject.colour?.let {
                Color(it.red(), it.green(), it.blue())
            }
            Tag(selectedProject.title, colour, Modifier.padding())
        }
    }
}

@Composable
fun TaskSummarySum(uiState: ReportsUiState) {
    Surface(
        modifier = Modifier.padding(bottom = 8.dp, start = 5.dp, end = 5.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Sum")
            Spacer(modifier = Modifier.weight(0.1f))
            Text(text = formatDuration(Duration.ofSeconds(uiState.taskSummaries.map { it.duration }
                .sumOf { it.seconds })))
        }
    }
}
