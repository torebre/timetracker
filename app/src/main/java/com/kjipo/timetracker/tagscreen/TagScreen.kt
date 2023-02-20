package com.kjipo.timetracker.tagscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.kjipo.timetracker.database.Tag
import com.kjipo.timetracker.taskscreen.TagUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class TagScreenInputParameter(
    val tagListUiState: StateFlow<TagListUiState>,
    val insertTag: (String) -> Unit,
    val updateTag: (Long, String) -> Unit
)


class TagScreenInputParameterProvider : PreviewParameterProvider<TagScreenInputParameter> {
    override val values =
        sequenceOf(
            TagScreenInputParameter(
                MutableStateFlow(
                    TagListUiState(
                        listOf(
                            TagUi(1, "Tag1", Color.Red),
                            TagUi(2, "Tag2", Color.Yellow),
                            TagUi(3, "Tag3", Color.Green)
                        )
                    )
                ),
                { _ ->
                    // Do nothing
                },
                { _, _ ->
                    // Do nothing
                })
        )

}

@Composable
fun TagScreen(tagModel: TagModel) {
    TagScreen(
        tagScreenInput = TagScreenInputParameter(
            tagModel.uiState,
            { title -> tagModel.insertTag(title) },
            { id, title -> tagModel.updateTag(id, title) })
    )

}

@Preview
@Composable
fun TagScreen(@PreviewParameter(TagScreenInputParameterProvider::class) tagScreenInput: TagScreenInputParameter) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(items = tagScreenInput.tagListUiState.value.tags,
            key = { tag ->
                tag.tagId
            }) {
            TagRow(it)
        }
    }

}

@Composable
fun TagRow(tagUi: TagUi) {
    Text(
        modifier = Modifier.background(tagUi.colour ?: MaterialTheme.colorScheme.background),
        text = tagUi.title
    )
}