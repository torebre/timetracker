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
import androidx.compose.runtime.*
import androidx.compose.runtime.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.kjipo.timetracker.database.TimeEntry
import com.kjipo.timetracker.dateFormatter
import com.kjipo.timetracker.timeFormatter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


@Composable
fun TimeEntryScreen(uiState: TimeEntryEditUiState) {
    val screenShowing = remember {
        mutableStateOf(TimeEntryShowing.OVERVIEW)
    }

    when {
        uiState.waiting -> {
            Text("Waiting")

        }
        uiState.timeEntry != null -> {
            TimeEntryInternal(uiState.timeEntry, screenShowing)
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

@Composable
private fun TimeEntryInternal(
    timeEntry: TimeEntry,
    screenShowing: MutableState<TimeEntryShowing>
) {
    val context = LocalContext.current
    val startTimeState = remember {
        mutableStateOf(timeEntry.start)
    }

    Column {
        when (screenShowing.value) {
            TimeEntryShowing.OVERVIEW -> {
                ShowOverview(timeEntry, context, screenShowing)
            }
            TimeEntryShowing.TIME_PICKER_START -> {
                EditTime(startTimeState, context, screenShowing)
            }
            TimeEntryShowing.DATE_PICKER_START -> {
                EditDate(startTimeState, context, screenShowing)
            }
            TimeEntryShowing.TIME_PICKER_STOP -> {
                // TODO
            }
            TimeEntryShowing.DATE_PICKER_STOP -> {
                // TODO
            }

        }
    }

}


@Composable
private fun ShowOverview(
    timeEntry: TimeEntry,
    context: Context,
    screenShowing: MutableState<TimeEntryShowing>
) {
    val start by remember { mutableStateOf(timeEntry.start) }

    Row {
        Text("Start:")
    }
    Row {
        Text(
            modifier = Modifier.clickable {
                screenShowing.value = TimeEntryShowing.DATE_PICKER_START
            },
            text = dateFormatter.format(start.atZone(ZoneId.systemDefault()))
        )
    }
    Row {
        Text(
            modifier = Modifier.clickable {
                screenShowing.value = TimeEntryShowing.TIME_PICKER_START
            },
            text = timeFormatter.format(start.atZone(ZoneId.systemDefault()))
        )
    }

    timeEntry.stop?.let { stop ->
        val stopState = remember { mutableStateOf(stop) }

        Row {
            Text("Stop:")
        }
        Row {
            Text(
                modifier = Modifier.clickable {
                    screenShowing.value = TimeEntryShowing.DATE_PICKER_STOP
                },
                text = dateFormatter.format(stop.atZone(ZoneId.systemDefault()))
            )
        }
        Row {
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
    context: Context,
    screenShowing: MutableState<TimeEntryShowing>
) {
    val localDateTime = LocalDateTime.ofInstant(instantState.value, ZoneId.systemDefault())

    DatePickerDialog(
        {
            screenShowing.value = TimeEntryShowing.OVERVIEW
        },
        {
            // TODO
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
                    instantState.value = localDateTime.withYear(year)
                        .withMonth(month)
                        .withDayOfMonth(dayOfMonth)
                        .toInstant(ZoneId.systemDefault().rules.getOffset(instantState.value))
                }
            }
        )


//        val datePicker = DatePicker(context)
//
//            datePicker.init(
//            localDateTime.year,
//            localDateTime.monthValue,
//            localDateTime.dayOfMonth
//        ) { _, year, monthOfYear, dayOfMonth ->
//            instantState.value = localDateTime.withYear(year)
//                .withMonth(monthOfYear)
//                .withDayOfMonth(dayOfMonth)
//                .toInstant(ZoneId.systemDefault().rules.getOffset(instantState.value))
//        }


    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTime(
    instantState: MutableState<Instant>,
    context: Context,
    screenShowing: MutableState<TimeEntryShowing>
) {
    val localDateTime = LocalDateTime.ofInstant(instantState.value, ZoneId.systemDefault())

    DatePickerDialog(
        {
            screenShowing.value = TimeEntryShowing.OVERVIEW
        },
        {
            // TODO
        }
    ) {
        val timePicker = TimePicker(context)
        timePicker.hour = localDateTime.hour
        timePicker.minute = localDateTime.minute

        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            instantState.value = localDateTime
                .withHour(hourOfDay)
                .withMinute(minute)
                .toInstant(ZoneId.systemDefault().rules.getOffset(instantState.value))
        }
    }

//    val timePickerDialog = TimePickerDialog(
//        context,
//        { _, hourOfDay, minute ->
//            instantState.value = localDateTime.withHour(hourOfDay).withMinute(minute)
//                .toInstant(ZoneId.systemDefault().rules.getOffset(instantState.value))
//        },
//        hour,
//        inputMinute,
//        true
//    )
//
//    timePickerDialog.show()
}
