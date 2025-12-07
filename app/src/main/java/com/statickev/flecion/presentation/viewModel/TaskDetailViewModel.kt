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
import com.statickev.flecion.presentation.presentationUtil.remindAtIsValid
import com.statickev.flecion.util.getDateTimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
                repo.updateTask(
                    it.copy(
                        remindAt = value?.takeIf { it.isNotEmpty() }?.let {
                            LocalDateTime.parse(it, getDateTimeFormatter())
                        }
                    )
                )

                _remindAtError.value = it.remindAt?.let { it2 ->
                    remindAtIsValid(it2, it.due, it.doAt)
                }

                validate()
            }
        }
    }


    fun onDueChanged(value: String?) {
        viewModelScope.launch {
            task.value?.let { it ->
                repo.updateTask(
                    it.copy(
                        due = value?.takeIf { it.isNotEmpty() }?.let {
                            LocalDateTime.parse(it, getDateTimeFormatter())
                        }
                    )
                )

                _remindAtError.value = it.remindAt?.let { it2 ->
                    remindAtIsValid(it2, it.due, it.doAt)
                }
                _doAtError.value = it.doAt?.let { it2 ->
                    doAtIsValid(it.due, it2 )
                }

                validate()
            }
        }
    }

    fun onDoAtChanged(value: String?) {
        viewModelScope.launch {
            task.value?.let { it ->
                repo.updateTask(
                    it.copy(
                        doAt = value?.takeIf { it.isNotEmpty() }?.let {
                            LocalDateTime.parse(it, getDateTimeFormatter())
                        }
                    )
                )

                _remindAtError.value = it.remindAt?.let { it2 ->
                    remindAtIsValid(it2, it.due, it.doAt)
                }
                _doAtError.value = it.doAt?.let { it2 ->
                    doAtIsValid(it.due, it2)
                }

                validate()
            }
        }
    }

    fun onDescriptionChanged(value: String) {
        _descriptionFlow.tryEmit(value)
    }

    fun validate() {
        isFormValid = _remindAtError.value == null && _doAtError.value == null
    }

    fun revertChanges(partial: Boolean) {
        viewModelScope.launch {
            task.value?.let {
                if (partial) {
                    repo.updateTask(
                        it.copy(
                            remindAt = initialTask?.remindAt,
                            due = initialTask?.due,
                            doAt = initialTask?.doAt
                        )
                    )
                }
                else {
                    repo.updateTask(initialTask?.copy() ?: it)
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