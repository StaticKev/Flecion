package com.statickev.flecion.presentation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statickev.flecion.data.model.Task
import com.statickev.flecion.data.model.TaskStatus
import com.statickev.flecion.data.repository.TaskRepository
import com.statickev.flecion.presentation.adapter.TaskAdapter.Companion.TASK_ID
import com.statickev.flecion.presentation.presentationUtil.doAtIsValid
import com.statickev.flecion.presentation.presentationUtil.dueIsValid
import com.statickev.flecion.presentation.presentationUtil.remindAtIsValid
import com.statickev.flecion.util.getDateTimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
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
class TaskDetailViewModel @Inject constructor(
    private val repo: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val taskId = savedStateHandle.get<Int>(TASK_ID)!!

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

    private val _descriptionFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val _remindAtError = MutableLiveData<String?>(null)
    val remindAtError: LiveData<String?> = _remindAtError
    private val _dueError = MutableLiveData<String?>(null)
    val dueError: LiveData<String?> = _dueError
    private val _doAtError = MutableLiveData<String?>(null)
    val doAtError: LiveData<String?> = _doAtError
    var isFormValid = true
        private set

    init {
        viewModelScope.launch {
            _descriptionFlow
                .debounce(500)
                .collect { desc ->
                    val current = task.value ?: return@collect
                    repo.updateTask(current.copy(description = desc))
                }
        }
    }

    fun onStatusChanged(value: TaskStatus) {
        viewModelScope.launch {
            task.value?.let {
                repo.updateTask(
                    it.copy(
                        status = value
                    )
                )
            }
        }
    }

    fun onPriorityLevelChanged(value: Float) {
        viewModelScope.launch {
            task.value?.let {
                repo.updateTask(
                    it.copy(
                        completionRate = value.toInt()
                    )
                )
            }
        }
    }

    fun onRemindAtChanged(value: String?) {
        viewModelScope.launch {
            task.value?.let { it ->
                val updatedTask = it.copy(
                    remindAt = value?.takeIf { it.isNotEmpty() }?.let {
                        LocalDateTime.parse(it, getDateTimeFormatter())
                    }
                )

                repo.updateTask(updatedTask)

//                _remindAtError.value = updatedTask.remindAt?.let { updatedTask ->
//                    remindAtIsValid(updatedTask, it.due, it.doAt)
//                }

                validate(updatedTask)
            }
        }
    }

    fun onDueChanged(value: String?) {
        viewModelScope.launch {
            task.value?.let { it ->
                val updatedTask = it.copy(
                    due = value?.takeIf { it.isNotEmpty() }?.let {
                        LocalDateTime.parse(it, getDateTimeFormatter())
                    }
                )

                repo.updateTask(updatedTask)

                validate(updatedTask)
            }
        }
    }

    fun onDoAtChanged(value: String?) {
        viewModelScope.launch {
            task.value?.let { it ->
                val updatedTask = it.copy(
                    doAt = value?.takeIf { it.isNotEmpty() }?.let {
                        LocalDateTime.parse(it, getDateTimeFormatter())
                    }
                )

                repo.updateTask(updatedTask)

                validate(updatedTask)
            }
        }
    }

    fun onDescriptionChanged(value: String) {
        _descriptionFlow.tryEmit(value)
    }

    fun validate(updatedTask: Task) {
        _remindAtError.value = updatedTask.remindAt?.let { ut ->
            remindAtIsValid(ut, updatedTask.due, updatedTask.doAt)
        }
        _dueError.value = updatedTask.due?.let { ut ->
            dueIsValid(ut)
        }
        _doAtError.value = updatedTask.doAt?.let { ut ->
            doAtIsValid(updatedTask.due, ut )
        }

        isFormValid = _remindAtError.value == null
                && _dueError.value == null
                && _doAtError.value == null
    }

    fun revertChanges(partial: Boolean) {
        viewModelScope.launch {
            var updatedTask: Task?

            task.value?.let {
                if (partial) {
                    val rcRemindAt = when {
                        remindAtError.value == null -> task.value?.remindAt
                        else -> initialTask?.remindAt
                    }
                    val rcDue = when {
                        dueError.value == null -> task.value?.due
                        else -> initialTask?.due
                    }
                    val rcDoAt = when {
                        _doAtError.value == null -> it.due
                        else -> initialTask?.doAt
                    }

                    updatedTask = it.copy(
                        remindAt = rcRemindAt,
                        due = rcDue,
                        doAt = rcDoAt
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