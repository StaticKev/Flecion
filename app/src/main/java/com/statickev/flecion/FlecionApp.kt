package com.statickev.flecion

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.statickev.flecion.platform.notification.createNotificationChannel
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FlecionApp : Application() {

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        createNotificationChannel(this)
    }

}