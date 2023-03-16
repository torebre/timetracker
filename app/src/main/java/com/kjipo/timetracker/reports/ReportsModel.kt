package com.kjipo.timetracker.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime


class ReportsModel(private val taskRepository: TaskRepository): ViewModel() {

    private val viewModelState = MutableStateFlow(ReportsUiState())

    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value
    )


    companion object {

        fun provideFactory(taskRepository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReportsModel(taskRepository) as T
                }
            }

    }


    data class ReportsUiState(val startTime: LocalDateTime? = null,
                              val stopTime: LocalDateTime? = null)


}