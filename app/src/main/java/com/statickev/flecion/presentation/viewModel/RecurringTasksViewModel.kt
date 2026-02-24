package com.statickev.flecion.presentation.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statickev.flecion.data.model.Task
import com.statickev.flecion.data.model.TaskStatus
import com.statickev.flecion.data.repository.TaskRepository
import com.statickev.flecion.platform.scheduler.cancelTaskReminder
import com.statickev.flecion.platform.scheduler.scheduleTaskReminder
import com.statickev.flecion.util.toEpochMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecurringTasksViewModel @Inject constructor(
    private val repo: TaskRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    val taskState = repo.getTaskByStatus(TaskStatus.ON_REPEAT)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun toggleNotification(task: Task) {
        try {
            viewModelScope.launch {
                if (task.sendNotification) {
                    // Cancel reminder
                    cancelTaskReminder(appContext, task.id)

                    // Update 'sendNotification' to false
                    repo.updateTask(
                        task.copy(
                            sendNotification = false
                        )
                    )
                }
                else {
                    task.remindAt?.let {
                        scheduleTaskReminder(
                            context = appContext,
                            taskId = task.id,
                            taskTitle = task.title,
                            triggerAtMillis = it.toEpochMillis(),
                            isRecurring = task.recurInterval != null
                        )
                    }

                    repo.updateTask(
                        task.copy(
                            sendNotification = true
                        )
                    )
                }
            }
        } catch (e: SecurityException) {}
    }
}