package com.kjipo.timetracker.tagscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kjipo.timetracker.database.TimeEntry
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ProjectScreen(tagScreenModel: TagScreenModel, navigateToElementList: () -> Unit) {
    val uiState = tagScreenModel.uiState.collectAsState()

    if (uiState.value.loading) {
        return
    }

    ProjectScreen(uiState.value, save = {
        tagScreenModel.updateTag(it)
    }, deleteElement = {
        tagScreenModel.deleteTag()
    }, navigateToElementList)
}

@Composable
fun ProjectScreen(
    tagUi: TaskMarkElementUiState,
    save: (tagUi: TaskMarkUiElement) -> Unit,
    deleteElement: () -> Unit,
    navigateToElementList: () -> Unit
) {
    val title = remember {
        mutableStateOf(tagUi.tag.title)
    }

    val totalDuration = tagUi.timeEntries.fold(Duration.ZERO) { acc, timeEntry ->
        acc.plus(timeEntry.getDurationMissingStopSetToNow())
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Project Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            Button(
                onClick = {
                    save(tagUi.tag.copy(title = title.value))
                    navigateToElementList()
                },
                enabled = title.value != tagUi.tag.title,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Save")
            }

            val isNewProject = tagUi.tag.elementId == 0L
            Button(onClick = {
                if (!isNewProject) {
                    deleteElement()
                }
                navigateToElementList()
            }) {
                if (isNewProject) {
                    Text("Cancel")
                } else {
                    Text("Delete")
                }
            }
        }

        if (tagUi.tag.elementId != 0L) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Total time spent: ${formatDuration(totalDuration)}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Time Entries:",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(tagUi.timeEntries.sortedByDescending { it.start }) { timeEntry ->
                    TimeEntryItem(timeEntry)
                }
            }
        }
    }
}

@Composable
fun TimeEntryItem(timeEntry: TimeEntry) {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "${formatter.format(timeEntry.start)} - ${
                timeEntry.stop?.let { formatter.format(it) } ?: "Ongoing"
            }",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Duration: ${formatDuration(timeEntry.getDurationMissingStopSetToNow())}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}
