package com.kjipo.timetracker.taskmarkelementlistscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement
import kotlinx.coroutines.flow.StateFlow


@Composable
fun TagListScreen(tagModel: TaskMarkerModel, goToTagScreen: (Long) -> Unit) {
    TagListScreen(
        tagModel.uiState,
        goToTagScreen
    )
}

@Composable
fun TagListScreen(
    tagListUiState: StateFlow<TagListUiState>,
    goToTag: (tagId: Long) -> Unit
) {


    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(items = tagListUiState.value.tags,
            key = { tag ->
                tag.elementId
            }) {
            TagRow(it, goToTag = { elementId -> goToTag(elementId) })
        }
    }

}

@Composable
fun TagRow(tagUi: TaskMarkUiElement, goToTag: (Long) -> Unit) {
    Row(modifier = Modifier.clickable { goToTag(tagUi.elementId) }) {
        Text(
            modifier = Modifier.background(tagUi.colour ?: MaterialTheme.colorScheme.background),
            style = MaterialTheme.typography.displayMedium,
            text = tagUi.title
        )
    }
}