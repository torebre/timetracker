package com.kjipo.timetracker

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kjipo.timetracker.tasklist.TaskList
import com.kjipo.timetracker.tasklist.TaskListModel
import com.kjipo.timetracker.taskscreen.TaskScreenModel
import timber.log.Timber


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTrackerScaffold(appState: TimeTrackerAppState,
appContainer: AppContainer) {

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
               val taskListModel: TaskListModel = viewModel(factory = TaskListModel.provideFactory(
                   appContainer.taskRepository
               ))

                TaskList(taskListModel) { taskId ->

                    Timber.tag("Navigation").i("Called to go to task with ID: $taskId")

                    appState.navigateToScreen("${Screens.TASK.name}/$taskId")
                }

            }

            composable(Screens.REPORTS.name) {

                // TODO
                Text("Reports")

            }

            composable("${Screens.TASK.name}/{taskId}",
            arguments = listOf(navArgument("taskId") {
                type = NavType.LongType
            })) { navBackStackEntry ->

                Timber.tag("Navigation").i("Navigation to task screen: ${navBackStackEntry.arguments?.getLong("taskId")}")

                navBackStackEntry.arguments?.getLong("taskId")?.let { taskId ->
                    val taskScreenModel = TaskScreenModel(taskId, appContainer.taskRepository)

                    Timber.tag("Navigation").i("Navigating to task: $taskId")

                    TaskScreen(taskScreenModel, {

                        // TODO Implement save functionality

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
