package com.kjipo.timetracker.weekview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.kjipo.timetracker.dateFormatter
import com.kjipo.timetracker.formatDuration
import com.kjipo.timetracker.reports.TaskSummaryRow


@Composable
fun WeekViewScreen(weekViewModel: WeekViewModel) {
    val uiState = weekViewModel.uiState.collectAsState()
    WeekViewScreen(uiState)
}


@Composable
fun WeekViewScreen(uiState: State<WeekViewState>) {
    val state = rememberLazyListState()

    LazyColumn(state = state) {
        for (daySummary in uiState.value.daySummaries) {
            item {
                DaySummary(daySummary)
            }
        }
    }
}

@Composable
private fun DaySummary(daySummary: DaySummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            style = MaterialTheme.typography.headlineMedium,
            text = dateFormatter.format(daySummary.date)
        )

        Text(
            style = MaterialTheme.typography.headlineMedium,
            text = "(${formatDuration(daySummary.timeLogged)})"
        )
    }

    for (task in daySummary.tasks) {
        TaskSummaryRow(
            task.title,
            task.duration,
            task.project,
            task.tags
        )
    }
}
