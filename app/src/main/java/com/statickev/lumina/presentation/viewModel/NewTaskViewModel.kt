package com.statickev.lumina.presentation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statickev.lumina.data.model.Task
import com.statickev.lumina.data.model.TaskStatus
import com.statickev.lumina.data.repository.TaskRepository
import com.statickev.lumina.util.getDateTimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class NewTaskViewModel @Inject constructor (
    private val taskRepo: TaskRepository
) : ViewModel() {
    private val _title = MutableLiveData("New Task")
    private val _description = MutableLiveData("")
    private val _timeToCompleteHours = MutableLiveData(0)
    private val _timeToCompleteMins = MutableLiveData(0)
    private val _taskStatus = MutableLiveData(TaskStatus.PENDING)
    private val _priorityLevel = MutableLiveData<Byte>(1)
    private val _remindAt = MutableLiveData<LocalDateTime?>(null)
    private val _due = MutableLiveData<LocalDateTime?>(null)
    private val _doAt = MutableLiveData<LocalDateTime?>(null)
    private val _addToCalendar = MutableLiveData(false)

    private val _timeToCompleteHoursError = MutableLiveData<String?>(null)
    val timeToCompleteHoursError: LiveData<String?> = _timeToCompleteHoursError
    private val _timeToCompleteMinsError = MutableLiveData<String?>(null)
    val timeToCompleteMinsError: LiveData<String?> = _timeToCompleteMinsError
    private val _remindAtError = MutableLiveData<String?>(null)
    val remindAtError: LiveData<String?> = _remindAtError
    private val _doAtError = MutableLiveData<String?>(null)
    val doAtError: LiveData<String?> = _doAtError
    private val _isFormValid = MutableLiveData(true)
    val isFormValid: LiveData<Boolean> = _isFormValid
    private val _onTaskAdded = MutableLiveData<Boolean>()
    val onTaskAdded: LiveData<Boolean> = _onTaskAdded

    fun onTitleChanged(value: String) {
        if (value.isBlank() || value.isEmpty()) _title.value = "New Task"
        else _title.value = value
    }

    fun onDescriptionChanged(value: String) {
        _description.value = value
    }

    fun onTimeToCompleteHoursChanged(value: String) {
        if (value.isNotEmpty()) {
            _timeToCompleteHours.value = value.toInt()
            _timeToCompleteHours.value?.let {
                if (it > 7200) _timeToCompleteHoursError.value = "Hours too large"
                else _timeToCompleteHoursError.value = null
            }
        }
        else {
            _timeToCompleteHours.value = null
            _timeToCompleteHoursError.value = "Required"
        }
        validate()
    }

    fun onTimeToCompleteMinsChanged(value: String) {
        if (value.isNotEmpty()) {
            _timeToCompleteMins.value = value.toInt()
            _timeToCompleteMinsError.value = null
        }
        else {
            _timeToCompleteMins.value = null
            _timeToCompleteMinsError.value = "Required"
        }
        validate()
    }

    fun onTaskStatusChanged(value: TaskStatus) {
        _taskStatus.value = value
    }

    fun onPriorityLevelChanged(value: Float) {
        _priorityLevel.value = value.toInt().toByte()
    }

    fun onRemindAtChanged(value: String?) {
        _remindAt.value = value?.takeIf { it.isNotEmpty() }?.let {
            LocalDateTime.parse(it, getDateTimeFormatter())
        }

        validateRemindAt()
        validate()
    }

    fun onDueChanged(value: String?) {
        _due.value = value?.takeIf { it.isNotEmpty() }?.let {
            LocalDateTime.parse(it, getDateTimeFormatter())
        }

        validateRemindAt()
        validateDoAt()
        validate()
    }

    fun onDoAtChanged(value: String?) {
        _doAt.value = value?.takeIf { it.isNotEmpty() }?.let {
            LocalDateTime.parse(it, getDateTimeFormatter())
        }

        validateRemindAt()
        validateDoAt()
        validate()
    }

    fun onAddToCalendarChanged(value: Boolean) {
        _addToCalendar.value = value
    }

    private fun validate() {
        _isFormValid.value = timeToCompleteHoursError.value == null &&
                timeToCompleteMinsError.value == null &&
                remindAtError.value == null
    }

    private fun validateRemindAt() {
        _remindAtError.value = when {
            _remindAt.value != null
                    && _due.value != null
                    && _remindAt.value?.isAfter(_due.value) == true ->
                        "This value cannot exceed the due date!"
            _remindAt.value != null
                    && _doAt.value != null
                    && _remindAt.value?.isAfter(_doAt.value) == true ->
                        "This value cannot exceed the do at date!"
            else -> null
        }
    }

    private fun validateDoAt() {
        _doAtError.value = when {
            _due.value != null
                    && _doAt.value != null
                    && _doAt.value?.isAfter(_due.value) == true ->
                        "This value cannot exceed the due date!"
            else -> null
        }
    }

    fun addTask() {
        val newTask = Task(
            title = _title.value!!,
            description = _description.value,
            status = _taskStatus.value!!,
            priorityLevel = _priorityLevel.value!!,
            timeToCompleteMins = _timeToCompleteMins.value!! + _timeToCompleteHours.value!! * 60,
            remindAt = _remindAt.value,
            due = _due.value,
            doAt = _doAt.value,
            addToCalendar = _addToCalendar.value!!
        )

        viewModelScope.launch {
            taskRepo.insertTask(newTask)
            _onTaskAdded.postValue(true)
        }
    }
}