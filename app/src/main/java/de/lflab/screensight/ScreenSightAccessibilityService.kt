package de.lflab.screensight

import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityButtonController.AccessibilityButtonCallback
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import de.lflab.screensight.support.FloatingServiceSupportActivity

class ScreenSightAccessibilityService : AccessibilityService() {
    private var internalAccessibilityButtonController: AccessibilityButtonController? = null
    private var accessibilityButtonCallback: AccessibilityButtonCallback? = null
    private var isAccessibilityButtonAvailable: Boolean = false

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
        startRunningNotification()

        val intent = Intent(this, FloatingServiceSupportActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun startRunningNotification() {
        val builder = Notification.Builder(this, "running")
            .setContentText("running notification")
            .setSmallIcon(R.drawable.ic_launcher_foreground)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "running",
            "running notification",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
        builder.setChannelId("running")
        val notification = builder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(100, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(100, notification)
        }
    }
}