package com.kjipo.timetracker.reports

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import java.io.File
import java.time.Duration
import java.time.LocalDateTime


@Composable
fun ReportScreen(reportsModel: ReportsModel) {
    val uiState = reportsModel.uiState.collectAsState()

    ReportScreen(uiState.value) { selectedTimeRange ->
        reportsModel.setSelectedTimeRange(selectedTimeRange)
    }
}

@Composable
fun ReportScreen(uiState: ReportsUiState, onTabSelected: (SelectedTimeRange) -> Unit) {
    val context = LocalContext.current
    // TODO Update selected tab when user clicks on it
    val selectedTab = remember { mutableStateOf(SelectedTimeRange.DAY) }

    Column {
        ScrollableTabRow(selectedTabIndex = selectedTab.value.ordinal) {
            SelectedTimeRange.values().forEachIndexed { index, timeRange ->
                Tab(selected = index == selectedTab.value.ordinal,
                    onClick = {
                        onTabSelected(timeRange)
                    }) {
                    Text(text = timeRange.name)
                }
            }
        }

        val state = rememberLazyListState()
        Column {
            LazyColumn(state = state) {
                for (projectSummary in uiState.projectSummaries) {
                    item {
                        ProjectSummaryScreen(projectSummary)
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
//        DateRangePicker(state = state, modifier = Modifier.weight(1f))

                uiState.projectSummaries

                // TODO Get file to export to

                // TODO Just here for testing
//    CalendarComponent(calendarUiState = CalendarUiState())

                val fileToExportTo = File(context.cacheDir, "export_file_temp.zip")
                Button(onClick = { exportData(fileToExportTo, context) }) {
                    Text("Export")
                }
            }

        }

    }
}

@Composable
fun ProjectSummaryScreen(projectSummary: ProjectSummary) {
    Text(projectSummary.title)
    Text("${projectSummary.duration}")
    Text("${projectSummary.percentage}")
}


@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    val pieChartData = PieChartData(
        listOf(
            PieChartEntry(1, 10, Color.Green),
            PieChartEntry(2, 50, Color.Yellow)
        )
    )

    val projectSummaries = listOf(
        ProjectSummary(
            1L,
            "Test project",
            Duration.ofMinutes(100),
            10.0
        )
    )

    val uiState = ReportsUiState(
        selectedTimeRange = SelectedTimeRange.DAY,
        pieChartData = pieChartData,
        projectSummaries = projectSummaries,
        customRange = DateRange(LocalDateTime.now().minusDays(1), LocalDateTime.now())
    )

    ReportScreen(uiState, {
        // Do nothing
    })

}