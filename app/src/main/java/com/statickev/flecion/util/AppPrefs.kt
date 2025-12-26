package com.statickev.flecion.util

import android.content.Context
import androidx.core.content.edit

object AppPrefs {
    private const val PREFS = "app_prefs"
    private const val KEY_FIRST_LAUNCH = "first_launch"

    fun isFirstLaunch(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val first = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        if (first) {
            prefs.edit { putBoolean(KEY_FIRST_LAUNCH, false) }
        }
        return first
    }
}
