package com.kjipo.timetracker.tagscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kjipo.timetracker.taglistscreen.TagScreenInputParameterProvider
import com.kjipo.timetracker.taskscreen.TagUi


class TagScreenInput(
    val tagUi: TagScreenUiState,
    val save: (tagUi: TagUi) -> Unit,
    val deleteTag: () -> Unit
)

class TagScreenParameterProvider : PreviewParameterProvider<TagScreenInput> {
    override val values = sequenceOf(
        TagScreenInput(TagScreenUiState(TagUi(1, "Test tag", Color.Green), false),
            { tagUi ->
                // Do nothing
            }, {
                // Do nothing
            })
    )

}

@Composable
fun TagScreen(tagScreenModel: TagScreenModel, goToTagList: () -> Unit) {
    val uiState = tagScreenModel.uiState.collectAsState()

    if(uiState.value.loading) {
        return
    }

    TagScreen(TagScreenInput(uiState.value, {
        tagScreenModel.updateTag(it)
    }, {
        tagScreenModel.deleteTag()
        goToTagList()
    }))


}

@Preview(showBackground = true)
@Composable
fun TagScreen(@PreviewParameter(TagScreenParameterProvider::class) tagScreenInput: TagScreenInput) {
    val title = remember {
        mutableStateOf(tagScreenInput.tagUi.tag.title)
    }

    Column {
        Text(text = title.value)

        Row {
            Button(
                onClick = {
                    tagScreenInput.save(tagScreenInput.tagUi.tag.copy(title = title.value))
                },
                enabled = title.value != tagScreenInput.tagUi.tag.title
            ) {
                Text("Save")
            }

            Button(onClick = {
                tagScreenInput.deleteTag()
            }) {
                Text("Delete")
            }

        }
    }


}
