package de.lflab.screensight

import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityButtonController.AccessibilityButtonCallback
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import de.lflab.screensight.support.FloatingServiceSupportActivity

class ScreenSightAccessibilityService : AccessibilityService() {
    private var internalAccessibilityButtonController: AccessibilityButtonController? = null
    private var accessibilityButtonCallback: AccessibilityButtonCallback? = null
    private var isAccessibilityButtonAvailable: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onServiceConnected() {
        internalAccessibilityButtonController = accessibilityButtonController
        isAccessibilityButtonAvailable =
            internalAccessibilityButtonController?.isAccessibilityButtonAvailable ?: false

        // if (!isAccessibilityButtonAvailable) return

        serviceInfo = serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_ACCESSIBILITY_BUTTON
        }

        accessibilityButtonCallback = object : AccessibilityButtonCallback() {
            override fun onClicked(controller: AccessibilityButtonController?) {
                Log.d("ScreenSightAccessibilityService", "Accessibility button clicked!")
                initAnalysis()
            }

            override fun onAvailabilityChanged(
                controller: AccessibilityButtonController?,
                available: Boolean
            ) {
                if (controller == internalAccessibilityButtonController) {
                    isAccessibilityButtonAvailable = available
                }
            }
        }

        accessibilityButtonCallback?.also {
            internalAccessibilityButtonController?.registerAccessibilityButtonCallback(it)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        TODO("Not yet implemented")
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }

    private fun initAnalysis() {
        val intent = Intent(this, FloatingServiceSupportActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}