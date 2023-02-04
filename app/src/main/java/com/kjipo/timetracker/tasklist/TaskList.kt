package com.kjipo.timetracker.tasklist

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kjipo.timetracker.database.Task
import androidx.lifecycle.compose.collectAsStateWithLifecycle


class TaskListInputParameters(val taskListUiState: TaskListUiState)

class TaskListParameterInputProvider: PreviewParameterProvider<TaskListInputParameters> {
    override val values = sequenceOf(TaskListInputParameters(TaskListUiState()))

}

@Composable
fun TaskList(taskListModel: TaskListModel) {
    val uiState = taskListModel.uiState.collectAsStateWithLifecycle()

    TaskList(TaskListInputParameters(uiState.value))

}

@Composable
fun TaskList(@PreviewParameter(TaskListParameterInputProvider::class) taskListInputParameters: TaskListInputParameters) {
    LazyColumn {
        for (task in taskListInputParameters.taskListUiState.tasks) {
            item {
                TaskRow(task)
            }
        }
    }

}


@Composable
fun TaskRow(task: Task) {
        Text(task.title)

}