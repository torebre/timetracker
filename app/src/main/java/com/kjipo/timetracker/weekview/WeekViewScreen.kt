package com.kjipo.timetracker.weekview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kjipo.timetracker.dateFormatter
import com.kjipo.timetracker.formatDuration
import com.kjipo.timetracker.reports.TaskSummaryRow


@Composable
fun WeekViewScreen(weekViewModel: WeekViewModel) {
    val uiState = weekViewModel.uiState.collectAsState()
    WeekViewScreen(uiState, weekViewModel::selectedWeekChanged)
}


@Composable
fun WeekViewScreen(uiState: State<WeekViewState>, selectedWeekChanged: (Long) -> Unit) {
    val state = rememberLazyListState()

    Column {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                selectedWeekChanged(-1)
            }) {
                Text("<")
            }
            Text(
                modifier = Modifier.padding(start = 5.dp, end = 5.dp),
                text = "${uiState.value.weekNumber}",
                style = MaterialTheme.typography.headlineMedium,
            )
            Button(onClick = {
                selectedWeekChanged(1)
            }) {
                Text(">")
            }
        }

        LazyColumn(state = state) {
            for (daySummary in uiState.value.daySummaries) {
                item(key = daySummary.date.toEpochDay()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(5.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        DaySummary(daySummary)
                    }
                }

            }
        }
    }
}

@Composable
private fun DaySummary(daySummary: DaySummary) {
    Column {
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
}
