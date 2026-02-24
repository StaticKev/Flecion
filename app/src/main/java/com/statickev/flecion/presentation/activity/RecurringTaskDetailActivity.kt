package com.statickev.flecion.presentation.activity

import android.os.Bundle
import android.text.InputFilter
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.statickev.flecion.databinding.ActivityRecurringTaskDetailBinding
import com.statickev.flecion.presentation.presentationUtil.generalSetup
import com.statickev.flecion.presentation.presentationUtil.showDatePicker
import com.statickev.flecion.presentation.presentationUtil.showTimePicker
import com.statickev.flecion.presentation.viewModel.RecurringTaskDetailViewModel
import com.statickev.flecion.util.getConcatenatedDateTime
import com.statickev.flecion.util.getFormattedDateTime
import com.statickev.flecion.util.minsToFormattedDuration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.getValue

@AndroidEntryPoint
class RecurringTaskDetailActivity : AppCompatActivity() {
    private val viewModel: RecurringTaskDetailViewModel by viewModels {
        defaultViewModelProviderFactory
    }
    private lateinit var binding: ActivityRecurringTaskDetailBinding
    private var isSet = false
    private var isReverted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        generalSetup(this)

        binding = ActivityRecurringTaskDetailBinding.inflate(layoutInflater)

        onBackPressedDispatcher.addCallback(this) {
            if (viewModel.isFormValid) finish()
            else {
                MaterialAlertDialogBuilder(this@RecurringTaskDetailActivity)
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
                    MaterialAlertDialogBuilder(this@RecurringTaskDetailActivity)
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
                MaterialAlertDialogBuilder(this@RecurringTaskDetailActivity)
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
                MaterialAlertDialogBuilder(this@RecurringTaskDetailActivity)
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

            etRecurInterval.filters = arrayOf(
                InputFilter { source, start, end, dest, dstart, dend ->
                    val newValue = (
                            dest.subSequence(0, dstart).toString() +
                                    source.subSequence(start, end) +
                                    dest.subSequence(dend, dest.length)
                            )

                    if (source.contains("-")) return@InputFilter ""

                    if (newValue == "0") return@InputFilter ""
                    if (newValue.length > 1 && newValue.startsWith("0")) return@InputFilter ""

                    null
                },
                InputFilter.LengthFilter(3)
            )

            tilRecurInterval.editText?.doOnTextChanged { text, _, _, _ ->
                viewModel.onRecurIntervalChanged(text.toString())
            }

            tilDescription.editText?.doOnTextChanged { text, _, _, _ ->
                viewModel.onDescriptionChanged(text.toString())
            }

            etRemindAt.apply {
                isFocusable = false
                isClickable = true
                isCursorVisible = false

                setOnClickListener {
                    if (etRemindAt.text.isNullOrEmpty()) {
                        lifecycleScope.launch {
                            try {
                                val dateMillis = showDatePicker(supportFragmentManager)
                                val timeMinutes = showTimePicker(supportFragmentManager)

                                etRemindAt.setText(getConcatenatedDateTime(dateMillis, timeMinutes))
                                viewModel.onRemindAtChanged(etRemindAt.text.toString())
                            } catch (_: CancellationException) { }
                        }
                    } else {
                        etRemindAt.text = null
                        viewModel.onRemindAtChanged(null)
                    }
                }
            }

            swSendNotification.setOnClickListener {
                viewModel.onSendNotificationChanged(swSendNotification.isChecked)
            }
        }

        viewModel.recurIntervalError.observe(this) { error ->
            binding.tilRecurInterval.error = error
            binding.tilRecurInterval.isErrorEnabled = error != null
        }

        viewModel.remindAtError.observe(this) { error ->
            binding.tilRemindAt.error = error
            binding.tilRemindAt.isErrorEnabled = error != null
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.task.collect { task ->
                    if (task != null) {
                        if (!isSet) viewModel.initialTask = task.copy()

                        with (binding) {
                            tvTitle.text = task.title
                            tvTimeToComplete.text = minsToFormattedDuration(task.timeToCompleteMins)
                            if (!isSet || isReverted) {
                                etRecurInterval.setText(task.recurInterval?.toString() ?: "")
                                task.remindAt?.let {
                                    etRemindAt.setText(getFormattedDateTime(it))
                                }
                                swSendNotification.isChecked = task.sendNotification
                                task.description?.let {
                                    etDescription.setText(task.description)
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