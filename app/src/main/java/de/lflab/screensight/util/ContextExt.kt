package de.lflab.screensight.util

import android.content.Context
import android.view.WindowManager

val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels

val Context.screenHeight: Int
    get() = resources.displayMetrics.heightPixels

val Context.deviceWidth: Int
    get() {
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.maximumWindowMetrics
        return display.bounds.width()
    }

val Context.deviceHeight: Int
    get() {
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.maximumWindowMetrics
        return display.bounds.height()
    }
