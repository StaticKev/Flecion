package com.statickev.flecion.presentation.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statickev.flecion.data.model.Task
import com.statickev.flecion.data.model.TaskStatus
import com.statickev.flecion.data.repository.TaskRepository
import com.statickev.flecion.platform.scheduler.scheduleTaskReminder
import com.statickev.flecion.presentation.presentationUtil.doAtIsValid
import com.statickev.flecion.presentation.presentationUtil.dueIsValid
import com.statickev.flecion.presentation.presentationUtil.remindAtIsValid
import com.statickev.flecion.util.getDateTimeFormatter
import com.statickev.flecion.util.toEpochMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class NewTaskViewModel @Inject constructor (
    private val taskRepo: TaskRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    private val _newTask = Task()

    private val _timeToCompleteHours = MutableLiveData(0)
    private val _timeToCompleteMins = MutableLiveData(0)

    private val _timeToCompleteHoursError = MutableLiveData<String?>(null)
    val timeToCompleteHoursError: LiveData<String?> = _timeToCompleteHoursError
    private val _timeToCompleteMinsError = MutableLiveData<String?>(null)
    val timeToCompleteMinsError: LiveData<String?> = _timeToCompleteMinsError
    private val _recurIntervalError = MutableLiveData<String?>(null)
    val recurIntervalError: LiveData<String?> = _recurIntervalError
    private val _remindAtError = MutableLiveData<String?>(null)
    val remindAtError: LiveData<String?> = _remindAtError
    val _dueError = MutableLiveData<String?>(null)
    val dueError: LiveData<String?> = _dueError
    private val _doAtError = MutableLiveData<String?>(null)
    val doAtError: LiveData<String?> = _doAtError

    private val _isFormValid = MutableLiveData(true)
    val isFormValid: LiveData<Boolean> = _isFormValid
    private val _onTaskAdded = MutableLiveData<Boolean>()
    val onTaskAdded: LiveData<Boolean> = _onTaskAdded

    fun onTitleChanged(value: String) {
        _newTask.title =
            if (value.isEmpty() || value.isBlank()) "New Task"
            else value
    }

    fun onDescriptionChanged(value: String) {
        _newTask.description = value
    }

    fun onTimeToCompleteHoursChanged(value: String) {
        if (value.isNotEmpty()) {
            _timeToCompleteHours.value = value.toInt()
            _timeToCompleteHours.value?.let {
                if (it > 7200) _timeToCompleteHoursError.value = "Hours too large"
                else {
                    _timeToCompleteHoursError.value = null

                    _newTask.timeToCompleteMins =
                        (_timeToCompleteHours.value ?: 0) * 60 +
                                (_timeToCompleteMins.value ?: 0)
                }
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

            _newTask.timeToCompleteMins =
                (_timeToCompleteHours.value ?: 0) * 60 +
                        (_timeToCompleteMins.value ?: 0)
        }
        else {
            _timeToCompleteMins.value = null
            _timeToCompleteMinsError.value = "Required"
        }

        validate()
    }

    fun onStatusChanged(value: TaskStatus) {
        _newTask.status = value

        _remindAtError.value = remindAtIsValid(
            _newTask.status,
            _newTask.remindAt,
            _newTask.due,
            _newTask.doAt
        )

        validate()
    }

    fun onPriorityLevelChanged(value: Float?) {
        _newTask.priorityLevel = value?.toInt()?.toByte()
    }

    fun onRecurIntervalChanged(value: String?) {
        if (value.isNullOrBlank()) {
            _newTask.recurInterval = null
            _recurIntervalError.value = "Required"
        }
        else {
            _newTask.recurInterval = value?.toInt()
            _recurIntervalError.value = null
        }

        validate()
    }

    fun onRemindAtChanged(value: String?) {
        _newTask.remindAt = value?.takeIf { it.isNotEmpty() }?.let {
            LocalDateTime.parse(it, getDateTimeFormatter())
        }

        _remindAtError.value = remindAtIsValid(
            _newTask.status,
            _newTask.remindAt,
            _newTask.due,
            _newTask.doAt
        )

        validate()
    }

    fun onDueChanged(value: String?) {
        _newTask.due = value?.takeIf { it.isNotEmpty() }?.let {
            LocalDateTime.parse(it, getDateTimeFormatter())
        }

        _remindAtError.value = remindAtIsValid(
            _newTask.status,
            _newTask.remindAt,
            _newTask.due,
            _newTask.doAt
        )
        _dueError.value = _newTask.due?.let {
            dueIsValid(it)
        }
        _doAtError.value = _newTask.doAt?.let {
            doAtIsValid(_newTask.due, it)
        }

        validate()
    }

    fun onDoAtChanged(value: String?) {
        _newTask.doAt = value?.takeIf { it.isNotEmpty() }?.let {
            LocalDateTime.parse(it, getDateTimeFormatter())
        }
        if (_newTask.doAt == null) _newTask.addToCalendar = false

        _remindAtError.value = remindAtIsValid(
            _newTask.status,
            _newTask.remindAt,
            _newTask.due,
            _newTask.doAt
        )
        _doAtError.value = _newTask.doAt?.let {
            doAtIsValid(_newTask.due, it)
        }

        validate()
    }

    fun onAddToCalendarChanged(value: Boolean) {
        _newTask.addToCalendar = value
    }

    fun onSendNotificationChanged(value: Boolean) {
        _newTask.sendNotification = value
    }

    private fun validate() {
        _isFormValid.value = timeToCompleteHoursError.value == null &&
                timeToCompleteMinsError.value == null &&
                recurIntervalError.value == null &&
                remindAtError.value == null &&
                dueError.value == null &&
                doAtError.value == null
    }

    fun addTask() {
        viewModelScope.launch {
            val taskId = taskRepo.insertTask(_newTask)

            if (_newTask.sendNotification) {
                try {
                    _newTask.remindAt?.let { remindAt ->
                        scheduleTaskReminder(
                            context = appContext,
                            taskId = taskId,
                            taskTitle = _newTask.title,
                            triggerAtMillis = remindAt.toEpochMillis(),
                            isRecurring = _newTask.recurInterval != null
                        )
                    }
                } catch (e: SecurityException) {}
            }

            _onTaskAdded.postValue(true)
        }
    }
}