package com.statickev.flecion.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statickev.flecion.data.repository.TaskRepository
import com.statickev.flecion.data.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor (
    private val taskRepo: TaskRepository
) : ViewModel() {

    private val _tasksState = MutableStateFlow<List<Task>>(emptyList())
    private val _pendingTaskState = MutableStateFlow<List<Task>>(emptyList())
    private val _onHoldTaskState = MutableStateFlow<List<Task>>(emptyList())
    private val _ongoingTaskState = MutableStateFlow<List<Task>>(emptyList())
    val tasksState: StateFlow<List<Task>> = _tasksState.asStateFlow()
    val pendingTaskState: StateFlow<List<Task>> = _pendingTaskState.asStateFlow()
    val onHoldTaskState: StateFlow<List<Task>> = _onHoldTaskState.asStateFlow()
    val ongoingTaskState: StateFlow<List<Task>> = _ongoingTaskState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            taskRepo.getAllTasks().collect { _tasksState.value = it }
        }
        viewModelScope.launch {
            taskRepo.getPendingTasks().collect { _pendingTaskState.value = it }
        }
        viewModelScope.launch {
            taskRepo.getOnHoldTasks().collect { _onHoldTaskState.value = it }
        }
        viewModelScope.launch {
            taskRepo.getOngoingTasks().collect { _ongoingTaskState.value = it }
        }
    }

    // TODO: Delete on production.
    fun addTasks(task: List<Task>) {
        viewModelScope.launch {
            taskRepo.insertTasks(task)
        }
    }

    // TODO: Add delete feature later.
//    fun deleteTask(task: Task) {
//        viewModelScope.launch {
//            taskRepo.deleteTask(task)
//            _uiEvent.emit(UiEvent.ShowSnackbar("Task deleted"))
//        }
//    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
    }
}