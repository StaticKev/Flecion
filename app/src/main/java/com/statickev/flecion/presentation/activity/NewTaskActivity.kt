package com.statickev.flecion.presentation.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.statickev.flecion.R
import com.statickev.flecion.data.model.TaskStatus
import com.statickev.flecion.databinding.ActivityNewTaskBinding
import com.statickev.flecion.presentation.presentationUtil.generalSetup
import com.statickev.flecion.presentation.viewModel.NewTaskViewModel
import com.statickev.flecion.util.getFormattedDateTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

// TODO: Implement the activity!
// TODO: Finish the activity once back button is clicked, send a confirmation message first.

@AndroidEntryPoint
class NewTaskActivity : AppCompatActivity() {
    private val newTaskViewModel: NewTaskViewModel by viewModels()
    private lateinit var binding: ActivityNewTaskBinding

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        generalSetup(this)

        onBackPressedDispatcher.addCallback(this) {
            MaterialAlertDialogBuilder(this@NewTaskActivity)
                .setTitle("Confirm")
                .setMessage("Go back and discard your changes?")
                .setPositiveButton("Yes") { _, _ ->
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }

        binding = ActivityNewTaskBinding.inflate(layoutInflater)

        // TODO: MAKE THE UI STATELESS!
        binding.etHours.setText("0")
        binding.etMinutes.setText("0")
        binding.rdbPending.isChecked = true

        binding.btnBack.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Confirm")
                .setMessage("Go back and discard your changes?")
                .setPositiveButton("Yes") { _, _ ->
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }

        binding.swAddToCalendar.visibility = View.GONE

        binding.btnInfo.setOnClickListener {
            // TODO: Remove when the feature is implemented.
            Toast.makeText(
                this,
                "This feature is under development!",
                Toast.LENGTH_SHORT).show()
        }

        binding.etHours.filters = arrayOf(
            InputFilter { source, start, end, dest, dstart, dend ->
                val newValue = (dest.subSequence(0, dstart).toString() +
                        source.subSequence(start, end) +
                        dest.subSequence(dend, dest.length))

                if (source.contains("-")) return@InputFilter ""

                if (newValue.length > 1 && newValue.startsWith("0")) {
                    return@InputFilter ""
                }

                null
            },
            InputFilter.LengthFilter(4)
        )

        binding.etMinutes.filters = arrayOf(
            InputFilter { source, start, end, dest, dstart, dend ->
                val newValue = dest.subSequence(0, dstart).toString() +
                        source.subSequence(start, end) +
                        dest.subSequence(dend, dest.length)

                if (newValue.isEmpty()) return@InputFilter null

                if (!newValue.matches(Regex("\\d{0,2}"))) return@InputFilter ""

                if (newValue.length == 2 && newValue.startsWith("0")) {
                    return@InputFilter ""
                }

                if (newValue.length == 2) {
                    val num = newValue.toIntOrNull() ?: return@InputFilter ""
                    if (num !in 0..59) return@InputFilter ""
                }

                null
            },
            InputFilter.LengthFilter(2)
        )

        binding.etTitle.doOnTextChanged {
            text, _, _, _ ->
            newTaskViewModel.onTitleChanged(text.toString())
        }

        binding.tilHours.editText?.doOnTextChanged { text, _, _, _ ->
            newTaskViewModel.onTimeToCompleteHoursChanged(text.toString())
        }

        binding.tilMinutes.editText?.doOnTextChanged { text, _, _, _ ->
            newTaskViewModel.onTimeToCompleteMinsChanged(text.toString())
        }

        binding.tgStatus.addOnButtonCheckedListener { _, checkedId, _ ->
            when (checkedId) {
                R.id.rdb_pending -> newTaskViewModel.onTaskStatusChanged(TaskStatus.PENDING)
                R.id.rdb_on_hold -> newTaskViewModel.onTaskStatusChanged(TaskStatus.ON_HOLD)
                R.id.rdb_ongoing -> newTaskViewModel.onTaskStatusChanged(TaskStatus.ONGOING)
            }
        }

        binding.sdPriorityLevel.addOnChangeListener { _, value, _ ->
            newTaskViewModel.onPriorityLevelChanged(value)
        }

        binding.etRemindAt.apply {
            isFocusable = false
            isClickable = true
            isCursorVisible = false

            setOnClickListener {
                if (binding.etRemindAt.text.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        try {
                            val dateMillis = showDatePicker()
                            val timeMinutes = showTimePicker()

                            binding.etRemindAt.setText(getFormattedDateTime(dateMillis, timeMinutes))
                            newTaskViewModel.onRemindAtChanged(binding.etRemindAt.text.toString())
                            // TODO: Remove when the feature is implemented.
                            Toast.makeText(
                                context,
                                "This feature is under development!",
                                Toast.LENGTH_SHORT).show()
                        } catch (_: CancellationException) { }
                    }
                } else {
                    binding.etRemindAt.text = null
                    newTaskViewModel.onRemindAtChanged(null)
                }
            }
        }

