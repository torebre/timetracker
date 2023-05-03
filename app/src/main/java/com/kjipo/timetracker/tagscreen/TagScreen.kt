package com.kjipo.timetracker.tagscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider


class TaskMarkScreenInput(
    val tagUi: TaskMarkElementUiState,
    val save: (tagUi: TaskMarkUiElement) -> Unit,
    val deleteElement: () -> Unit,
    val navigateToElementList: () -> Unit
)

class TaskMarkElementParameterProvider : PreviewParameterProvider<TaskMarkScreenInput> {
    override val values = sequenceOf(
        TaskMarkScreenInput(TaskMarkElementUiState(TaskMarkUiElement(1, "Test tag", Color.Green), false),
            { tagUi ->
                // Do nothing
            }, {
                // Do nothing
            }, {
                // Do nothing
            })
    )

}

@Composable
fun TaskMarkElementScreen(tagScreenModel: TagScreenModel, navigateToTagList: () -> Unit) {
    val uiState = tagScreenModel.uiState.collectAsState()

    if(uiState.value.loading) {
        return
    }

    TaskMarkElementScreen(TaskMarkScreenInput(uiState.value, {
        tagScreenModel.updateTag(it)
    }, {
        tagScreenModel.deleteTag()
    }, navigateToTagList))


}

@Preview(showBackground = true)
@Composable
fun TaskMarkElementScreen(@PreviewParameter(TaskMarkElementParameterProvider::class) tagScreenInput: TaskMarkScreenInput) {
    val title = remember {
        mutableStateOf(tagScreenInput.tagUi.tag.title)
    }

    Column {
        TextField(value = title.value, onValueChange = {
            title.value = it
        })

        Row {
            Button(
                onClick = {
                    tagScreenInput.save(tagScreenInput.tagUi.tag.copy(title = title.value))
                    tagScreenInput.navigateToElementList()
                },
                enabled = title.value != tagScreenInput.tagUi.tag.title
            ) {
                Text("Save")
            }

            val isNewTag = tagScreenInput.tagUi.tag.elementId == 0L
            Button(onClick = {
                // Nothing to delete if the tag is new
                if(!isNewTag) {
                    tagScreenInput.deleteElement()
                }
                tagScreenInput.navigateToElementList()
            }) {
                // If the ID is 0 then this a new tag
                if(isNewTag) {
                   Text("Cancel")
                }
                else {
                    Text("Delete")
                }
            }

        }
    }


}
