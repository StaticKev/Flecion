package com.statickev.flecion.presentation.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.statickev.flecion.R
import com.statickev.flecion.data.model.TaskStatus
import com.statickev.flecion.databinding.ActivityTaskDetailBinding
import com.statickev.flecion.presentation.presentationUtil.generalSetup
import com.statickev.flecion.presentation.presentationUtil.showDatePicker
import com.statickev.flecion.presentation.presentationUtil.showTimePicker
import com.statickev.flecion.presentation.viewModel.TaskDetailViewModel
import com.statickev.flecion.util.getConcatenatedDateTime
import com.statickev.flecion.util.getFormattedDateTime
import com.statickev.flecion.util.minsToFormattedDuration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.getValue

@AndroidEntryPoint
class TaskDetailActivity : AppCompatActivity() {

    private val viewModel: TaskDetailViewModel by viewModels {
        defaultViewModelProviderFactory
    }

    private var isSet = false
    private var isReverted = false
    private lateinit var binding: ActivityTaskDetailBinding

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        generalSetup(this)

        binding = ActivityTaskDetailBinding.inflate(layoutInflater)

        onBackPressedDispatcher.addCallback(this) {
            if (viewModel.isFormValid) finish()
            else {
                MaterialAlertDialogBuilder(this@TaskDetailActivity)
                    .setTitle("Invalid Inputs")
                    .setMessage("Proceeding will keep the previous value of each invalid fields.")
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.revertChanges(true)
                        finish()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        with (binding) {

            btnBack.setOnClickListener {
                if (viewModel.isFormValid) finish()
                else {
                    MaterialAlertDialogBuilder(this@TaskDetailActivity)
                        .setTitle("Invalid Inputs")
                        .setMessage("Proceeding will keep the previous value of each invalid fields.")
                        .setPositiveButton("Yes") { _, _ ->
                            viewModel.revertChanges(true)
                            finish()
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            }

            btnUndo.setOnClickListener {
                MaterialAlertDialogBuilder(this@TaskDetailActivity)
                    .setTitle("Revert Changes")
                    .setMessage("Revert all field to the initial state?")
                    .setPositiveButton("Yes") { _, _ ->
                        currentFocus?.clearFocus()
                        isReverted = true
                        viewModel.revertChanges(false)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(this@TaskDetailActivity)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure want to delete this task?")
                    .setPositiveButton("Yes") { _, _ ->
                        currentFocus?.clearFocus()
                        viewModel.deleteTask()
                        finish()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            tgStatus.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    when (checkedId) {
                        R.id.rdb_pending -> viewModel.onStatusChanged(TaskStatus.PENDING)
                        R.id.rdb_on_hold -> viewModel.onStatusChanged(TaskStatus.ON_HOLD)
                        R.id.rdb_ongoing -> viewModel.onStatusChanged(TaskStatus.ONGOING)
                    }
                }
            }

            tgStatusPendingHidden.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    when (checkedId) {
                        R.id.rdb_on_hold_pending_hidden -> viewModel.onStatusChanged(TaskStatus.ON_HOLD)
                        R.id.rdb_ongoing_pending_hidden -> viewModel.onStatusChanged(TaskStatus.ONGOING)
                    }
                }
            }

            sdCompletionRate.addOnChangeListener { _, value, _ ->
                viewModel.onPriorityLevelChanged(value)
            }

            etRemindAt.apply {
                isFocusable = false
                isClickable = true
                isCursorVisible = false

                setOnClickListener {
                    if (binding.etRemindAt.text.isNullOrEmpty()) {
                        lifecycleScope.launch {
                            try {
                                val dateMillis = showDatePicker(supportFragmentManager)
                                val timeMinutes = showTimePicker(supportFragmentManager)

                                binding.etRemindAt.setText(getConcatenatedDateTime(dateMillis, timeMinutes))
                                viewModel.onRemindAtChanged(binding.etRemindAt.text.toString())
                                // TODO: Remove when the feature is implemented.
                                Toast.makeText(
                                    context,
                                    "This feature is under development!",
                                    Toast.LENGTH_SHORT).show()
                            } catch (_: CancellationException) { }
                        }
                    } else {
                        binding.etRemindAt.text = null
                        viewModel.onRemindAtChanged(null)
                    }
                }
            }

            etDue.apply {
                isFocusable = false
                isClickable = true
                isCursorVisible = false

                setOnClickListener {
                    if (binding.etDue.text.isNullOrEmpty()) {
                        lifecycleScope.launch {
                            try {
                                val dateMillis = showDatePicker(supportFragmentManager)
                                val timeMinutes = showTimePicker(supportFragmentManager)

                                binding.etDue.setText(getConcatenatedDateTime(dateMillis, timeMinutes))
                                viewModel.onDueChanged(binding.etDue.text.toString())
                            } catch (_: kotlinx.coroutines.CancellationException) { }
                        }
                    } else {
                        binding.etDue.text = null
                        viewModel.onDueChanged(null)
                    }
                }
            }

            etDoAt.apply {
                isFocusable = false
                isClickable = true
                isCursorVisible = false

                setOnClickListener {
                    if (binding.etDoAt.text.isNullOrEmpty()) {
                        lifecycleScope.launch {
                            try {
                                val dateMillis = showDatePicker(supportFragmentManager)
                                val timeMinutes = showTimePicker(supportFragmentManager)

                                binding.etDoAt.setText(getConcatenatedDateTime(dateMillis, timeMinutes))
                                viewModel.onDoAtChanged(binding.etDoAt.text.toString())
                                binding.swAddToCalendar.visibility = View.VISIBLE
                            } catch (_: CancellationException) { }
                        }
                    } else {
                        binding.etDoAt.text = null
                        binding.swAddToCalendar.visibility = View.GONE
                        viewModel.onDoAtChanged(null)
                    }
                }
            }

            etDescription.doOnTextChanged { text, _, _, _ ->
                viewModel.onDescriptionChanged(text.toString())
            }
        }

        viewModel.remindAtError.observe(this) { error ->
            binding.tilRemindAt.error = error
            binding.tilRemindAt.isErrorEnabled = error != null
        }

        viewModel.dueError.observe(this) { error ->
            binding.tilDue.error = error
            binding.tilDue.isErrorEnabled = error != null
        }

        viewModel.doAtError.observe(this) { error ->
            binding.tilDoAt.error = error
            binding.tilDoAt.isErrorEnabled = error != null
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.task.collect { task ->
                    if (task != null) {
                        if (!isSet) viewModel.initialTask = task.copy()

                        with (binding) {
                            if (viewModel.initialTask?.status != TaskStatus.PENDING) {
                                tgStatusPendingHidden.visibility = View.VISIBLE
                                tgStatusPendingHidden.check(
                                    when {
                                        task.status == TaskStatus.ON_HOLD -> R.id.rdb_on_hold_pending_hidden
                                        else -> R.id.rdb_ongoing_pending_hidden
                                    }
                                )
                            }
                            else {
                                tgStatus.visibility = View.VISIBLE
                                tgStatus.check(
                                    when {
                                        task.status == TaskStatus.PENDING -> R.id.rdb_pending
                                        task.status == TaskStatus.ON_HOLD -> R.id.rdb_on_hold
                                        else -> R.id.rdb_ongoing
                                    }
                                )
                            }

                            if (task.addToCalendar) swAddToCalendar.visibility = View.GONE
                            else swAddToCalendar.visibility = View.VISIBLE

                            tvTitle.text = task.title
                            tvCompletionRate.text = buildString {
                                append(task.completionRate.toString())
                                append("%")
                            }
                            sdCompletionRate.value = task.completionRate.toFloat()
                            tvTimeToComplete.text = minsToFormattedDuration(task.timeLeftToComplete)
                            tvPriorityLevel.text = task.priorityLevel.toString()
                            swAddToCalendar.isChecked = task.addToCalendar

                            if (!isSet || isReverted) {
                                task.remindAt?.let { remindAt ->
                                    etRemindAt.setText(getFormattedDateTime(remindAt))
                                }
                                task.due?.let { due ->
                                    etDue.setText(getFormattedDateTime(due))
                                }
                                task.doAt?.let { doAt ->
                                    etDoAt.setText(getFormattedDateTime(doAt))
                                }
                                task.description?.let { description ->
                                    etDescription.setText(description)
                                }
                                isReverted = false
                            }
                            isSet = true
                        }
                    }
                }
            }
        }

        setContentView(binding.root)
    }
}
