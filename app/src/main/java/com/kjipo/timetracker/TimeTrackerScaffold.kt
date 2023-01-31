package com.kjipo.timetracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTrackerScaffold(appState: TimeTrackerAppState) {

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

                // TODO
                Text("Tasks")
            }

            composable(Screens.REPORTS.name) {

                // TODO
                Text("Reports")

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
