package com.kjipo.timetracker.sprints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.timetracker.database.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SprintUiState(
    val sprints: List<Sprint> = emptyList(),
    val dayTypes: List<DayType> = emptyList(),
    val currentSprint: Sprint? = null,
    val sprintDays: List<SprintDay> = emptyList(),
    val customDays: List<CustomDay> = emptyList(),
    val isLoading: Boolean = false,
    val draftSprint: Sprint? = null,
    val draftSprintDays: Map<LocalDate, Long> = emptyMap(),
    val draftCustomDays: Map<LocalDate, Double> = emptyMap()
)

class SprintsViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SprintUiState())
    val uiState: StateFlow<SprintUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SprintEvent>()
    val events: SharedFlow<SprintEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val dayTypes = repository.sprintDao.getDayTypes()
            _uiState.update { it.copy(dayTypes = dayTypes) }

            repository.sprintDao.getAllSprints().collect { sprints ->
                _uiState.update { it.copy(sprints = sprints, isLoading = false) }
            }
        }
    }

    fun refreshDayTypes() {
        viewModelScope.launch {
            val dayTypes = repository.sprintDao.getDayTypes()
            _uiState.update { it.copy(dayTypes = dayTypes) }
        }
    }

    fun startCreatingSprint() {
        refreshDayTypes()
        _uiState.update { 
            it.copy(
                draftSprint = Sprint(title = "", startDate = LocalDate.now(), endDate = LocalDate.now().plusWeeks(2)),
                draftSprintDays = emptyMap(),
                draftCustomDays = emptyMap()
            )
        }
    }

    fun startEditingSprint(sprint: Sprint) {
        viewModelScope.launch {
            val dayTypes = repository.sprintDao.getDayTypes()
            val sprintDays = repository.sprintDao.getSprintDays(sprint.sprintId)
            val customDays = repository.sprintDao.getCustomDays(sprint.sprintId)
            _uiState.update { 
                it.copy(
                    dayTypes = dayTypes,
                    draftSprint = sprint,
                    draftSprintDays = sprintDays.associate { sd -> sd.date to sd.dayTypeId },
                    draftCustomDays = customDays.associate { cd -> cd.date to cd.workingHours }
                )
            }
        }
    }

    fun updateDraftSprint(title: String, startDate: LocalDate, endDate: LocalDate) {
        _uiState.update { 
            it.copy(draftSprint = it.draftSprint?.copy(title = title, startDate = startDate, endDate = endDate))
        }
    }

    fun setDraftSprintDay(date: LocalDate, dayTypeId: Long?) {
        _uiState.update { state ->
            val newSprintDays = state.draftSprintDays.toMutableMap()
            val newCustomDays = state.draftCustomDays.toMutableMap()
            if (dayTypeId == null) {
                newSprintDays.remove(date)
            } else {
                newSprintDays[date] = dayTypeId
                newCustomDays.remove(date)
            }
            state.copy(draftSprintDays = newSprintDays, draftCustomDays = newCustomDays)
        }
    }

    fun setDraftCustomDay(date: LocalDate, workingHours: Double?) {
        _uiState.update { state ->
            val newSprintDays = state.draftSprintDays.toMutableMap()
            val newCustomDays = state.draftCustomDays.toMutableMap()
            if (workingHours == null) {
                newCustomDays.remove(date)
            } else {
                newCustomDays[date] = workingHours
                newSprintDays.remove(date)
            }
            state.copy(draftSprintDays = newSprintDays, draftCustomDays = newCustomDays)
        }
    }

    fun saveDraftSprint() {
        val state = _uiState.value
        val sprint = state.draftSprint ?: return
        
        viewModelScope.launch {
            // Check if day types in draft still exist in the database
            val currentDayTypes = repository.sprintDao.getDayTypes()
            val currentDayTypeIds = currentDayTypes.map { it.dayTypeId }.toSet()
            
            val validDraftSprintDays = state.draftSprintDays.filter { (_, dayTypeId) ->
                currentDayTypeIds.contains(dayTypeId)
            }

            val sprintId = if (sprint.sprintId == 0L) {
                repository.sprintDao.insertSprint(sprint)
            } else {
                repository.sprintDao.updateSprint(sprint)
                sprint.sprintId
            }

            val sprintDays = validDraftSprintDays.map { (date, dayTypeId) ->
                SprintDay(sprintId = sprintId, dayTypeId = dayTypeId, date = date)
            }
            val customDays = state.draftCustomDays.map { (date, workingHours) ->
                CustomDay(sprintId = sprintId, date = date, workingHours = workingHours)
            }

            repository.sprintDao.clearAndInsertSprintDays(sprintId, sprintDays)
            repository.sprintDao.clearAndInsertCustomDays(sprintId, customDays)
            
            _events.emit(SprintEvent.SaveSuccess)
            _uiState.update { it.copy(draftSprint = null) }
        }
    }

    fun cancelDraft() {
        _uiState.update { it.copy(draftSprint = null) }
    }

    fun createSprint(title: String, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            repository.sprintDao.insertSprint(Sprint(title = title, startDate = startDate, endDate = endDate))
        }
    }

    fun updateSprint(sprint: Sprint) {
        viewModelScope.launch {
            repository.sprintDao.updateSprint(sprint)
        }
    }

    fun deleteSprint(sprint: Sprint) {
        viewModelScope.launch {
            repository.sprintDao.deleteSprint(sprint)
            if (_uiState.value.currentSprint?.sprintId == sprint.sprintId) {
                _uiState.update { it.copy(currentSprint = null, sprintDays = emptyList(), customDays = emptyList()) }
            }
        }
    }

    fun selectSprint(sprint: Sprint?) {
        viewModelScope.launch {
            if (sprint == null) {
                _uiState.update { it.copy(currentSprint = null, sprintDays = emptyList(), customDays = emptyList()) }
            } else {
                val sprintDays = repository.sprintDao.getSprintDays(sprint.sprintId)
                val customDays = repository.sprintDao.getCustomDays(sprint.sprintId)
                _uiState.update { it.copy(currentSprint = sprint, sprintDays = sprintDays, customDays = customDays) }
            }
        }
    }

    fun setSprintDay(sprintId: Long, date: LocalDate, dayTypeId: Long?) {
        viewModelScope.launch {
            if (dayTypeId == null) {
                repository.sprintDao.deleteSprintDay(sprintId, date)
            } else {
                repository.sprintDao.insertSprintDay(SprintDay(sprintId = sprintId, dayTypeId = dayTypeId, date = date))
            }
            refreshSprintDetails(sprintId)
        }
    }

    fun setCustomDay(sprintId: Long, date: LocalDate, workingHours: Double?) {
        viewModelScope.launch {
            if (workingHours == null) {
                repository.sprintDao.deleteCustomDay(sprintId, date)
            } else {
                repository.sprintDao.insertCustomDay(CustomDay(sprintId = sprintId, date = date, workingHours = workingHours))
            }
            refreshSprintDetails(sprintId)
        }
    }

    private suspend fun refreshSprintDetails(sprintId: Long) {
        val sprintDays = repository.sprintDao.getSprintDays(sprintId)
        val customDays = repository.sprintDao.getCustomDays(sprintId)
        _uiState.update { it.copy(sprintDays = sprintDays, customDays = customDays) }
    }

    sealed class SprintEvent {
        object SaveSuccess : SprintEvent()
    }

    companion object {
        fun provideFactory(repository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SprintsViewModel(repository) as T
                }
            }
    }
}
