package com.kjipo.timetracker.tasklist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kjipo.timetracker.taskscreen.TagUi
import com.kjipo.timetracker.toHoursPartHelper
import com.kjipo.timetracker.toMinutesPartHelper
import com.kjipo.timetracker.toSecondsPartHelper
import com.kjipo.timetracker.toTwoDigits
import java.time.Duration


class TaskListInputParameters(
    val taskListUiState: TaskListUiState,
    val navigateToTaskScreen: (Long) -> Unit,
    val toggleStartStop: (Long) -> Unit
)

class TaskListParameterInputProvider : PreviewParameterProvider<TaskListInputParameters> {
    override val values = sequenceOf(
        TaskListInputParameters(TaskListUiState(getPreviewTasks()), {
            // Do nothing
        },
            {
                // Do nothing
            })
    )

}


private fun getPreviewTasks(): List<TaskUi> {
    return (1L until 20).map { id ->
        TaskUi(id, "Task $id", Duration.ofMinutes(10), id.mod(2) == 0)
    }
}


@Composable
fun TaskList(
    taskListModel: TaskListModel,
    navigateToTaskScreen: (Long) -> Unit,
    toggleStartStop: (Long) -> Unit
) {
    val uiState = taskListModel.uiState.collectAsStateWithLifecycle()

    TaskList(TaskListInputParameters(uiState.value, navigateToTaskScreen, toggleStartStop))
}


@Preview(showBackground = true)
@Composable
fun TaskList(@PreviewParameter(TaskListParameterInputProvider::class) taskListInputParameters: TaskListInputParameters) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(items = taskListInputParameters.taskListUiState.tasks,
            key = { taskUi ->
                taskUi.id
            }) { task ->
            TaskRow(
                TaskRowInput(
                    task,
                    taskListInputParameters.navigateToTaskScreen,
                    taskListInputParameters.toggleStartStop
                )
            )

        }
    }
}


class TaskRowInput(
    val task: TaskUi,
    val navigateToTaskScreen: (Long) -> Unit,
    val toggleStartStop: (Long) -> Unit
)

class TaskRowParameterProvider : PreviewParameterProvider<TaskRowInput> {

    override val values = sequenceOf(
        TaskRowInput(TaskUi(1, "Task 1", Duration.ofMinutes(10), false), {
            // Do nothing
        },
            {
                // Do nothing
            })
    )

}

@Preview(showBackground = true)
@Composable
fun TaskRow(@PreviewParameter(TaskRowParameterProvider::class) taskRowInput: TaskRowInput) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .clickable {
                    taskRowInput.navigateToTaskScreen(taskRowInput.task.id)
                }
                .width(120.dp)
                .padding(start = 5.dp),
            text = taskRowInput.task.title
        )

        val durationText = with(taskRowInput.task.duration) {
            "${toTwoDigits(toHoursPartHelper())}:${toTwoDigits(toMinutesPartHelper())}:${
                toTwoDigits(
                    toSecondsPartHelper()
                )
            }"
        }

        Text(
            text = durationText
        )

        // This is to push the buttons to the end of the row
        Spacer(Modifier.weight(1f))

        IconButton(modifier = Modifier.padding(end = 5.dp),
            onClick = {
                taskRowInput.toggleStartStop(taskRowInput.task.id)
            }) {
            if (taskRowInput.task.ongoing) {
                // TODO Use better icon
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Stop"
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start"
                )
            }
        }
    }

    Row {
        taskRowInput.task.tags.forEach { tagUi ->
            Tag(tagUi)
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tag(tagUi: TagUi) {
    AssistChip(modifier = Modifier
        .background(
            tagUi.colour ?: MaterialTheme.colorScheme.background
        )
        .padding(1.dp),
        onClick = {
                  // Do nothing
        },
        label = { Text(tagUi.title) },
        trailingIcon = { Icons.Default.Close })

}
