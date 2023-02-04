package com.kjipo.timetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kjipo.timetracker.taskscreen.TaskScreenUiState


class TaskScreenParameterProvider : PreviewParameterProvider<TaskScreenInput> {

    override val values = sequenceOf(
        TaskScreenInput(
            TaskScreenUiState(
                "Task name",
                true
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