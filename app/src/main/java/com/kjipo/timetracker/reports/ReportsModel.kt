package com.kjipo.timetracker.reports

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.time.*


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


    data class PieChartEntry(val tagId: Long, val percentage: Int, val colour: Color)
    data class PieChartData(val pieChartEntries: List<PieChartEntry>)

    data class ReportsUiState(val startTime: LocalDateTime? = null,
                              val stopTime: LocalDateTime? = null,
    val pieChartData: PieChartData? = null)


}

data class CalendarUiState(val year: Year = Year.now(),
                           val month: Month = LocalDate.now().month,
                           val startTime: LocalDateTime? = null,
                           val stopTime: LocalDateTime? = null)