package com.kjipo.timetracker.timeentryscreen

import android.app.TimePickerDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview


@Preview
@Composable
fun TimeEntryScreen() {

    val selectedTime = remember { mutableStateOf(Pair(0, 0)) }

    val timePickerDialog = TimePickerDialog(
        LocalContext.current,
        { _, hourOfDay, minute ->
            selectedTime.value = Pair(hourOfDay, minute)
        },
        selectedTime.value.first,
        selectedTime.value.second,
        true
    )


    Button(onClick = {
        timePickerDialog.show()
    }) {
        Text("Start")
    }




}