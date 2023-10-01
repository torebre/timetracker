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
import com.kjipo.timetracker.export.ExportScreen
import com.kjipo.timetracker.reports.ReportScreen
import com.kjipo.timetracker.reports.ReportsModel
import com.kjipo.timetracker.taglistscreen.TagModel
import com.kjipo.timetracker.taglistscreen.TagListScreen
import com.kjipo.timetracker.tagscreen.TaskMarkElementScreen
import com.kjipo.timetracker.tagscreen.TagScreenModel
import com.kjipo.timetracker.tasklist.TaskList
import com.kjipo.timetracker.tasklist.TaskListModel
import com.kjipo.timetracker.taskscreen.TaskScreen
import com.kjipo.timetracker.taskscreen.TaskScreenModel
import com.kjipo.timetracker.timeentryscreen.TimeEntryEditUiState
import com.kjipo.timetracker.timeentryscreen.TimeEntryScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun TimeTrackerScaffold(
    appState: TimeTrackerAppState,
    appContainer: AppContainer
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val drawerItems = listOf(Screens.PROJECTS, Screens.TAGS, Screens.REPORTS, Screens.EXPORT)
    val scope = rememberCoroutineScope()
    val selectedItem = remember { mutableStateOf(drawerItems[0]) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                drawerItems.forEach { drawerItem ->
                    NavigationDrawerItem(label = {
                        Text(drawerItem.name)
                    },
                        selected = drawerItem == selectedItem.value,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                selectedItem.value = drawerItem
                                appState.navigateToScreen(drawerItem.name)
                            }
                        })
                }

                NavigationDrawerItem(label = {
                    Text("Setup test data")
                },
                    selected = false,
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            appContainer.appDatabase.clearAllTables()
                            addTestData(appContainer.appDatabase)
                        }
                    })

                NavigationDrawerItem(label = {
                    Text("Clear database")
                },
                    selected = false,
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            appContainer.appDatabase.clearAllTables()
                        }
                    })
            }
        })
    {
        MainContentScaffold(appState, appContainer)
    }

}


