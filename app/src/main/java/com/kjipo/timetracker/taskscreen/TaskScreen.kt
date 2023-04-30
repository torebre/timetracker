package com.kjipo.timetracker.taskscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.kjipo.timetracker.dateFormatter
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement
import com.kjipo.timetracker.timeFormatter
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset


class TaskScreenParameterProvider : PreviewParameterProvider<TaskScreenInput> {

    override val values = sequenceOf(
        TaskScreenInput(
            mutableStateOf(
                TaskScreenUiState(
                    TaskUi(
                        1,
                        "Task name"
                    ),
                    getExampleTimeEntries(),
                    initialLoading = false
                )
            ),
            { taskName, tags ->
                // Do nothing
            },
            {
                // Do nothing
            }, {
                // Do nothing
            })
    )

    private fun getExampleTimeEntries() =
        listOf(
            TimeEntryUi(
                1,
                LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
                    ZoneOffset.UTC
                ),
                LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
                    ZoneOffset.UTC
                )
            ),
            TimeEntryUi(
                2,
                LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
                    ZoneOffset.UTC
                ),
                LocalDateTime.of(2023, 1, 5, 12, 0, 5).toInstant(
                    ZoneOffset.UTC
                )
            )
        )

}


class TaskScreenInput(
    val taskScreenUiState: State<TaskScreenUiState>,
    val saveData: (String, List<TaskMarkUiElement>) -> Unit,
    val navigateToTimeEditScreen: (Long) -> Unit,
    val deleteTimeEntry: (Long) -> Unit
)

@Composable
fun TaskScreen(
    taskScreenModel: TaskScreenModel,
    saveTask: (String, List<TaskMarkUiElement>) -> Unit,
    navigateToTimeEditScreen: (Long) -> Unit,
    deleteTimeEntry: (Long) -> Unit
) {
    val uiState = taskScreenModel.uiState.collectAsState()

    TaskScreen(
        TaskScreenInput(
            uiState,
            saveTask,
            navigateToTimeEditScreen,
            deleteTimeEntry
        )
    )
}


@Preview(showBackground = true)
@Composable
fun TaskScreen(@PreviewParameter(TaskScreenParameterProvider::class) taskScreenInput: TaskScreenInput) {
    val taskUiState = taskScreenInput.taskScreenUiState.value

    if (taskUiState.initialLoading) {
        return
    }

    Timber.tag("TaskScreen").i("${taskUiState.tags}")

    val inputText = remember {
        mutableStateOf(taskUiState.taskUi.taskName)
    }

    val availableTags = remember {
        mutableStateOf(taskUiState.availableTags)
    }

    val usedTags = remember {
        mutableStateOf(taskUiState.tags)
    }

    val expanded = remember {
        mutableStateOf(false)
    }

    Column {
        Row {
            TextField(
                value = inputText.value,
                onValueChange = { inputText.value = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.h4
            )
        }

        Row {
            usedTags.value.forEach { tagUi ->
                Tag(tagUi) {
                    usedTags.value -= tagUi
                    availableTags.value += tagUi
                }
            }
        }

        Button(
            onClick = {
                expanded.value = !expanded.value
            },
            enabled = taskUiState.availableTags.isNotEmpty()
        ) {
            Text("Add tag")
        }

        if (expanded.value) {
            TagSelectionMenu(
                availableTags = taskUiState.availableTags,
                addTag = { tagUi ->
                    usedTags.value += tagUi
                    expanded.value = false
                },
                expanded
            )
        }

        for (timeEntry in taskUiState.timeEntries) {
            LazyRow {
                item {
                    TimeEntryRow(
                        timeEntry,
                        taskScreenInput.navigateToTimeEditScreen,
                        taskScreenInput.deleteTimeEntry
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    taskScreenInput.saveData(inputText.value, usedTags.value)
                },
                enabled = inputText.value.isNotBlank() && (inputText.value != taskUiState.taskUi.taskName || !usedTags.value.containsAll(
                    taskUiState.tags
                ) || !taskUiState.tags.containsAll(usedTags.value))
            ) {
                Text("Save")
            }
        }

    }

}


private fun getDateTimeText(instant: Instant): String {
    val date = dateFormatter.format(instant.atZone(ZoneId.systemDefault()))
    val time = timeFormatter.format(instant.atZone(ZoneId.systemDefault()))

    return "$date $time"
}

@Composable
fun TimeEntryRow(
    timeEntry: TimeEntryUi,
    navigateToTimeEditScreen: (Long) -> Unit,
    deleteTimeEntry: (Long) -> Unit
) {
    Column(modifier = Modifier.padding(start = 5.dp, top = 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(text = getDateTimeText(timeEntry.start))

            timeEntry.stop?.let {
                Text(getDateTimeText(it))
            }

            Spacer(Modifier.weight(1f))

            IconButton(modifier = Modifier.padding(end = 5.dp),
                onClick = {
                    navigateToTimeEditScreen(timeEntry.id)
                }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit time entry"
                )
            }

            IconButton(modifier = Modifier.padding(end = 5.dp),
                onClick = {
                    deleteTimeEntry(timeEntry.id)
                }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete time entry"
                )
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tag(tagUi: TaskMarkUiElement, removeTag: () -> Unit) {
    InputChip(modifier = Modifier
        .background(
            tagUi.colour ?: androidx.compose.material3.MaterialTheme.colorScheme.background
        )
        .padding(start = 2.dp),
        selected = true,
        onClick = {
            removeTag()
        },
        label = { Text(tagUi.title) },
        trailingIcon = { Icons.Default.Close })

}


@Composable
fun TagSelectionMenu(
    availableTags: List<TaskMarkUiElement>,
    addTag: (TaskMarkUiElement) -> Unit,
    expanded: MutableState<Boolean>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopStart)
    ) {
        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            availableTags.forEach { tagUi ->
                DropdownMenuItem(text = {
                    Text(tagUi.title)
                },
                    onClick = {
                        addTag(tagUi)
                    })

            }
        }


    }


}