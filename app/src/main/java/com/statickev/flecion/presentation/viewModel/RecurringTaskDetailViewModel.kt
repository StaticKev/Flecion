package com.statickev.flecion.presentation.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statickev.flecion.data.model.Task
import com.statickev.flecion.data.repository.TaskRepository
import com.statickev.flecion.platform.scheduler.cancelTaskReminder
import com.statickev.flecion.platform.scheduler.scheduleTaskReminder
import com.statickev.flecion.presentation.adapter.TaskAdapter.Companion.TASK_ID
import com.statickev.flecion.util.getDateTimeFormatter
import com.statickev.flecion.util.toEpochMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class RecurringTaskDetailViewModel @Inject constructor(
    private val repo: TaskRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    private val taskId = savedStateHandle.get<Long>(TASK_ID)!!

    val task = repo.getTaskById(taskId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    var initialTask: Task? = null
        set(value) {
            if (initialTask == null) field = value
        }

    private val _recurIntervalError = MutableLiveData<String?>(null)
    val recurIntervalError: LiveData<String?> = _recurIntervalError
    private val _remindAtError = MutableLiveData<String?>(null)
    val remindAtError: LiveData<String?> = _remindAtError
    private val _recurIntervalFlow = MutableSharedFlow<Task>(extraBufferCapacity = 1)
    private val _descriptionFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    var isFormValid = true
        private set

    init {
        viewModelScope.launch {
            _recurIntervalFlow
                .debounce(500)
                .collect { updatedRecurIntervalTask ->
                    val current = task.value ?: return@collect
                    repo.updateTask(updatedRecurIntervalTask)
                }
        }
        viewModelScope.launch {
            _descriptionFlow
                .debounce(500)
                .collect { desc ->
                    val current = task.value ?: return@collect
                    repo.updateTask(current.copy(description = desc))
                }
        }
    }

    fun onRecurIntervalChanged(value: String) {
        task.value?.let {
            val updatedTask = it.copy(recurInterval = value.toIntOrNull())

            _recurIntervalFlow.tryEmit(updatedTask)
            validate(updatedTask)
        }
    }

    // TODO: If RemindAt is empty, cancel notification.
    fun onRemindAtChanged(value: String?) {
        viewModelScope.launch {
            task.value?.let { it ->
                val updatedTask = it.copy(
                    remindAt = value?.takeIf { it.isNotEmpty() }?.let {
                        LocalDateTime.parse(it, getDateTimeFormatter())
                    }
                )

                try {
                    if (updatedTask.remindAt != null) scheduleTaskReminder(
                        context = appContext,
                        taskId = updatedTask.id,
                        taskTitle = updatedTask.title,
                        triggerAtMillis = updatedTask.remindAt!!.toEpochMillis(),
                        isRecurring = updatedTask.recurInterval != null
                    )
                    else cancelTaskReminder(appContext, updatedTask.id)
                } catch (e: SecurityException) {}

                repo.updateTask(updatedTask)

                validate(updatedTask)
            }
        }
    }

    fun onSendNotificationChanged(value: Boolean) {
        viewModelScope.launch {
            task.value?.let { it ->
                val updatedTask = it.copy(
                    sendNotification = value
                )

                repo.updateTask(updatedTask)

                try {
                    if (updatedTask.sendNotification) scheduleTaskReminder(
                        context = appContext,
                        taskId = updatedTask.id,
                        taskTitle = updatedTask.title,
                        triggerAtMillis = updatedTask.remindAt!!.toEpochMillis(),
                        isRecurring = updatedTask.recurInterval != null
                    )
                    else cancelTaskReminder(appContext, updatedTask.id)
                } catch (e: SecurityException) {}
            }
        }
    }

    fun onDescriptionChanged(value: String) {
        _descriptionFlow.tryEmit(value)
    }

    fun validate(updatedTask: Task) {
        if (updatedTask.recurInterval == null) _recurIntervalError.value = "Required"
        else _recurIntervalError.value = null

        val remindAt = updatedTask.remindAt

        _remindAtError.value = when {
            remindAt == null -> "Required!"

            remindAt.isBefore(LocalDateTime.now()) -> "Value must be later than now!"

            else -> null
        }

        isFormValid = _recurIntervalError.value == null &&
                _remindAtError.value == null
    }

    fun revertChanges(partial: Boolean) {
        viewModelScope.launch {
            var updatedTask: Task?

            task.value?.let { it ->
                if (partial) {
                    updatedTask = it.copy(
                        remindAt = when {
                            remindAtError.value == null -> it.remindAt
                            else -> initialTask?.remindAt
                        },
                        recurInterval = when {
                            recurIntervalError.value == null -> it.recurInterval
                            else -> initialTask?.recurInterval
                        }
                    )

                    repo.updateTask(updatedTask)
                    validate(updatedTask)
                }
                else {
                    initialTask?.let {
                        updatedTask = initialTask!!.copy()
                        repo.updateTask(updatedTask)
                        validate(updatedTask)
                    }
                }
            }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            task.value?.let {
                repo.deleteTask(it)
            }
        }
    }
}