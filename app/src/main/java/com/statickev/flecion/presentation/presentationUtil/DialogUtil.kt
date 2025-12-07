package com.statickev.flecion.presentation.presentationUtil

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.statickev.flecion.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun showDatePicker(supportFragmentManager: FragmentManager): Long = suspendCancellableCoroutine { cont ->
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

suspend fun showTimePicker(supportFragmentManager: FragmentManager): Int = suspendCancellableCoroutine { cont ->
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

fun showSnackbar(view: View, message: String) {
    val snack = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)

    val snackbarView = snack.view
    snackbarView.setBackgroundResource(R.drawable.drawable_snackbar_container)

    val text = snackbarView.findViewById<TextView>(
        com.google.android.material.R.id.snackbar_text
    )
    text.setTextColor(Color.WHITE)
    text.textSize = 16f

    snack.show()
}