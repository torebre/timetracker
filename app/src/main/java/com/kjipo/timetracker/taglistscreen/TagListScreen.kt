package com.kjipo.timetracker.taglistscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kjipo.timetracker.tagscreen.TaskMarkUiElement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class TagScreenInputParameter(
    val tagListUiState: StateFlow<TagListUiState>,
    val insertTag: (String) -> Unit,
    val updateTag: (Long, String) -> Unit,
    val goToTag: (tagId: Long) -> Unit
)

class TagScreenInputParameterProvider : PreviewParameterProvider<TagScreenInputParameter> {
    override val values =
        sequenceOf(
            TagScreenInputParameter(
                MutableStateFlow(
                    TagListUiState(
                        listOf(
                            TaskMarkUiElement(1, "Tag1", Color.Red),
                            TaskMarkUiElement(2, "Tag2", Color.Yellow),
                            TaskMarkUiElement(3, "Tag3", Color.Green)
                        )
                    )
                ),
                { _ ->
                    // Do nothing
                },
                { _, _ ->
                    // Do nothing
                },
                { _ ->
                    // Do nothing

                })
        )

}

@Composable
fun TagListScreen(tagModel: TagModel, goToTagScreen: (Long) -> Unit) {
    TagListScreen(
        tagScreenInput = TagScreenInputParameter(
            tagModel.uiState,
            { title -> tagModel.insertTag(title) },
            { id, title -> tagModel.updateTag(id, title) },
            goToTagScreen)
    )
}

@Preview
@Composable
fun TagListScreen(@PreviewParameter(TagScreenInputParameterProvider::class) tagScreenInput: TagScreenInputParameter) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(items = tagScreenInput.tagListUiState.value.tags,
            key = { tag ->
                tag.elementId
            }) {
            TagRow(it, { elementId -> tagScreenInput.goToTag(elementId) })
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