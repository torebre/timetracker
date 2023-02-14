package com.kjipo.timetracker

import androidx.compose.runtime.Composable
import com.kjipo.timetracker.ui.theme.TimeTrackerTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController


@Composable
fun TimeTrackerApp(appContainer: AppContainer) {
    TimeTrackerTheme {
        val appState = rememberTimeTrackerAppState()
        TimeTrackerScaffold(appState, appContainer)
    }


}


@Composable
fun rememberTimeTrackerAppState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    navController: NavHostController = rememberNavController(),
    taskScreenShowing: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }
) = remember(scaffoldState) {
    TimeTrackerAppState(
        scaffoldState,
        navController,
        taskScreenShowing
    )
}


class TimeTrackerAppState(
    val scaffoldState: ScaffoldState,
    val navController: NavHostController,
    val taskScreenShowing: MutableState<Boolean>
) {

    val currentRoute: String?
        get() = navController.currentDestination?.route


    fun navigateToScreen(route: String) {
        if (route != currentRoute) {

            // TODO Is this a good way to handle the route to the task screen?
            navController.navigate(route) {
                taskScreenShowing.value = route.startsWith(Screens.TASK.name)

                launchSingleTop = true
                restoreState = !route.startsWith(Screens.TASK.name)
                // Pop up backstack to the first destination and save state. This makes going back
                // to the start destination when pressing back in any other bottom tab.
                popUpTo(findStartDestination(navController.graph).id) {
                    saveState = !route.startsWith(Screens.TASK.name)
                }
            }
        }
    }


    private val NavGraph.startDestination: NavDestination?
        get() = findNode(startDestinationId)


    private tailrec fun findStartDestination(graph: NavDestination): NavDestination {
        return if (graph is NavGraph) findStartDestination(graph.startDestination!!) else graph
    }

}