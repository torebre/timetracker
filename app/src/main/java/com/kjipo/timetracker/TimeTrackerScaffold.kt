package com.kjipo.timetracker

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTrackerScaffold(
    appState: TimeTrackerAppState,
    appContainer: AppContainer
) {

    Scaffold(
        bottomBar = {
            TimeTrackerBottomBar(appState::navigateToScreen)
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

                    TaskScreen(taskScreenModel, {

                        // TODO Implement save functionality

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

                    TimeEntryScreen(uiState)
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
