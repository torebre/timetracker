package com.kjipo.timetracker.timeentryscreen

import android.app.TimePickerDialog
import android.content.Context
import android.view.ContextThemeWrapper
import android.widget.CalendarView
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.viewinterop.AndroidView
import com.kjipo.timetracker.database.TimeEntry
import com.kjipo.timetracker.dateFormatter
import com.kjipo.timetracker.timeFormatter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset


@Composable
fun TimeEntryScreen(
    uiState: TimeEntryEditUiState,
    updateEntry: (TimeEntry) -> Unit
) {
    val screenShowing = remember {
        mutableStateOf(TimeEntryShowing.OVERVIEW)
    }

    when {
        uiState.waiting -> {
            Text("Waiting")

        }
        uiState.timeEntry != null -> {
            TimeEntryInternal(TimeEntryInternalParameters(uiState.timeEntry, screenShowing, updateEntry))
        }
        else -> {
            // TODO


        }

    }

}


private enum class TimeEntryShowing {
    OVERVIEW,
    TIME_PICKER_START,
    DATE_PICKER_START,
    TIME_PICKER_STOP,
    DATE_PICKER_STOP
}

private class TimeEntryInternalParameterProvider: PreviewParameterProvider<TimeEntryInternalParameters> {

    override val values = sequenceOf(TimeEntryInternalParameters(
        TimeEntry(1, 1,
            LocalDateTime.of(2000, 5, 10, 5, 0, 0).toInstant(ZoneOffset.UTC),
                LocalDateTime.of(2000, 5, 10, 5, 0, 0).toInstant(ZoneOffset.UTC)),
        mutableStateOf(TimeEntryShowing.OVERVIEW), {
            // Do nothing
        }
    ))
}


private class TimeEntryInternalParameters(
    val timeEntry: TimeEntry,
    val screenShowing: MutableState<TimeEntryShowing>,
    val updateEntry: (TimeEntry) -> Unit
)

@Preview(showBackground = true)
@Composable
private fun TimeEntryInternal(
   @PreviewParameter(TimeEntryInternalParameterProvider::class) timeEntryInternalParameters: TimeEntryInternalParameters
) {
    val startTimeState = remember {
        mutableStateOf(timeEntryInternalParameters.timeEntry.start)
    }
    val stopTimeState = remember {
        mutableStateOf(timeEntryInternalParameters.timeEntry.stop ?: Instant.now())
    }

    Column {
        when (timeEntryInternalParameters.screenShowing.value) {
            TimeEntryShowing.OVERVIEW -> {
                ShowOverview(timeEntryInternalParameters.timeEntry, timeEntryInternalParameters.screenShowing)
            }
            TimeEntryShowing.TIME_PICKER_START -> {
                EditTime(startTimeState, timeEntryInternalParameters.screenShowing)
            }
            TimeEntryShowing.DATE_PICKER_START -> {
                EditDate(startTimeState, timeEntryInternalParameters.screenShowing)
            }
            TimeEntryShowing.TIME_PICKER_STOP -> {
                EditTime(stopTimeState, timeEntryInternalParameters.screenShowing)
            }
            TimeEntryShowing.DATE_PICKER_STOP -> {
                EditDate(stopTimeState, timeEntryInternalParameters.screenShowing)
            }
        }

        TextButton(
            onClick = {
                timeEntryInternalParameters.updateEntry(
                    timeEntryInternalParameters.timeEntry.copy(
                        start = startTimeState.value,
                        stop = stopTimeState.value
                    )
                )
            },
            enabled = startTimeState.value != timeEntryInternalParameters.timeEntry.start
                    || stopTimeState.value != timeEntryInternalParameters.timeEntry.stop
        ) {
            Text("Save ${
                startTimeState.value != timeEntryInternalParameters.timeEntry.start
                        || stopTimeState.value != timeEntryInternalParameters.timeEntry.stop
            }")
        }

    }

}