@Composable
private fun MainContentScaffold(
    appState: TimeTrackerAppState,
    appContainer: AppContainer
) {
    Scaffold(
        bottomBar = {
            TimeTrackerBottomBar(appState::navigateToScreen)
        },
        floatingActionButton = {
            AddTaskButton(appState.screenShowing,
                {
                    appState.navigateToScreen("${Screens.TASK.name}/0")
                },
                {
                    appState.navigateToScreen("${Screens.TAG.name}/0")
                },
                {
                    appState.navigateToScreen("${Screens.PROJECT.name}/0")
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = appState.navController,
            startDestination = Screens.TASKS.name,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screens.TASKS.name) {
                GoToTasksScreen(appContainer, appState)
            }

            composable(Screens.REPORTS.name) {
                val reportsModel = ReportsModel(appContainer.taskRepository)
                ReportScreen(reportsModel)
            }

            composable(
                "${Screens.TASK.name}/{taskId}",
                arguments = listOf(navArgument("taskId") {
                    type = NavType.LongType
                })
            ) { navBackStackEntry ->
                GoToTaskScreen(navBackStackEntry, appContainer, appState)
            }

            composable(
                "${Screens.TIME_ENTRY_EDIT.name}/{timeEntryId}",
                arguments = listOf(navArgument("timeEntryId") {
                    type = NavType.LongType
                })
            ) { navBackStackEntry ->
                GoToTimeEntryScreen(navBackStackEntry, appContainer, appState)
            }

            composable(Screens.TAGS.name) {
                GoToTaskMarkersList(true, appContainer, appState)
            }

            composable(Screens.PROJECTS.name) {
                GoToTaskMarkersList(false, appContainer, appState)
            }

            composable(
                "${Screens.TAG.name}/{tagId}",
                arguments = listOf(navArgument("tagId") {
                    type = NavType.LongType
                })
            ) { navBackStackEntry ->
                GoToTaskMarkerScreen(
                    appContainer,
                    true,
                    navBackStackEntry.arguments?.getLong("tagId"),
                    appState
                )
            }

            composable(
                "${Screens.PROJECT.name}/{projectId}",
                arguments = listOf(navArgument("projectId") {
                    type = NavType.LongType
                })
            ) { navBackStackEntry ->
                GoToTaskMarkerScreen(
                    appContainer,
                    false,
                    navBackStackEntry.arguments?.getLong("projectId"),
                    appState
                )
            }

            composable(Screens.EXPORT.name) {
                ExportScreen()
            }
        }
    }

}

@Composable
private fun GoToTaskMarkerScreen(
    appContainer: AppContainer,
    isTag: Boolean,
    elementId: Long?,
    appState: TimeTrackerAppState
) {
    val taskMarkerModel: TagScreenModel = viewModel(
        factory = TagScreenModel.provideFactory(
            isTag,
            appContainer.taskRepository
        )
    )

    elementId?.let {
        taskMarkerModel.setCurrentTag(it)
    }

    TaskMarkElementScreen(taskMarkerModel) {
        if (isTag) {
            appState.navigateToScreen(Screens.TAGS.name)
        } else {
            appState.navigateToScreen(Screens.PROJECTS.name)
        }
    }
}

@Composable
private fun GoToTaskMarkersList(
    isTag: Boolean,
    appContainer: AppContainer,
    appState: TimeTrackerAppState
) {
    val tagModel: TagModel = viewModel(
        factory = TagModel.provideFactory(
            isTag,
            appContainer.taskRepository
        )
    )

    TagListScreen(tagModel) {
        if (isTag) {
            appState.navigateToScreen("${Screens.TAG.name}/${it}")
        } else {
            appState.navigateToScreen("${Screens.PROJECT.name}/${it}")
        }
    }
}


@Composable
private fun GoToTasksScreen(
    appContainer: AppContainer,
    appState: TimeTrackerAppState
) {
    val taskListModel: TaskListModel = viewModel(
        factory = TaskListModel.provideFactory(
            appContainer.taskRepository
        )
    )
    taskListModel.refresh()

    TaskList(taskListModel, { taskId ->
        appState.navigateToScreen("${Screens.TASK.name}/$taskId")
    },
        { taskId ->
            taskListModel.toggleStartStop(taskId)
        }
    )
}

@Composable
private fun GoToTaskScreen(
    navBackStackEntry: NavBackStackEntry,
    appContainer: AppContainer,
    appState: TimeTrackerAppState
) {
    navBackStackEntry.arguments?.getLong("taskId")?.let { taskId ->
        val taskScreenModel: TaskScreenModel = viewModel(
            factory = TaskScreenModel.provideFactory(
                taskId,
                appContainer.taskRepository
            )
        )

        TaskScreen(taskScreenModel, { title, tags, project ->
            taskScreenModel.saveTask(title, tags, project)
        },
            { timeEntryId ->
                appState.navigateToScreen("${Screens.TIME_ENTRY_EDIT.name}/$timeEntryId")
            },
            { timeEntryId ->
                taskScreenModel.deleteTimeEntry(timeEntryId)
            })
    }

}

@Composable
private fun GoToTimeEntryScreen(
    navBackStackEntry: NavBackStackEntry,
    appContainer: AppContainer,
    appState: TimeTrackerAppState
) {
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

//        Button(onClick = {
//            navigateToRoute(Screens.TAGS.name)
//        }) {
//            Text("Tags")
//        }
//
//        Button(onClick = {
//            navigateToRoute(Screens.PROJECTS.name)
//        }) {
//            Text("Projects")
//        }
    }

}

@Composable
fun AddTaskButton(
    taskScreenShowing: MutableState<Screens?>,
    addTask: () -> Unit,
    addTag: () -> Unit,
    addProject: () -> Unit
) {
    when (taskScreenShowing.value) {
        Screens.TASKS -> {
            FloatingAddButton(contentDescription = "Add task", onClickHandler = addTask)
        }

        Screens.TAGS -> {
            FloatingAddButton(contentDescription = "Add tag", onClickHandler = addTag)
        }

        Screens.PROJECTS -> {
            FloatingAddButton(contentDescription = "Add project", onClickHandler = addProject)
        }

        else -> {
            // Do not add a floating button
        }
    }
}


@Composable
fun FloatingAddButton(contentDescription: String, onClickHandler: () -> Unit) {
    FloatingActionButton(
        onClick = onClickHandler,
        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ) {
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp)
        )
    }

}
