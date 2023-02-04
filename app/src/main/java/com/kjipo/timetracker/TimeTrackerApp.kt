package com.kjipo.timetracker

import androidx.compose.runtime.Composable
import com.kjipo.timetracker.ui.theme.TimeTrackerTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
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
    navController: NavHostController = rememberNavController()
) = remember(scaffoldState) {
    TimeTrackerAppState(scaffoldState,
    navController)
}


class TimeTrackerAppState(
    val scaffoldState: ScaffoldState,
    val navController: NavHostController
) {

    val currentRoute: String?
        get() = navController.currentDestination?.route


    fun navigateToScreen(route: String) {
        if (route != currentRoute) {
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
                // Pop up backstack to the first destination and save state. This makes going back
                // to the start destination when pressing back in any other bottom tab.
                popUpTo(findStartDestination(navController.graph).id) {
                    saveState = true
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