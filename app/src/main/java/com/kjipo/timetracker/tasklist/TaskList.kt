package com.kjipo.timetracker.tasklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kjipo.timetracker.formatDuration
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement


class TaskListInputParameters(
    val taskListUiState: TaskListUiState,
    val navigateToTaskScreen: (Long) -> Unit,
    val toggleStartStop: (Long) -> Unit
)





@Composable
fun TaskList(
    taskListModel: TaskListModel,
    navigateToTaskScreen: (Long) -> Unit,
    toggleStartStop: (Long) -> Unit
) {
    // TODO What does collectAsStateWithLifecycle do?
    val uiState = taskListModel.uiState.collectAsStateWithLifecycle()

    TaskList(TaskListInputParameters(uiState.value, navigateToTaskScreen, toggleStartStop))
}


@Composable
fun TaskList(taskListInputParameters: TaskListInputParameters) {
    val activeTasks = taskListInputParameters.taskListUiState.activeTasks

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.padding(top = 8.dp))

        activeTasks.forEach { activeTaskId ->
            taskListInputParameters.taskListUiState.tasks.find { it.id == activeTaskId }
                ?.let { activeTask ->
                    TaskListEntry(
                        activeTask,
                        taskListInputParameters.navigateToTaskScreen,
                        taskListInputParameters.toggleStartStop,
                        true
                    )
                }
        }

        Spacer(modifier = Modifier.padding(5.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(items = taskListInputParameters.taskListUiState.tasks.filter {
                !activeTasks.contains(
                    it.id
                )
            },
                key = { taskUi ->
                    taskUi.id
                }) { task ->
                TaskListEntry(
                    task,
                    taskListInputParameters.navigateToTaskScreen,
                    taskListInputParameters.toggleStartStop,
                    false
                )
            }
        }
    }
}

@Composable
private fun TaskListEntry(
    taskUi: TaskUi,
    navigateToTaskScreen: (Long) -> Unit,
    toggleStartStop: (Long) -> Unit,
    showAsActive: Boolean
) {
    TaskRow(
        taskUi,
        navigateToTaskScreen,
        toggleStartStop,
        showAsActive
    )
}


@Composable
fun TaskRow(
    task: TaskUi,
    navigateToTaskScreen: (Long) -> Unit,
    toggleStartStop: ((Long) -> Unit)? = null,
    showAsActive: Boolean
) {
    val color = if (showAsActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        modifier = Modifier.padding(bottom = 8.dp, start = 5.dp, end = 5.dp),
        color = color,
        shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth(),
        ) {
            Column {
                Text(
                    modifier = Modifier
                        .clickable {
                            navigateToTaskScreen(task.id)
                        },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        textDecoration = if (task.closed) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    text = task.title
                )

                if (showAsActive) {
                    Row {
                        Text(modifier = Modifier.defaultMinSize(70.dp), text = "Current:")
                        Text(
                            text =
                            task.getCurrentDuration()?.let {
                                formatDuration(it)
                            } ?: ""
                        )
                    }
                }

                Row {
                    Text(modifier = Modifier.defaultMinSize(70.dp), text = "Total:")
                    Text(
                        text = formatDuration(task.computeDurationOfNotOpenEntries())
                    )
                }

                if (task.tags.isNotEmpty()) {
                    Row {
                        task.tags.forEachIndexed { index, tagUi ->
                            val modifier = if (index == 0) {
                                Modifier.padding()
                            } else {
                                Modifier.padding(start = 5.dp)
                            }
                            Tag(tagUi, modifier)
                        }
                    }
                }

                task.project?.let { project ->
                    Row(modifier = Modifier.padding(top = 5.dp)) {
                        Tag(project, Modifier)
                    }
                }

                // TODO Temporarily in its own row here to make the button visible then the title is long
                Row {
                    // This is to push the buttons to the end of the row
                    Spacer(Modifier.weight(1f))

                    toggleStartStop?.let { toggle ->
                        IconButton(modifier = Modifier
                            .padding(end = 5.dp)
                            .align(Alignment.CenterVertically),
                            onClick = {
                                toggle(task.id)
                            }) {
                            if (task.isOngoing()) {
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
                }
            }
        }
    }
}


@Composable
fun Tag(tagUi: TaskMarkUiElement, modifier: Modifier) {
    Tag(tagUi.title, colour = tagUi.colour, modifier = modifier)
}

@Composable
fun Tag(title: String, colour: Color?, modifier: Modifier) {
    Badge(modifier = modifier, containerColor = colour ?: Color.White) {
        Text(title)
    }
}
