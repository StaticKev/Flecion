package com.statickev.flecion.presentation.viewModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statickev.flecion.data.model.Task
import com.statickev.flecion.data.repository.TaskRepository
import com.statickev.flecion.platform.scheduler.cancelTaskReminder
import com.statickev.flecion.platform.scheduler.scheduleTaskReminder
import com.statickev.flecion.util.toEpochMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repo: TaskRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    val pendingTaskState = repo.getPendingTasks()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val onHoldTaskState = repo.getOnHoldTasks()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val ongoingTaskState = repo.getOngoingTasks()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    // TODO: Delete on production.
    fun addTasks(task: List<Task>) {
        viewModelScope.launch {
            repo.insertTasks(task)
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repo.insertTask(task)
            @SuppressLint("ScheduleExactAlarm")
            task.remindAt?.let { remindAt ->
                scheduleTaskReminder(
                    context = appContext,
                    taskId = task.id,
                    taskTitle = task.title,
                    triggerAtMillis = remindAt.toEpochMillis()
                )
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repo.deleteTask(task)
            cancelTaskReminder(appContext, task.id)
            _uiEvent.emit(UiEvent.ShowSnackbarOnDelete("Task deleted", task))
        }
    }

    sealed class UiEvent {
        data class ShowSnackbarOnDelete(val message: String, val deletedTask: Task) : UiEvent()
    }
}