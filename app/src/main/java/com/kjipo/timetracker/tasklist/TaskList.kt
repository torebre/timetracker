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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kjipo.timetracker.database.TimeEntry
import com.kjipo.timetracker.formatDuration
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random


class TaskListInputParameters(
    val taskListUiState: TaskListUiState,
    val navigateToTaskScreen: (Long) -> Unit,
    val toggleStartStop: (Long) -> Unit
)


private fun getPreviewTasks(): List<TaskUi> {
    val random = Random(1)

    return (1L until 15).map { id ->
        val timeEntries = getRandomTimeEntries(random.nextInt(3))
        val projectId = random.nextLong(10)

        TaskUi(
            id,
            "Task $id aaaaaaa bbbbbbbb ccccccccccc",
            timeEntries,
            Duration.ofSeconds(
                timeEntries.map { it.getDuration() }.filterNotNull().sumOf { it.seconds }),
            getRandomTags(random.nextInt(4)),
            TaskMarkUiElement(
                projectId,
                "Project $projectId",
                Color.hsl(random.nextInt(360).toFloat(), 1f, 0.5f)
            )
        )
    }
}

private fun getRandomTimeEntries(numberOfTimeEntries: Int): List<TimeEntry> {
    val random = Random(1)
    var timeCounter = LocalDateTime.of(2020, 2, 3, 17, 0, 0).toEpochSecond(ZoneOffset.UTC)
    val timeEntries = mutableListOf<TimeEntry>()

    for (i in 0 until numberOfTimeEntries) {
        val durationInSeconds = random.nextInt(500, 10000)

        timeEntries.add(
            TimeEntry(
                1, 1,
                Instant.ofEpochSecond(timeCounter),
                Instant.ofEpochSecond(timeCounter + durationInSeconds)
            )
        )

        timeCounter += durationInSeconds + random.nextInt(3600, 20000)
    }

    return timeEntries
}

private fun getRandomTags(numberOfTags: Int): MutableList<TaskMarkUiElement> {
    val random = Random(1)
    val tags = mutableListOf<TaskMarkUiElement>()

    for (i in 0L until numberOfTags) {
        tags.add(TaskMarkUiElement(i, "Tag $i", Color.hsl(random.nextInt(360).toFloat(), 1f, 0.5f)))
    }

    return tags
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


@Composable
fun TaskList(taskListInputParameters: TaskListInputParameters) {

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(items = taskListInputParameters.taskListUiState.tasks,
            key = { taskUi ->
                taskUi.id
            }) { task ->
            Surface(
                modifier = Modifier.padding(bottom = 8.dp, start = 5.dp, end = 5.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp),
            ) {
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
}


class TaskRowInput(
    val task: TaskUi,
    val navigateToTaskScreen: (Long) -> Unit,
    val toggleStartStop: (Long) -> Unit
)


@Composable
fun TaskRow(taskRowInput: TaskRowInput) {

    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            modifier = Modifier
                .clickable {
                    taskRowInput.navigateToTaskScreen(taskRowInput.task.id)
                }
                .fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            text = taskRowInput.task.title
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Column {
                Row {
                    Text(modifier = Modifier.defaultMinSize(70.dp), text = "Current:")
                    Text(
                        text =
                        taskRowInput.task.getCurrentDuration()?.let {
                            formatDuration(it)
                        } ?: ""
                    )
                }

                Row {
                    Text(modifier = Modifier.defaultMinSize(70.dp), text = "Total:")
                    Text(
                        text = formatDuration(taskRowInput.task.computeDurationOfNotOpenEntries())
                    )
                }
            }

            // This is to push the buttons to the end of the row
            Spacer(Modifier.weight(1f))

            IconButton(modifier = Modifier.padding(end = 5.dp),
                onClick = {
                    taskRowInput.toggleStartStop(taskRowInput.task.id)
                }) {
                if (taskRowInput.task.isOngoing()) {
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
            taskRowInput.task.tags.forEachIndexed { index, tagUi ->
                val modifier = if (index == 0) {
                    Modifier.padding()
                } else {
                    Modifier.padding(start = 5.dp)
                }
                Tag(tagUi, modifier)
            }
        }

        Row(modifier = Modifier.padding(top = 5.dp)) {
            taskRowInput.task.project?.let {
                Tag(it, Modifier)
            }
        }
    }
}


@Composable
fun Tag(tagUi: TaskMarkUiElement, modifier: Modifier) {
    Badge(modifier = modifier, containerColor = tagUi.colour ?: Color.White) {
        Text(tagUi.title)
    }
}


@Preview(showBackground = true)
@Composable
private fun ShowPreview() {
    val previewTasks = getPreviewTasks()
    TaskList(TaskListInputParameters(TaskListUiState(previewTasks), {

    }, {

    }))
}