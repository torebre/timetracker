package com.kjipo.timetracker.sprints

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kjipo.timetracker.database.DayType
import com.kjipo.timetracker.database.Sprint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SprintsScreen(
    viewModel: SprintsViewModel,
    onAddSprint: () -> Unit,
    onEditSprint: (Sprint) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onAddSprint()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Sprint")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            SprintList(
                sprints = uiState.sprints,
                onSprintClick = {
                    onEditSprint(it)
                },
                onEditClick = {
                    onEditSprint(it)
                },
                onDeleteClick = { viewModel.deleteSprint(it) }
            )
        }
    }
}

@Composable
fun SprintList(
    sprints: List<Sprint>,
    onSprintClick: (Sprint) -> Unit,
    onEditClick: (Sprint) -> Unit,
    onDeleteClick: (Sprint) -> Unit
) {
    LazyColumn {
        items(sprints) { sprint ->
            ListItem(
                modifier = Modifier.clickable { onSprintClick(sprint) },
                headlineContent = { Text(sprint.title) },
                supportingContent = { Text("${sprint.startDate} - ${sprint.endDate}") },
                trailingContent = {
                    Row {
                        IconButton(onClick = { onEditClick(sprint) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { onDeleteClick(sprint) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    }
}

