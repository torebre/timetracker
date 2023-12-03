package com.kjipo.timetracker.timeentryscreen

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.Instant


@Composable
fun TimeEntryDialog(
    timeEntryEditUiState: MutableState<TimeEntryEditUiState>,
    setShowDialog: (Boolean) -> Unit,
    addTimeEntry: (start: Instant, stop: Instant?) -> Unit
) {
    Dialog(onDismissRequest = { setShowDialog(false) }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Green
        ) {
            TimeEntryScreen(
                timeEntryEditUiState.value,
                { _, start, stop ->
                    addTimeEntry(start, stop)
                },
                {
                    setShowDialog(false)
                })
        }
    }

}