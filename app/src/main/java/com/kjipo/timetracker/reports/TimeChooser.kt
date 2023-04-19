package com.kjipo.timetracker.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CalendarComponent(calendarUiState: CalendarUiState) {
    var expanded by remember { mutableStateOf(false) }
    var dayInMonth = LocalDate.of(calendarUiState.year.value, calendarUiState.month, 1)
    var selectedMonth by remember { mutableStateOf(calendarUiState.month) }

    Column {
        ExposedDropdownMenuBox(expanded = expanded,
            onExpandedChange = { expanded = !expanded }) {
            TextField(readOnly = true,
                value = selectedMonth.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                onValueChange = { },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            })
            ExposedDropdownMenu(expanded = expanded,
                onDismissRequest = { expanded = false }) {
                for (month in Month.values()) {
                    DropdownMenuItem(onClick = { selectedMonth = month; expanded = false }) {
                        Text(text = month.getDisplayName(TextStyle.FULL, Locale.getDefault()))
                    }
                }
            }
        }

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

}


@Preview
@Composable
fun CalendarPreview() {
    val calendarUiState = CalendarUiState()

    CalendarComponent(calendarUiState)

}