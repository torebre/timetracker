package com.kjipo.timetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kjipo.timetracker.database.TimeEntry
import com.kjipo.timetracker.taskscreen.TaskScreenModel
import com.kjipo.timetracker.taskscreen.TaskScreenUiState
import java.time.LocalDateTime
import java.time.ZoneOffset


class TaskScreenParameterProvider : PreviewParameterProvider<TaskScreenInput> {

    override val values = sequenceOf(
        TaskScreenInput(
            TaskScreenUiState(
                1,
                "Task name",
                listOf(
                    TimeEntry(
                        1, 1, LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
                            ZoneOffset.UTC
                        ),
                        LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
                            ZoneOffset.UTC
                        )
                    ),
                    TimeEntry(
                        2, 1, LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
                            ZoneOffset.UTC
                        ),
                        LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
                            ZoneOffset.UTC
                        )
                    )
                )
            ),
            {
                // Do nothing
            }
        )
    )


}


class TaskScreenInput(
    val taskScreenUiState: TaskScreenUiState,
    val saveData: () -> Unit
) {
    // Do nothing

}


@Composable
fun TaskScreen(taskScreenModel: TaskScreenModel, saveTask: () -> Unit) {
    val uiState = taskScreenModel.uiState.collectAsState()

    TaskScreen(TaskScreenInput(uiState.value, saveTask))

}


@Preview
@Composable
fun TaskScreen(@PreviewParameter(TaskScreenParameterProvider::class) taskScreenInput: TaskScreenInput) {
    val inputText = remember {
        mutableStateOf(taskScreenInput.taskScreenUiState.taskName)
    }


    Column {

        Row {
            TextField(
                value = taskScreenInput.taskScreenUiState.taskName,
                onValueChange = { inputText.value = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = taskScreenInput.saveData,
                enabled = inputText.value != taskScreenInput.taskScreenUiState.taskName
            ) {
                Text("Save")
            }
        }

    }

}