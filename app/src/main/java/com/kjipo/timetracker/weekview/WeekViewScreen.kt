package com.kjipo.timetracker.weekview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kjipo.timetracker.dateFormatter
import com.kjipo.timetracker.formatDuration
import java.time.Duration
import java.time.LocalDate


@Composable
fun WeekViewScreen(weekViewModel: WeekViewModel) {
    val uiState = weekViewModel.uiState.collectAsState()
    WeekViewScreen(uiState)
}


@Composable
fun WeekViewScreen(uiState: State<WeekViewState>) {
    Column {
        for (daySummary in uiState.value.daySummaries) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.padding(start = 5.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        text = task.title
                    )

                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = formatDuration(task.duration)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun WeekPreviewScreen() {
    val weekViewState = WeekViewState(
        listOf(
            DaySummary(
                LocalDate.now().minusDays(1),
                Duration.ofHours(3),
                listOf(
                    DayTaskSummary("Task 1", Duration.ofHours(1)),
                    DayTaskSummary("Task 2", Duration.ofHours(2))
                )
            ),
            DaySummary(
                LocalDate.now(),
                Duration.ofHours(2),
                listOf(DayTaskSummary("Task 1", Duration.ofHours(2)))
            )
        )
    )

    WeekViewScreen(remember {
        mutableStateOf(weekViewState)
    })

}