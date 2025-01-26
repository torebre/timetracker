package com.kjipo.timetracker

import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import com.kjipo.timetracker.ui.theme.TimeTrackerTheme
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
    screenshowing: MutableState<Screens?> = remember {
        // The tasks screen is the default screen
        mutableStateOf(Screens.TASKS)
    }
) = remember(scaffoldState) {
    TimeTrackerAppState(
        scaffoldState,
        navController,
        screenshowing
    )
}


class TimeTrackerAppState(
    val scaffoldState: ScaffoldState,
    val navController: NavHostController,
    val screenShowing: MutableState<Screens?>
) {

    private val currentRoute: String?
        get() = navController.currentDestination?.route


    fun navigateToScreen(route: String) {
        if (route != currentRoute) {
            navController.navigate(route) {
                screenShowing.value = Screens.entries.first {
                    route.startsWith(it.name)
                }

                launchSingleTop = true
                // Some screens should load the state again every time the user navigates to them
                // in case the state they are showing have been affected by changes done in some
                // other screen
                val save = shouldStateBeSaved(route)
                restoreState = save
                // Pop up backstack to the first destination and save state. This makes going back
                // to the start destination when pressing back in any other bottom tab.
                popUpTo(findStartDestination(navController.graph).id) {
                    saveState = save
                }
            }
        }
    }


    private val NavGraph.startDestination: NavDestination?
        get() = findNode(startDestinationId)


    private tailrec fun findStartDestination(graph: NavDestination): NavDestination {
        return if (graph is NavGraph) findStartDestination(graph.startDestination!!) else graph
    }

    private fun shouldStateBeSaved(route: String): Boolean {
        return !route.startsWith(Screens.TAG.name)
                && !route.startsWith(Screens.TASKS.name)
                && !route.startsWith(Screens.PROJECTS.name)
    }

}