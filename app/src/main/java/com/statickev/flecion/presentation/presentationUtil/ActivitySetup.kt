package com.statickev.flecion.presentation.presentationUtil

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding

@SuppressLint("RestrictedApi")
fun generalSetup(activity: Activity) {

    activity.window.navigationBarColor = ContextCompat.getColor(activity, android.R.color.white)
    WindowInsetsControllerCompat(activity.window, activity.window.decorView).isAppearanceLightNavigationBars = true

    activity.window.apply {
        setDecorFitsSystemWindows(false)
        statusBarColor = Color.TRANSPARENT
        decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                )
        navigationBarColor = Color.TRANSPARENT
    }

    activity.window.apply {
        statusBarColor = Color.TRANSPARENT
        navigationBarColor = Color.TRANSPARENT
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
    controller.show(WindowInsetsCompat.Type.systemBars())

    val contentView = activity.findViewById<View>(android.R.id.content)
    ViewCompat.setOnApplyWindowInsetsListener(contentView) { view, insets ->
        view.updatePadding(top = 0)
        insets
    }
}
