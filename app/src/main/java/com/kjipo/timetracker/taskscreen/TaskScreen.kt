package com.kjipo.timetracker.taskscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults.inputChipColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kjipo.timetracker.FloatingAddButton
import com.kjipo.timetracker.database.TimeEntry
import com.kjipo.timetracker.dateFormatter
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement
import com.kjipo.timetracker.timeFormatter
import com.kjipo.timetracker.timeentryscreen.TimeEntryDialog
import com.kjipo.timetracker.timeentryscreen.TimeEntryEditUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId


class TaskScreenInput(
    val taskScreenUiState: State<TaskScreenUiState>,
    val saveData: (String, List<TaskMarkUiElement>, TaskMarkUiElement?) -> Unit,
    val deleteTimeEntry: (Long) -> Unit,
    val addTimeEntry: (start: Instant, stop: Instant?) -> Unit,
    val getTimeEntry: suspend (Long) -> TimeEntry?,
    val updateTimeEntry: (value: Long, start: Instant, stop: Instant?) -> Unit
)

@Composable
fun TaskScreen(
    taskScreenModel: TaskScreenModel
) {
    TaskScreen(taskScreenModel,
        { title, tags, project ->
            taskScreenModel.saveTask(title, tags, project)
        },
        { timeEntryId ->
            taskScreenModel.deleteTimeEntry(timeEntryId)
        }, { start, stop ->
            taskScreenModel.addTimeEntry(start, stop)
        }
    )

}

@Composable
fun TaskScreen(
    taskScreenModel: TaskScreenModel,
    saveTask: (String, List<TaskMarkUiElement>, TaskMarkUiElement?) -> Unit,
    deleteTimeEntry: (Long) -> Unit,
    addTimeEntry: (start: Instant, stop: Instant?) -> Unit,
) {
    val uiState = taskScreenModel.uiState.collectAsState()

    TaskScreen(
        TaskScreenInput(
            uiState,
            saveTask,
            deleteTimeEntry,
            addTimeEntry,
            { timeEntryId ->
                taskScreenModel.getTimeEntry(timeEntryId)
            },
            { timeEntryId, start, stop ->
                taskScreenModel.updateTimeEntry(timeEntryId, start, stop)
            }
        )
    )
}


@Composable
fun TaskScreen(taskScreenInput: TaskScreenInput) {
    val taskUiState = taskScreenInput.taskScreenUiState.value

    if (taskUiState.initialLoading) {
        return
    }

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

    val expandedProject = remember {
        mutableStateOf(false)
    }

    val selectedProject = remember {
        mutableStateOf(taskUiState.project)
    }

    val showDialog = remember { mutableStateOf(false) }

    val timeEntryEditUiState = remember {
        mutableStateOf(TimeEntryEditUiState(waiting = false))
    }

    val editTimeEntry = remember {
        mutableLongStateOf(0)
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(floatingActionButton =
    {
        FloatingAddButton(contentDescription = "Add time entry", onClickHandler = {
            editTimeEntry.longValue = 0L
            showDialog.value = true
        })
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row {
                TextField(
                    value = inputText.value,
                    onValueChange = { inputText.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.headlineSmall
                )
            }

            Row {
                usedTags.value.forEachIndexed { index, tagUi ->
                    val modifier = if (index == 0) {
                        Modifier
                    } else {
                        Modifier.padding(start = 5.dp)
                    }
                    Tag(tagUi, modifier) {
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
                    availableElements = taskUiState.availableTags,
                    addElement = { tagUi ->
                        usedTags.value += tagUi
                        expanded.value = false
                    },
                    expanded
                )
            }

            Row {
                selectedProject.value?.let {
                    Tag(it, Modifier) {
                        selectedProject.value = null
                    }
                }
            }

            Button(
                onClick = {
                    expandedProject.value = !expandedProject.value
                },
                enabled = taskUiState.availableProjects.isNotEmpty() && selectedProject.value == null
            ) {
                Text("Set project")
            }

            if (expandedProject.value) {
                TagSelectionMenu(
                    availableElements = taskUiState.availableProjects,
                    addElement = { projectElement ->
                        selectedProject.value = projectElement
                        expandedProject.value = false
                    },
                    expandedProject
                )
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items = taskUiState.timeEntries,
                    key = { timeEntry ->
                        timeEntry.id
                    }) { timeEntry ->
                    TimeEntryRow(
                        timeEntry,
                        {
                            timeEntryEditUiState.value =
                                timeEntryEditUiState.value.copy(waiting = true)
                            editTimeEntry.longValue = timeEntry.id

                            coroutineScope.launch(Dispatchers.IO) {
                                taskScreenInput.getTimeEntry(timeEntry.id)?.let {
                                    timeEntryEditUiState.value =
                                        timeEntryEditUiState.value.copy(
                                            waiting = false,
                                            timeEntry = it
                                        )
                                }
                                showDialog.value = true
                            }
                        },
                        taskScreenInput.deleteTimeEntry
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        taskScreenInput.saveData(
                            inputText.value,
                            usedTags.value,
                            selectedProject.value
                        )
                    },
                    enabled = inputText.value.isNotBlank()
                            && (inputText.value != taskUiState.taskUi.taskName
                            || !usedTags.value.containsAll(
                        taskUiState.tags
                    ) || !taskUiState.tags.containsAll(usedTags.value)
                            || selectedProject.value != taskUiState.project)
                ) {
                    Text("Save")
                }
            }
        }

        if (showDialog.value) {
            TimeEntryDialog(
                timeEntryEditUiState,
                setShowDialog = { show ->
                    showDialog.value = show
                }, addTimeEntry = { start, stop ->
                    if (editTimeEntry.longValue != 0L) {
                        taskScreenInput.updateTimeEntry(editTimeEntry.longValue, start, stop)
                    } else {
                        taskScreenInput.addTimeEntry(start, stop)
                    }
                })
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


    Surface(
        modifier = Modifier.padding(bottom = 5.dp, start = 5.dp, end = 5.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.padding(
                    start = 5.dp,
                    end = 5.dp,
                    top = 5.dp,
                    bottom = 5.dp
                )
            ) {
                Text(text = getDateTimeText(timeEntry.start))

                timeEntry.stop?.let {
                    Text(getDateTimeText(it))
                }
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


@Composable
fun Tag(tagUi: TaskMarkUiElement, modifier: Modifier, removeTag: () -> Unit) {
    InputChip(modifier = modifier,
        selected = true,
        enabled = true,
        onClick = {
            removeTag()
        },
        colors = inputChipColors(
            selectedContainerColor = tagUi.colour
                ?: MaterialTheme.colorScheme.background
        ),
        label = { Text(tagUi.title) },
        trailingIcon = { Icons.Default.Close })
}


@Composable
fun TagSelectionMenu(
    availableElements: List<TaskMarkUiElement>,
    addElement: (TaskMarkUiElement) -> Unit,
    expanded: MutableState<Boolean>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopStart)
    ) {
        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            availableElements.forEach { tagUi ->
                DropdownMenuItem(text = {
                    Text(tagUi.title)
                },
                    onClick = {
                        addElement(tagUi)
                    })
            }
        }

    }

}
