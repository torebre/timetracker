package com.kjipo.timetracker

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kjipo.timetracker.tasklist.TaskList
import com.kjipo.timetracker.tasklist.TaskListModel
import com.kjipo.timetracker.taskscreen.TaskScreen
import com.kjipo.timetracker.taskscreen.TaskScreenModel
import com.kjipo.timetracker.timeentryscreen.TimeEntryEditUiState
import com.kjipo.timetracker.timeentryscreen.TimeEntryScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTrackerScaffold(
    appState: TimeTrackerAppState,
    appContainer: AppContainer
) {

    Scaffold(
        bottomBar = {
            TimeTrackerBottomBar(appState::navigateToScreen)
        },
        floatingActionButton = {
            AddTaskButton(appState.taskScreenShowing) {
                appState.navigateToScreen("${Screens.TASK.name}/0")
            }
        }

    ) { paddingValues ->
        NavHost(
            navController = appState.navController,
            startDestination = Screens.TASKS.name,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screens.TASKS.name) {
                val taskListModel: TaskListModel = viewModel(
                    factory = TaskListModel.provideFactory(
                        appContainer.taskRepository
                    )
                )

                Timber.tag("Navigation").i("Creating new TaskList. Model: ${taskListModel}")
                taskListModel.refresh()

                TaskList(taskListModel, { taskId ->
                    appState.navigateToScreen("${Screens.TASK.name}/$taskId")
                },
                    { taskId ->
                        taskListModel.toggleStartStop(taskId)
                    }
                )
            }

            composable(Screens.REPORTS.name) {

                // TODO
                Text("Reports")

            }

            composable(
                "${Screens.TASK.name}/{taskId}",
                arguments = listOf(navArgument("taskId") {
                    type = NavType.LongType
                })
            ) { navBackStackEntry ->
                navBackStackEntry.arguments?.getLong("taskId")?.let { taskId ->
                    val taskScreenModel: TaskScreenModel = viewModel(
                        factory = TaskScreenModel.provideFactory(
                            taskId,
                            appContainer.taskRepository
                        )
                    )

                    Timber.tag("Navigation").i("Creating new TaskScreen. Model: ${taskScreenModel}")

                    TaskScreen(taskScreenModel, { title ->
                        taskScreenModel.saveTask(title)
                    },
                        { timeEntryId ->
                            appState.navigateToScreen("${Screens.TIME_ENTRY_EDIT.name}/$timeEntryId")
                        })
                }
            }

            composable(
                "${Screens.TIME_ENTRY_EDIT.name}/{timeEntryId}",
                arguments = listOf(navArgument("timeEntryId") {
                    type = NavType.LongType
                })
            ) { navBackStackEntry ->
                val coroutineScope = rememberCoroutineScope()

                navBackStackEntry.arguments?.getLong("timeEntryId")?.let { timeEntryId ->
                    val uiState by produceState(initialValue = TimeEntryEditUiState(waiting = true)) {

                        coroutineScope.launch(Dispatchers.IO) {
                            val timeEntry = appContainer.taskRepository.getTimeEntry(timeEntryId)
                            value = TimeEntryEditUiState(timeEntry = timeEntry)
                        }
                    }

                    TimeEntryScreen(uiState, { timeEntry ->
                        coroutineScope.launch(Dispatchers.IO) {
                            appContainer.taskRepository.updateTimeEntry(timeEntry)
                            coroutineScope.launch(Dispatchers.Main) {
                                appState.navigateToScreen("${Screens.TASK.name}/${timeEntry.taskId}")
                            }
                        }
                    },
                        {
                            coroutineScope.launch(Dispatchers.IO) {
                                appContainer.taskRepository.getTimeEntry(timeEntryId)?.let {
                                    coroutineScope.launch(Dispatchers.Main) {
                                        appState.navigateToScreen("${Screens.TASK.name}/${it.taskId}")
                                    }
                                }
                            }
                        })
                }
            }
        }
    }

}


@Composable
fun TimeTrackerBottomBar(
    navigateToRoute: (String) -> Unit,
) {
    Row {
        Button(onClick = {
            navigateToRoute(Screens.TASKS.name)
        }) {
            Text("Tasks")
        }

        Button(onClick = {
            navigateToRoute(Screens.REPORTS.name)
        }) {
            Text("Reports")
        }

    }

}

@Composable
fun AddTaskButton(taskScreenShowing: MutableState<Boolean>, addTask: () -> Unit) {
    if (taskScreenShowing.value) {
        FloatingActionButton(
            onClick = addTask,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Add task",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}