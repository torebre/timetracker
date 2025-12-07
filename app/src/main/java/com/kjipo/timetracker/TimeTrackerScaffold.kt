package com.kjipo.timetracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kjipo.timetracker.export.ExportScreen
import com.kjipo.timetracker.reports.ReportScreen
import com.kjipo.timetracker.reports.ReportsModel
import com.kjipo.timetracker.taskmarkelementlistscreen.TaskMarkerModel
import com.kjipo.timetracker.taskmarkelementlistscreen.TagListScreen
import com.kjipo.timetracker.tagscreen.TaskMarkElementScreen
import com.kjipo.timetracker.tagscreen.TagScreenModel
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement
import com.kjipo.timetracker.tasklist.SortOrder
import com.kjipo.timetracker.tasklist.TaskList
import com.kjipo.timetracker.tasklist.TaskListModel
import com.kjipo.timetracker.taskscreen.TaskScreen
import com.kjipo.timetracker.taskscreen.TaskScreenModel
import com.kjipo.timetracker.timeentryscreen.TimeEntryEditUiState
import com.kjipo.timetracker.timeentryscreen.TimeEntryScreen
import com.kjipo.timetracker.weekview.WeekViewModel
import com.kjipo.timetracker.weekview.WeekViewScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


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
            SetupModalDrawer(drawerItems, selectedItem, scope, drawerState, appState, appContainer)
        })
    {
        MainContentScaffold(appState, appContainer)
    }
}

@Composable
private fun SetupModalDrawer(
    drawerItems: List<Screens>,
    selectedItem: MutableState<Screens>,
    scope: CoroutineScope,
    drawerState: DrawerState,
    appState: TimeTrackerAppState,
    appContainer: AppContainer
) {
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
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContentScaffold(
    appState: TimeTrackerAppState,
    appContainer: AppContainer
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterModal by remember { mutableStateOf(false) }

    var showTagSelection by remember { mutableStateOf(false) }

    val taskListModel: TaskListModel = viewModel(
        factory = TaskListModel.provideFactory(
            appContainer.taskRepository
        )
    )

    val reportsModel: ReportsModel = viewModel(
        factory = ReportsModel.provideFactory(appContainer.taskRepository)
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Top bar") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ), actions = {
                    if (appState.screenShowing.value == Screens.TASKS) {
                        Button(onClick = {
                            showFilterModal = true
                            // TODO

                        }) {
                            Text("Filter")
                        }
                        Button(onClick = {
                            showSortMenu = true
                        }) {
                            Text("Sort")
                        }
                    } else if (appState.screenShowing.value == Screens.REPORTS) {
                        Button(onClick = {
                            showTagSelection = true
                        }) { Text("Tags") }
                    }
                    Button(onClick = {
                        showBottomSheet = true
                    }) {
                        Text("Test")
                    }
                })
        },
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
                },
//                {
//                    appState.navigateToScreen("${Screens.TIME_ENTRY_EDIT.name}/0")
//                }
            )
        }
    ) { paddingValues ->
        SetupNavHost(appState, paddingValues, appContainer, taskListModel, reportsModel)

        if (showFilterModal) {
            val uiState = taskListModel.uiState.collectAsStateWithLifecycle()
            FilterModal(
                availableFilters = uiState.value.availableFilters,
                initialSelectedFilters = uiState.value.selectedFilters,
                onApply = { filters ->
                    taskListModel.updateFilter(filters)
                },
                setShowDialog = { show ->
                    showFilterModal = show
                }
            )
        }

        if (showTagSelection) {
            // TODO

        }

        if (showSortMenu) {
            DropdownMenu(expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }) {
                DropdownMenuItem(text = {
                    Text("Recently used")
                },
                    onClick = {
                        taskListModel.setSortOrder(SortOrder.RECENTLY_USED)
                        showSortMenu = false
                    })
                DropdownMenuItem(text = {
                    Text("Default")
                },
                    onClick = {
                        taskListModel.setSortOrder(SortOrder.DEFAULT)
                        showSortMenu = false
                    })
            }

        }

        if (showBottomSheet) {
            ModalBottomSheet(onDismissRequest = {
                showBottomSheet = false
            }, sheetState = sheetState) {
                Button(onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }

                    }
                }) {
                    Text("Hide bottom sheet")
                }
            }
        }
    }

}

