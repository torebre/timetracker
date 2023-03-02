package com.kjipo.timetracker.taskscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.kjipo.timetracker.timeFormatter
import timber.log.Timber
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
            )),
            {
                // Do nothing
            },
            {
                // Do nothing
            },
            {
                // Do nothing
            }
        ) {
            // Do nothing
        }
    )


}


class TaskScreenInput(
    val taskScreenUiState: State<TaskScreenUiState>,
    val saveData: (String) -> Unit,
    val navigateToTimeEditScreen: (Long) -> Unit,
    val removeTag: (Long) -> Unit,
    val addTag: (Long) -> Unit
)

@Composable
fun TaskScreen(
    taskScreenModel: TaskScreenModel,
    saveTask: (String) -> Unit,
    navigateToTimeEditScreen: (Long) -> Unit,
    removeTag: (Long) -> Unit,
    addTag: (Long) -> Unit
) {
    val uiState = taskScreenModel.uiState.collectAsState()

    TaskScreen(
        TaskScreenInput(
            uiState,
            saveTask,
            navigateToTimeEditScreen,
            removeTag,
            addTag
        )
    )
}


@Preview
@Composable
fun TaskScreen(@PreviewParameter(TaskScreenParameterProvider::class) taskScreenInput: TaskScreenInput) {
    if (taskScreenInput.taskScreenUiState.value.initialLoading) {
        return
    }

    Timber.tag("TaskScreen").i("${taskScreenInput.taskScreenUiState.value.tags}")

    val inputText = remember {
        mutableStateOf(taskScreenInput.taskScreenUiState.value.taskUi.taskName)
    }

    val tags = remember {
       mutableStateOf(taskScreenInput.taskScreenUiState.value.tags)
    }

    val expanded = remember {
        mutableStateOf(false)
    }


    Column {
        Row {
            TextField(
                value = inputText.value,
                onValueChange = { inputText.value = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row {
            tags.value.forEach { tagUi ->
                Tag(tagUi) {
                    taskScreenInput.removeTag(tagUi.tagId)
                }
            }
        }

        Button(onClick = {
            expanded.value = !expanded.value
        }) {
            Text("Add tag")
        }

        if (expanded.value) {
            TagSelectionMenu(
                availableTags = taskScreenInput.taskScreenUiState.value.availableTags,
                addTag = {
                    taskScreenInput.addTag(it)
                    expanded.value = false
                },
            expanded)
        }


        for (timeEntry in taskScreenInput.taskScreenUiState.value.timeEntries) {
            LazyRow {
                item {
                    TimeEntryRow(timeEntry, taskScreenInput.navigateToTimeEditScreen)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    taskScreenInput.saveData(inputText.value)
                },
                enabled = inputText.value != taskScreenInput.taskScreenUiState.value.taskUi.taskName
            ) {
                Text("Save")
            }
        }

    }

}


@Composable
fun TimeEntryRow(timeEntry: TimeEntryUi, navigateToTimeEditScreen: (Long) -> Unit) {
    Column(modifier = Modifier.padding(start = 5.dp, top = 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            timeEntry.start.let {
                val date = dateFormatter.format(it.atZone(ZoneId.systemDefault()))
                val time = timeFormatter.format(it.atZone(ZoneId.systemDefault()))
                Text("Start: $date $time")
            }

            timeEntry.stop?.let {
                val date = dateFormatter.format(it.atZone(ZoneId.systemDefault()))
                val time = timeFormatter.format(it.atZone(ZoneId.systemDefault()))
                Text("Stop: $date $time")
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
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tag(tagUi: TagUi, removeTag: () -> Unit) {
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
    availableTags: List<TagUi>,
    addTag: (Long) -> Unit,
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
                        addTag(tagUi.tagId)
                    })

            }
        }


    }


}