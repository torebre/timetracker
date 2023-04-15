package com.kjipo.timetracker.reports

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate


@Composable
fun CalendarComponent(calendarUiState: CalendarUiState) {
    var dayInMonth = LocalDate.of(calendarUiState.year.value, calendarUiState.month, 1)

    Box {
        Column {
            while (dayInMonth.month == calendarUiState.month) {
                Row {
                    for (dayOfWeek in DayOfWeek.values()) {
                        Box(modifier = Modifier.size(48.dp)) {
                            if (dayInMonth.dayOfWeek == dayOfWeek) {
                                Text(text = dayInMonth.dayOfMonth.toString())
                                dayInMonth = dayInMonth.plusDays(1)
                            }
                        }
                    }
                }
            }
        }

    }

}


@Preview
@Composable
fun CalendarPreview() {
    val calendarUiState = CalendarUiState()

    CalendarComponent(calendarUiState)

}