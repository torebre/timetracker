package com.kjipo.timetracker.weekview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.kjipo.timetracker.dateFormatter
import com.kjipo.timetracker.formatDuration


@Composable
fun WeekViewScreen(weekViewModel: WeekViewModel) {
    val uiState = weekViewModel.uiState.collectAsState()

    Column {
        for (daySummary in uiState.value.daySummaries) {
            Row {
                Text(dateFormatter.format(daySummary.date))
            }

            for (task in daySummary.tasks) {
                Text(formatDuration(task.duration))
            }

        }


    }


}