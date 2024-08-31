package com.kjipo.timetracker.tagscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember


@Composable
fun TaskMarkElementScreen(tagScreenModel: TagScreenModel, navigateToElementList: () -> Unit) {
    val uiState = tagScreenModel.uiState.collectAsState()

    if (uiState.value.loading) {
        return
    }

    TaskMarkElementScreen(uiState.value, save = {
        tagScreenModel.updateTag(it)
    }, deleteElement = {
        tagScreenModel.deleteTag()
    }, navigateToElementList)
}

@Composable
fun TaskMarkElementScreen(
    tagUi: TaskMarkElementUiState,
    save: (tagUi: TaskMarkUiElement) -> Unit,
    deleteElement: () -> Unit,
    navigateToElementList: () -> Unit
) {
    val title = remember {
        mutableStateOf(tagUi.tag.title)
    }

    Column {
        TextField(value = title.value, onValueChange = {
            title.value = it
        })

        Row {
            Button(
                onClick = {
                    save(tagUi.tag.copy(title = title.value))
                    navigateToElementList()
                },
                enabled = title.value != tagUi.tag.title
            ) {
                Text("Save")
            }

            val isNewTag = tagUi.tag.elementId == 0L
            Button(onClick = {
                // Nothing to delete if the tag is new
                if (!isNewTag) {
                    deleteElement()
                }
                navigateToElementList()
            }) {
                // If the ID is 0 then this a new tag
                if (isNewTag) {
                    Text("Cancel")
                } else {
                    Text("Delete")
                }
            }
        }
    }

}
