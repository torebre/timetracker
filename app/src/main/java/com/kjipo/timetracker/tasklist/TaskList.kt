package com.kjipo.timetracker.tasklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kjipo.timetracker.database.Task
import timber.log.Timber


class TaskListInputParameters(val taskListUiState: TaskListUiState, val navigateToTaskScreen: (Long) -> Unit)

class TaskListParameterInputProvider : PreviewParameterProvider<TaskListInputParameters> {
    override val values = sequenceOf(TaskListInputParameters(TaskListUiState()) {
        // Do nothing
    })

}

@Composable
fun TaskList(taskListModel: TaskListModel, navigateToTaskScreen: (Long) -> Unit) {
    val uiState = taskListModel.uiState.collectAsStateWithLifecycle()

    TaskList(TaskListInputParameters(uiState.value, navigateToTaskScreen))

}


@Composable
fun TaskList(@PreviewParameter(TaskListParameterInputProvider::class) taskListInputParameters: TaskListInputParameters) {
    LazyColumn {
        for (task in taskListInputParameters.taskListUiState.tasks) {
            item {
                TaskRow(TaskRowInput(task, taskListInputParameters.navigateToTaskScreen))
            }
        }
    }

}


class TaskRowInput(val task: Task, val navigateToTaskScreen: (Long) -> Unit)

class TaskRowParameterProvider : PreviewParameterProvider<TaskRowInput> {

    override val values = sequenceOf(TaskRowInput(Task(1, "Task 1")) {
        // Do nothing
    })

}

@Preview
@Composable
fun TaskRow(@PreviewParameter(TaskRowParameterProvider::class) taskRowInput: TaskRowInput) {
    Text(
        modifier = Modifier.clickable {

            Timber.tag("Tasklist").i("Task title: ${taskRowInput.task.title}. Task ID: ${taskRowInput.task.taskId}")

            taskRowInput.navigateToTaskScreen(taskRowInput.task.taskId)
        },
        text = taskRowInput.task.title
    )

}