@Composable
private fun ShowOverview(
    timeEntry: TimeEntry,
    screenShowing: MutableState<TimeEntryShowing>
) {
    val start by remember { mutableStateOf(timeEntry.start) }

    Text("Start:")
    Row {
        Text(
            modifier = Modifier.clickable {
                screenShowing.value = TimeEntryShowing.DATE_PICKER_START
            },
            text = dateFormatter.format(start.atZone(ZoneId.systemDefault()))
        )
        Text(
            modifier = Modifier.clickable {
                screenShowing.value = TimeEntryShowing.TIME_PICKER_START
            },
            text = timeFormatter.format(start.atZone(ZoneId.systemDefault()))
        )
    }

    timeEntry.stop?.let { stop ->
        Text("Stop:")

        Row {
            Text(
                modifier = Modifier.clickable {
                    screenShowing.value = TimeEntryShowing.DATE_PICKER_STOP
                },
                text = dateFormatter.format(stop.atZone(ZoneId.systemDefault()))
            )
            Text(
                modifier = Modifier.clickable {
                    screenShowing.value = TimeEntryShowing.TIME_PICKER_STOP
                },
                text = timeFormatter.format(stop.atZone(ZoneId.systemDefault()))
            )
        }
    }
}


data class TimeEntryEditUiState(
    val waiting: Boolean = false,
    val timeEntry: TimeEntry? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDate(
    instantState: MutableState<Instant>,
    screenShowing: MutableState<TimeEntryShowing>
) {
    var localDateTime =
        remember { LocalDateTime.ofInstant(instantState.value, ZoneId.systemDefault()) }

    DatePickerDialog(
        {
            screenShowing.value = TimeEntryShowing.OVERVIEW
        },
        confirmButton = {
            TextButton(onClick = {
                instantState.value =
                    localDateTime.toInstant(ZoneId.systemDefault().rules.getOffset(instantState.value))
                screenShowing.value = TimeEntryShowing.OVERVIEW
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                screenShowing.value = TimeEntryShowing.OVERVIEW
            }) {
                Text("Cancel")
            }
        }
    ) {
        AndroidView(
            modifier = Modifier.wrapContentSize(),
            factory = { context ->
                CalendarView(
                    ContextThemeWrapper(
                        context,
                        com.kjipo.timetracker.R.style.CalenderViewCustom
                    )
                )
            },
            update = { view ->
//                view.minDate = // contraints
//                    view.maxDate = // contraints

                view.setOnDateChangeListener { _, year, month, dayOfMonth ->
                    localDateTime = localDateTime.withYear(year)
                        .withMonth(month)
                        .withDayOfMonth(dayOfMonth)
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTime(
    instantState: MutableState<Instant>,
    screenShowing: MutableState<TimeEntryShowing>
) {
    var localDateTime = LocalDateTime.ofInstant(instantState.value, ZoneId.systemDefault())

    DatePickerDialog(
        {
            screenShowing.value = TimeEntryShowing.OVERVIEW
        },
        confirmButton = {
            TextButton(onClick = {
                instantState.value =
                    localDateTime.toInstant(ZoneId.systemDefault().rules.getOffset(instantState.value))
                screenShowing.value = TimeEntryShowing.OVERVIEW
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                screenShowing.value = TimeEntryShowing.OVERVIEW
            }) {
                Text("Cancel")
            }
        }
    ) {
        AndroidView(
            modifier = Modifier.wrapContentSize(),
            factory = { context ->
                TimePicker(context).also {
                    it.hour = localDateTime.hour
                    it.minute = localDateTime.minute
                }
            },
            update = { view ->
//                view.minDate = // contraints
//                    view.maxDate = // contraints

                view.setOnTimeChangedListener { _, hourOfDay, minute ->
                    localDateTime = localDateTime
                        .withHour(hourOfDay)
                        .withMinute(minute)
                }
            }
        )
    }
}
