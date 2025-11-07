package com.statickev.lumina.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statickev.lumina.data.AppRepository
import com.statickev.lumina.data.model.Task
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
    private val taskRepo: AppRepository
) : ViewModel() {

    private val _tasksState = MutableStateFlow<List<Task>>(emptyList())
    val tasksState: StateFlow<List<Task>> = _tasksState.asStateFlow()
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            taskRepo.getAllTasks().collect { _tasksState.value = it }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepo.insertTask(task)
                _uiEvent.emit(UiEvent.ShowSnackbar("Task added successfully"))
            } catch (_: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to add task"))
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepo.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepo.deleteTask(task)
            _uiEvent.emit(UiEvent.ShowSnackbar("Task deleted"))
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
    }
}