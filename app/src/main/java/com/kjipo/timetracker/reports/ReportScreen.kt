package com.kjipo.timetracker.reports

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Duration
import java.time.LocalDateTime


@Composable
fun ReportScreen(reportsModel: ReportsModel) {
    val uiState = reportsModel.uiState.collectAsState()

    ReportScreen(uiState) { selectedTimeRange ->
        reportsModel.setSelectedTimeRange(selectedTimeRange)
    }
}

@Composable
fun ReportScreen(uiState: State<ReportsUiState>, onTabSelected: (SelectedTimeRange) -> Unit) {
    val uiStateValue = uiState.value
    ReportScreen(uiState = uiStateValue, onTabSelected = onTabSelected)
}

@Composable
fun ReportScreen(uiState: ReportsUiState, onTabSelected: (SelectedTimeRange) -> Unit) {
    val selectedTab = remember { mutableStateOf(SelectedTimeRange.DAY) }

    Column {
        ScrollableTabRow(
            selectedTabIndex = selectedTab.value.ordinal,
            modifier = Modifier.height(50.dp)
            ) {
            SelectedTimeRange.values().forEachIndexed { index, timeRange ->
                Tab(selected = index == selectedTab.value.ordinal,
                    onClick = {
                        onTabSelected(timeRange)
                        selectedTab.value = timeRange
                    }) {
                    Text(text = timeRange.name)
                }
            }
        }

//        Column {
        ProjectSummaryList(uiState)

        PieChartReport(uiState.pieChartData)

//            Column(modifier = Modifier.fillMaxWidth()) {
//        DateRangePicker(state = state, modifier = Modifier.weight(1f))

        // TODO Get file to export to

        // TODO Just here for testing
//    CalendarComponent(calendarUiState = CalendarUiState())

//            }

//        }

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


    val pieChartDataWeek = PieChartData(
        listOf(
            PieChartEntry(1, 10, Color.Green),
            PieChartEntry(2, 50, Color.Yellow),
            PieChartEntry(2, 5, Color.hsl(200f, 1f, 0.5f)),
            PieChartEntry(2, 5, Color.hsl(30f, 1f, 0.5f)),
            PieChartEntry(2, 30, Color.hsl(150f, 1f, 0.5f)),
        )
    )

    val uiState = ReportsUiState(
        selectedTimeRange = SelectedTimeRange.DAY,
        pieChartData = pieChartDataWeek,
        projectSummaries = projectSummaries,
        customRange = DateRange(LocalDateTime.now().minusDays(1), LocalDateTime.now())
    )

//    val mutableUiState = remember {
//        mutableStateOf(uiState)
//    }

    ReportScreen(uiState) { selectedTimeRange ->
        // Do nothing

    }

}