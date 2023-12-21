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
    updateOrCreateEntry: (timeEntryId: Long?, start: Instant, stop: Instant?) -> Unit,
    cancel: () -> Unit
) {
    when {
        uiState.waiting -> {
            Text("Waiting")
        }

        uiState.timeEntry != null -> {
            TimeEntryInternal(
                TimeEntryInternalParameters(
                    uiState.timeEntry.timeEntryId,
                    uiState.timeEntry.start,
                    uiState.timeEntry.stop,
                    updateOrCreateEntry,
                    cancel
                )
            )
        }

        uiState.timeEntry == null -> {
            TimeEntryInternal(
                TimeEntryInternalParameters(
                    null,
                    Instant.now().minusSeconds(300),
                    Instant.now(),
                    updateOrCreateEntry,
                    cancel
                )
            )
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
        1L,
        LocalDateTime.of(2000, 5, 10, 5, 0, 0).toInstant(ZoneOffset.UTC),
        LocalDateTime.of(2000, 5, 10, 5, 0, 0).toInstant(ZoneOffset.UTC),
        { timeEntryId, start, stop ->
            // Do nothing
        },
        {
            // Do nothing
        }
    ))
}


class TimeEntryInternalParameters(
    val timeEntryId: Long?,
    val start: Instant,
    val stop: Instant?,
    val updateEntry: (timeEntryId: Long?, start: Instant, stop: Instant?) -> Unit,
    val cancel: () -> Unit
)

@Preview(showBackground = true)
@Composable
fun TimeEntryInternal(
    @PreviewParameter(TimeEntryInternalParameterProvider::class) timeEntryInternalParameters: TimeEntryInternalParameters
) {
    val screenShowing = remember {
        mutableStateOf(TimeEntryShowing.OVERVIEW)
    }

    val startTimeState = remember {
        mutableStateOf(timeEntryInternalParameters.start)
    }

    // TODO Does this work? Setting stop time to now if no stop is set for time
    val stopTimeState = remember {
        mutableStateOf(timeEntryInternalParameters.stop ?: Instant.now())
    }

    Column(modifier = Modifier.padding(10.dp)) {
        when (screenShowing.value) {
            TimeEntryShowing.OVERVIEW -> {
                ShowOverview(
                    screenShowing,
                    startTimeState,
                    stopTimeState
                )
            }

            TimeEntryShowing.TIME_PICKER_START -> {
                EditTime(startTimeState, screenShowing)
            }

            TimeEntryShowing.DATE_PICKER_START -> {
                EditDate(startTimeState, screenShowing)
            }

            TimeEntryShowing.TIME_PICKER_STOP -> {
                EditTime(stopTimeState, screenShowing)
            }

            TimeEntryShowing.DATE_PICKER_STOP -> {
                EditDate(stopTimeState, screenShowing)
            }
        }

        if (screenShowing.value == TimeEntryShowing.OVERVIEW) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        timeEntryInternalParameters.updateEntry(
                            timeEntryInternalParameters.timeEntryId,
                            startTimeState.value,
                            stopTimeState.value
                        )
                    },
                    enabled = startTimeState.value != timeEntryInternalParameters.start
                            || stopTimeState.value != timeEntryInternalParameters.stop
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
    screenShowing: MutableState<TimeEntryShowing>,
    startTimeState: MutableState<Instant>,
    stopTimeState: MutableState<Instant?>
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

        Text(
            modifier = Modifier.clickable {
                screenShowing.value = TimeEntryShowing.TIME_PICKER_START
            },
            style = MaterialTheme.typography.labelMedium,
            text = timeFormatter.format(startTimeState.value.atZone(ZoneId.systemDefault()))
        )
    }

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
            text = stopTimeState.value?.let { dateFormatter.format(it.atZone(ZoneId.systemDefault())) }
                ?: ""
        )

        Spacer(modifier = Modifier.weight(0.5f))

        Text(
            modifier = Modifier.clickable {
                screenShowing.value = TimeEntryShowing.TIME_PICKER_STOP
            },
            style = MaterialTheme.typography.labelMedium,
            text = stopTimeState.value?.let {
                timeFormatter.format(it.atZone(ZoneId.systemDefault()))
            } ?: ""
        )
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