        binding.etDue.apply {
            isFocusable = false
            isClickable = true
            isCursorVisible = false

            setOnClickListener {
                if (binding.etDue.text.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        try {
                            val dateMillis = showDatePicker()
                            val timeMinutes = showTimePicker()

                            binding.etDue.setText(getFormattedDateTime(dateMillis, timeMinutes))
                            newTaskViewModel.onDueChanged(binding.etDue.text.toString())
                        } catch (_: kotlinx.coroutines.CancellationException) { }
                    }
                } else {
                    binding.etDue.text = null
                    newTaskViewModel.onDueChanged(null)
                }
            }
        }

        binding.etDoAt.apply {
            isFocusable = false
            isClickable = true
            isCursorVisible = false

            setOnClickListener {
                if (binding.etDoAt.text.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        try {
                            val dateMillis = showDatePicker()
                            val timeMinutes = showTimePicker()

                            binding.etDoAt.setText(getFormattedDateTime(dateMillis, timeMinutes))
                            newTaskViewModel.onDoAtChanged(binding.etDoAt.text.toString())
                            binding.swAddToCalendar.visibility = View.VISIBLE
                        } catch (_: CancellationException) { }
                    }
                } else {
                    binding.etDoAt.text = null
                    binding.swAddToCalendar.visibility = View.GONE
                    newTaskViewModel.onDoAtChanged(null)
                }
            }
        }

        binding.etDescription.doOnTextChanged { text, _, _, _ ->
            newTaskViewModel.onDescriptionChanged(text.toString())
        }

        binding.swAddToCalendar.setOnClickListener {
            newTaskViewModel.onAddToCalendarChanged(binding.swAddToCalendar.isChecked)
            // TODO: Remove when the feature is implemented.
            Toast.makeText(
                binding.root.context,
                "This feature is under development!",
                Toast.LENGTH_SHORT).show()
        }

        binding.btnAddTask.setOnClickListener {
            newTaskViewModel.addTask()
        }

        newTaskViewModel.timeToCompleteHoursError.observe(this) { error ->
            binding.tilHours.error = error
            binding.tilHours.isErrorEnabled = error != null

            if (binding.tilHours.isErrorEnabled &&
                !binding.tilMinutes.isErrorEnabled) binding.tilMinutes.helperText = " "
            else binding.tilMinutes.helperText = ""
        }

        newTaskViewModel.timeToCompleteMinsError.observe(this) { error ->
            binding.tilMinutes.error = error
            binding.tilMinutes.isErrorEnabled = error != null

            if (binding.tilMinutes.isErrorEnabled &&
                !binding.tilHours.isErrorEnabled) binding.tilHours.helperText = " "
            else binding.tilHours.helperText = ""
        }

        newTaskViewModel.remindAtError.observe(this) { error ->
            binding.tilRemindAt.error = error
            binding.tilRemindAt.isErrorEnabled = error != null
        }

        newTaskViewModel.doAtError.observe(this) { error ->
            binding.tilDoAt.error = error
            binding.tilRemindAt.isErrorEnabled = error != null
        }

        newTaskViewModel.isFormValid.observe(this) { isValid ->
            binding.btnAddTask.isEnabled = isValid
        }

        newTaskViewModel.onTaskAdded.observe(this) { done ->
            if (done == true) {
                val resultIntent = Intent().apply { putExtra("taskCreated", true) }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        setContentView(binding.root)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun showDatePicker(): Long = suspendCancellableCoroutine { cont ->
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selected ->
            if (cont.isActive) cont.resume(selected)
        }

        picker.addOnNegativeButtonClickListener {
            if (cont.isActive) cont.cancel()
        }

        picker.addOnCancelListener {
            if (cont.isActive) cont.cancel()
        }

        picker.show(supportFragmentManager, "DATE_PICKER")
    }

    suspend fun showTimePicker(): Int = suspendCancellableCoroutine { cont ->
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setTitleText("Select Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val minutes = picker.hour * 60 + picker.minute
            if (cont.isActive) cont.resume(minutes)
        }

        picker.addOnNegativeButtonClickListener {
            if (cont.isActive) cont.cancel()
        }

        picker.addOnCancelListener {
            if (cont.isActive) cont.cancel()
        }

        picker.show(supportFragmentManager, "TIME_PICKER")
    }

}