@Composable
private fun SetupNavHost(
    appState: TimeTrackerAppState,
    paddingValues: PaddingValues,
    appContainer: AppContainer,
    taskListModel: TaskListModel,
    reportsModel: ReportsModel
) {
    NavHost(
        navController = appState.navController,
        startDestination = Screens.TASKS.name,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screens.TASKS.name) {
            GoToTasksScreen(appContainer, appState, taskListModel)
        }

        composable(Screens.REPORTS.name) {
            ReportScreen(reportsModel)
        }

        composable(Screens.WEEKVIEW.name) {
            val weekViewModel = WeekViewModel(appContainer.taskRepository)
            WeekViewScreen(weekViewModel)
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
            ExportScreen(appContainer.taskRepository)
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

    TaskMarkElementScreen(taskMarkerModel, navigateToElementList = {
        if (isTag) {
            appState.navigateToScreen(Screens.TAGS.name)
        } else {
            appState.navigateToScreen(Screens.PROJECTS.name)
        }
    })
}

@Composable
private fun GoToTaskMarkersList(
    isTag: Boolean,
    appContainer: AppContainer,
    appState: TimeTrackerAppState
) {
    val tagModel: TaskMarkerModel = viewModel(
        factory = TaskMarkerModel.provideFactory(
            isTag,
            appContainer.taskRepository
        )
    )

    TagListScreen(tagModel, goToTagScreen = { id: Long ->
        Timber.tag("TaskMarkerList").i("Going to element $id")

        if (isTag) {
            appState.navigateToScreen("${Screens.TAG.name}/${id}")
        } else {
            appState.navigateToScreen("${Screens.PROJECT.name}/${id}")
        }
    })
}


@Composable
private fun GoToTasksScreen(
    appContainer: AppContainer,
    appState: TimeTrackerAppState,
    taskListModel: TaskListModel
) {
//    val taskListModel: TaskListModel = viewModel(
//        factory = TaskListModel.provideFactory(
//            appContainer.taskRepository
//        )
//    )
    taskListModel.refresh()

    TaskList(taskListModel,
        navigateToTaskScreen = { taskId ->
            appState.navigateToScreen("${Screens.TASK.name}/$taskId")
        },
        toggleStartStop = { taskId ->
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
        TaskScreen(taskScreenModel)
    }

}

@Composable
private fun GoToTimeEntryScreen(
    navBackStackEntry: NavBackStackEntry,
    appContainer: AppContainer,
    appState: TimeTrackerAppState
) {
    val coroutineScope = rememberCoroutineScope()

    val timeEntryId = navBackStackEntry.arguments?.getLong("timeEntryId")
    if (timeEntryId != null && timeEntryId != 0L) {
        val uiState by produceState(initialValue = TimeEntryEditUiState(waiting = true),
            producer = {
                coroutineScope.launch(Dispatchers.IO) {
                    val timeEntry = appContainer.taskRepository.getTimeEntry(timeEntryId)
                    value = TimeEntryEditUiState(timeEntry = timeEntry)
                }
            })

        TimeEntryScreen(uiState, updateOrCreateEntry = { currentTimeEntryId, start, stop ->
            coroutineScope.launch(Dispatchers.IO) {
                // On this time entry screen the timeEntry should not be null since an existing entry is being edited
                currentTimeEntryId?.let {
                    appContainer.taskRepository.updateTimeEntry(currentTimeEntryId, start, stop)
                        .let { updatedTimeEntry ->
                            if (updatedTimeEntry != null) {
                                coroutineScope.launch(Dispatchers.Main) {
                                    appState.navigateToScreen("${Screens.TASK.name}/${updatedTimeEntry.taskId}")
                                }
                            }
                        }
                }
            }
        },
            cancel = {
                coroutineScope.launch(Dispatchers.IO) {
                    val timeEntry = appContainer.taskRepository.getTimeEntry(timeEntryId)

                    if (timeEntry != null) {
                        coroutineScope.launch(Dispatchers.Main) {
                            appState.navigateToScreen("${Screens.TASK.name}/${timeEntry.taskId}")
                        }
                    } else {
                        coroutineScope.launch(Dispatchers.Main) {
                            appState.navController.popBackStack()
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
        Button(modifier = Modifier.padding(start = 5.dp), onClick = {
            navigateToRoute(Screens.TASKS.name)
        }) {
            Text("Tasks")
        }

        Button(modifier = Modifier.padding(start = 5.dp), onClick = {
            navigateToRoute(Screens.REPORTS.name)
        }) {
            Text("Reports")
        }

        Button(modifier = Modifier.padding(start = 5.dp), onClick = {
            navigateToRoute(Screens.WEEKVIEW.name)
        }) {
            Text("Week")
        }

    }

}

@Composable
fun AddTaskButton(
    taskScreenShowing: MutableState<Screens?>,
    addTask: () -> Unit,
    addTag: () -> Unit,
    addProject: () -> Unit,
//    addTimeEntry: () -> Unit
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

//        Screens.TASK -> {
//            FloatingAddButton(contentDescription = "Add time entry", onClickHandler = addTimeEntry)
//        }

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


@Composable
fun FilterModal(
    availableFilters: List<TaskMarkUiElement>,
    initialSelectedFilters: List<TaskMarkUiElement>,
    onApply: (List<TaskMarkUiElement>) -> Unit,
    setShowDialog: (Boolean) -> Unit,
) {
    var selectedFilters by remember { mutableStateOf(initialSelectedFilters) }

    Dialog(onDismissRequest = { setShowDialog(false) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Filter by Tags/Projects",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 300.dp)
                ) {
                    items(availableFilters.size) { filter ->
                        val currentFilter = availableFilters[filter]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFilters = if (selectedFilters.contains(currentFilter)) {
                                        selectedFilters - currentFilter
                                    } else {
                                        selectedFilters + currentFilter
                                    }
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = selectedFilters.contains(currentFilter),
                                onCheckedChange = { checked ->
                                    selectedFilters = if (checked) {
                                        selectedFilters + currentFilter
                                    } else {
                                        selectedFilters - currentFilter
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(currentFilter.title)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { selectedFilters = emptyList() }) {
                        Text("Clear All")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { setShowDialog(false) }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onApply(selectedFilters)
                        setShowDialog(false)
                    }) {
                        Text("Apply")
                    }
                }
            }
        }
    }

}

@Composable
fun TagSelectionModal(
    tags: List<String>,
    updateTagFilterList: (List<String>) -> Unit,
    setShowDialog: (Boolean) -> Unit,
) {
    Dialog(onDismissRequest = { setShowDialog(false) }) {
        val checkboxStates = remember {
            tags.map { false }.toMutableStateList()
        }

        Column {
            tags.forEachIndexed { index, value ->
                Checkbox(checked = checkboxStates[index],
                    onCheckedChange = { isChecked ->
                        checkboxStates[index] = isChecked
                    })
            }

        }

        Button({
            updateTagFilterList(checkboxStates.mapIndexed { index, value ->
                if (value) {
                    tags[index]
                } else {
                    null
                }
            }.filterNotNull())

            setShowDialog(false)
        }) {
            Text("Close")
        }
    }
}
