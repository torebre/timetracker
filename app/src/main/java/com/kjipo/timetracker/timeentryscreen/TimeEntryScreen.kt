package com.kjipo.timetracker.timeentryscreen

import android.view.ContextThemeWrapper
import android.widget.CalendarView
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
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
    updateEntry: (TimeEntry) -> Unit,
    cancel: () -> Unit
) {
    val screenShowing = remember {
        mutableStateOf(TimeEntryShowing.OVERVIEW)
    }

    when {
        uiState.waiting -> {
            Text("Waiting")

        }
        uiState.timeEntry != null -> {
            TimeEntryInternal(
                TimeEntryInternalParameters(
                    uiState.timeEntry,
                    screenShowing,
                    updateEntry,
                    cancel
                )
            )
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

private class TimeEntryInternalParameterProvider :
    PreviewParameterProvider<TimeEntryInternalParameters> {

    override val values = sequenceOf(TimeEntryInternalParameters(
        TimeEntry(
            1, 1,
            LocalDateTime.of(2000, 5, 10, 5, 0, 0).toInstant(ZoneOffset.UTC),
            LocalDateTime.of(2000, 5, 10, 5, 0, 0).toInstant(ZoneOffset.UTC)
        ),
        mutableStateOf(TimeEntryShowing.OVERVIEW), {
            // Do nothing
        },
        {
            // Do nothing
        }
    ))
}


private class TimeEntryInternalParameters(
    val timeEntry: TimeEntry,
    val screenShowing: MutableState<TimeEntryShowing>,
    val updateEntry: (TimeEntry) -> Unit,
    val cancel: () -> Unit
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
                ShowOverview(
                    timeEntryInternalParameters.timeEntry,
                    timeEntryInternalParameters.screenShowing,
                    startTimeState,
                    stopTimeState
                )
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

        if (timeEntryInternalParameters.screenShowing.value == TimeEntryShowing.OVERVIEW) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        timeEntryInternalParameters.updateEntry(
                            timeEntryInternalParameters.timeEntry.copy(
                                start = startTimeState.value,
                                stop = stopTimeState.value
                            )
                        )
                    },
                    enabled = startTimeState.value != timeEntryInternalParameters.timeEntry.start
                            || stopTimeState.value != timeEntryInternalParameters.timeEntry.stop,
                ) {
                    Text("Save")
                }

                Button(
                    modifier = Modifier.padding(start = 5.dp),
                    onClick = {
                        timeEntryInternalParameters.cancel()
                    },
                ) {
                    Text("Cancel")
                }
            }
        }
    }

}


@Composable
private fun ShowOverview(
    timeEntry: TimeEntry,
    screenShowing: MutableState<TimeEntryShowing>,
    startTimeState: MutableState<Instant>,
    stopTimeState: MutableState<Instant>
) {
    Text(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        style = MaterialTheme.typography.headlineSmall,
        text = "Start"
    )
    Row(modifier = Modifier.padding(top = 5.dp)) {
        Text(
            modifier = Modifier.clickable {
                screenShowing.value = TimeEntryShowing.DATE_PICKER_START
            },
            style = MaterialTheme.typography.labelMedium,
            text = dateFormatter.format(startTimeState.value.atZone(ZoneId.systemDefault()))
        )

        Spacer(modifier = Modifier.weight(0.5f))

        if (timeEntry.stop != null) {
            Text(
                modifier = Modifier.clickable {
                    screenShowing.value = TimeEntryShowing.TIME_PICKER_START
                },
                style = MaterialTheme.typography.labelMedium,
                text = timeFormatter.format(startTimeState.value.atZone(ZoneId.systemDefault()))
            )
        }
    }

    if (timeEntry.stop != null) {
        Text(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            style = MaterialTheme.typography.headlineSmall,
            text = "Stop"
        )

        Row {
            Text(
                modifier = Modifier.clickable {
                    screenShowing.value = TimeEntryShowing.DATE_PICKER_STOP
                },
                style = MaterialTheme.typography.labelMedium,
                text = dateFormatter.format(stopTimeState.value.atZone(ZoneId.systemDefault()))
            )

            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                modifier = Modifier.clickable {
                    screenShowing.value = TimeEntryShowing.TIME_PICKER_STOP
                },
                style = MaterialTheme.typography.labelMedium,
                text = timeFormatter.format(stopTimeState.value.atZone(ZoneId.systemDefault()))
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
