package com.statickev.flecion.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.statickev.flecion.R
import com.statickev.flecion.data.model.TaskStatus
import com.statickev.flecion.databinding.ActivityNewTaskBinding
import com.statickev.flecion.presentation.presentationUtil.generalSetup
import com.statickev.flecion.presentation.presentationUtil.showDatePicker
import com.statickev.flecion.presentation.presentationUtil.showTimePicker
import com.statickev.flecion.presentation.viewModel.NewTaskViewModel
import com.statickev.flecion.util.getConcatenatedDateTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@AndroidEntryPoint
class NewTaskActivity : AppCompatActivity() {

    companion object {
        const val IS_RECURRING = "IS_RECURRING"
    }

    private val viewModel: NewTaskViewModel by viewModels()
    private lateinit var binding: ActivityNewTaskBinding

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

        with (binding) {
            etHours.setText("0")
            etMinutes.setText("0")
            rdbPending.isChecked = true
            swSendNotification.isChecked = true

            if (intent.getBooleanExtra(IS_RECURRING, false)) {
                rdbRecurring.isChecked = true
                displayFieldsB()
            }
            else {
                rdbOneTime.isChecked = true
                displayFieldsA()
            }

            btnBack.setOnClickListener {
                MaterialAlertDialogBuilder(this@NewTaskActivity)
                    .setTitle("Confirm")
                    .setMessage("Go back and discard your changes?")
                    .setPositiveButton("Yes") { _, _ ->
                        finish()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            etHours.filters = arrayOf(
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

            etMinutes.filters = arrayOf(
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

            etRecurInterval.filters = arrayOf(
                InputFilter { source, start, end, dest, dstart, dend ->
                    val newValue = (dest.subSequence(0, dstart).toString() +
                            source.subSequence(start, end) +
                            dest.subSequence(dend, dest.length))

                    if (source.contains("-")) return@InputFilter ""

                    if (dstart == 0 && source.toString() == "0") return@InputFilter ""

                    if (newValue.length > 1 && newValue.startsWith("0")) {
                        return@InputFilter ""
                    }

                    null
                },
                InputFilter.LengthFilter(3)
            )

            etTitle.doOnTextChanged {
                    text, _, _, _ ->
                viewModel.onTitleChanged(text.toString())
            }

            tilHours.editText?.doOnTextChanged { text, _, _, _ ->
                viewModel.onTimeToCompleteHoursChanged(text.toString())
            }

            tilMinutes.editText?.doOnTextChanged { text, _, _, _ ->
                viewModel.onTimeToCompleteMinsChanged(text.toString())
            }

            tilRecurInterval.editText?.doOnTextChanged { text, _, _, _ ->
                viewModel.onRecurIntervalChanged(text.toString())
            }

            tgFrequency.addOnButtonCheckedListener { group, checkedId, isChecked ->
                if (!isChecked) return@addOnButtonCheckedListener

                when (checkedId) {
                    R.id.rdb_one_time -> displayFieldsA()
                    R.id.rdb_recurring -> displayFieldsB()
                }
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

            sdPriorityLevel.addOnChangeListener { _, value, _ ->
                viewModel.onPriorityLevelChanged(value)
                binding.tvPriorityLevel.text = value.toInt().toString()
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
                        binding.swAddToCalendar.isChecked = false
                        binding.swAddToCalendar.visibility = View.GONE
                        viewModel.onDoAtChanged(null)
                    }
                }
            }

            etDescription.doOnTextChanged { text, _, _, _ ->
                viewModel.onDescriptionChanged(text.toString())
            }

            swAddToCalendar.setOnClickListener {
                viewModel.onAddToCalendarChanged(binding.swAddToCalendar.isChecked)
                // TODO: Remove when the feature is implemented.
                Toast.makeText(
                    binding.root.context,
                    "This feature is under development!",
                    Toast.LENGTH_SHORT).show()
            }

            swSendNotification.setOnClickListener {
                viewModel.onSendNotificationChanged(binding.swSendNotification.isChecked)
            }

            btnAddTask.setOnClickListener {
                viewModel.addTask()
            }
        }

        viewModel.timeToCompleteHoursError.observe(this) { error ->
            binding.tilHours.error = error
            binding.tilHours.isErrorEnabled = error != null

            val hoursError = binding.tilHours.isErrorEnabled
            val minsError = binding.tilMinutes.isErrorEnabled

            binding.tilHours.helperText =
                if (hoursError && !minsError) "" else if (!hoursError && minsError) " " else ""
            binding.tilMinutes.helperText =
                if (hoursError && !minsError) " " else if (!hoursError && minsError) "" else ""
        }

        viewModel.timeToCompleteMinsError.observe(this) { error ->
            binding.tilMinutes.error = error
            binding.tilMinutes.isErrorEnabled = error != null

            val hoursError = binding.tilHours.isErrorEnabled
            val minsError = binding.tilMinutes.isErrorEnabled

            binding.tilHours.helperText =
                if (hoursError && !minsError) ""
                else if (!hoursError && minsError) " "
                else ""
            binding.tilMinutes.helperText =
                if (hoursError && !minsError) " "
                else if (!hoursError && minsError) ""
                else ""
        }

        viewModel.recurIntervalError.observe(this) { error ->
            binding.tilRecurInterval.error = error
            binding.tilRecurInterval.isErrorEnabled = error != null
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

        viewModel.isFormValid.observe(this) { isValid ->
            binding.btnAddTask.isEnabled = isValid
        }

        viewModel.onTaskAdded.observe(this) { done ->
            if (done == true) {
                val resultIntent = Intent().apply { putExtra("taskCreated", true) }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        setContentView(binding.root)
    }

    fun displayFieldsA() {
        with (binding){
            viewModel.onStatusChanged(TaskStatus.PENDING)
            etRecurInterval.setText("1")
            viewModel.onRecurIntervalChanged(null)
            swSendNotification.isChecked = true

            tvStatus.visibility = View.VISIBLE
            tgStatus.visibility = View.VISIBLE
            tvPriorityLevelLabel.visibility = View.VISIBLE
            tvPriorityLevel.visibility = View.VISIBLE
            sdPriorityLevel.visibility = View.VISIBLE
            etDue.visibility = View.VISIBLE
            tilDue.visibility = View.VISIBLE
            etDoAt.visibility = View.VISIBLE
            tilDoAt.visibility = View.VISIBLE

            tvRecurInterval.visibility = View.GONE
            etRecurInterval.visibility = View.GONE
            tilRecurInterval.visibility = View.GONE
            swSendNotification.visibility = View.GONE
            swAddToCalendar.visibility = View.GONE

            val params = tilRemindAt.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(
                tilRemindAt.marginLeft,
                tilRemindAt.marginTop - 12,
                tilRemindAt.marginRight,
                tilRemindAt.marginBottom
            )
            tilRemindAt.layoutParams = params
        }
    }

    fun displayFieldsB() {
        with (binding) {
            viewModel.onRecurIntervalChanged("1")
            etRecurInterval.setText("1")

            rdbPending.isChecked = true
            viewModel.onStatusChanged(TaskStatus.ON_REPEAT)
            sdPriorityLevel.value = 1.0f
            viewModel.onPriorityLevelChanged(null)
            etDue.text?.clear()
            viewModel.onDueChanged(etDue.text.toString())
            etDoAt.text?.clear()
            viewModel.onDoAtChanged(etDoAt.text.toString())
            swAddToCalendar.isChecked = false
            swAddToCalendar.visibility = View.GONE

            tvRecurInterval.visibility = View.VISIBLE
            etRecurInterval.visibility = View.VISIBLE
            tilRecurInterval.visibility = View.VISIBLE
            swSendNotification.visibility = View.VISIBLE

            tvStatus.visibility = View.GONE
            tgStatus.visibility = View.GONE
            tvPriorityLevelLabel.visibility = View.GONE
            tvPriorityLevel.visibility = View.GONE
            sdPriorityLevel.visibility = View.GONE
            etDue.visibility = View.GONE
            tilDue.visibility = View.GONE
            etDoAt.visibility = View.GONE
            tilDoAt.visibility = View.GONE

            val params = tilRemindAt.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(
                tilRemindAt.marginLeft,
                tilRemindAt.marginTop + 12,
                tilRemindAt.marginRight,
                tilRemindAt.marginBottom
            )
            tilRemindAt.layoutParams = params
        }
    }
}