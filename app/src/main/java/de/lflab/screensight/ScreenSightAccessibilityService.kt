package de.lflab.screensight

import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityButtonController.AccessibilityButtonCallback
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import de.lflab.screensight.support.CaptureSupportActivity
import java.io.FileNotFoundException

private const val TAG = "ScreenSightAccessibilityService"

class ScreenSightAccessibilityService : AccessibilityService() {
    private var internalAccessibilityButtonController: AccessibilityButtonController? = null
    private var accessibilityButtonCallback: AccessibilityButtonCallback? = null
    private var isAccessibilityButtonAvailable: Boolean = false

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.i(TAG, "Received Intent for $action")

            when (action) {
                "action_screenshot_saved" -> {
                    val filename = intent.getStringExtra("image")
                    if (filename == null) {
                        Log.e(TAG, "No image received!")
                        return
                    }
                    try {
                        val inputStream = openFileInput(filename)
                        val screenshotBitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close()
                        evaluateImage(screenshotBitmap)
                    } catch (e: FileNotFoundException) {
                        Log.e(TAG, "File not found! ${e.message}")
                        return
                    }
                }

                "action_start_foreground" -> {
                    startRunningNotification()
                }
            }
        }
    }

    override fun onCreate() {
        // Start foreground service if permission is given
        val intentFilter = IntentFilter("action_start_foreground")
        ContextCompat.registerReceiver(
            this,
            broadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )

        // Handle successful screenshot creation
        val screenshotReceiveIntentFilter = IntentFilter("action_screenshot_saved")
        ContextCompat.registerReceiver(
            this,
            broadcastReceiver,
            screenshotReceiveIntentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onServiceConnected() {
        internalAccessibilityButtonController = accessibilityButtonController
        isAccessibilityButtonAvailable =
            internalAccessibilityButtonController?.isAccessibilityButtonAvailable ?: false

        serviceInfo = serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_ACCESSIBILITY_BUTTON
        }

        accessibilityButtonCallback = object : AccessibilityButtonCallback() {
            override fun onClicked(controller: AccessibilityButtonController?) {
                initCapture()
            }

            override fun onAvailabilityChanged(
                controller: AccessibilityButtonController?,
                available: Boolean,
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

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        TODO("Not yet implemented")
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }

    private fun initCapture() {
        val intent = Intent(this, CaptureSupportActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun startRunningNotification() {
        val builder = Notification.Builder(this, "running")
            .setContentText("ScreenSight is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "running",
            "Screen Sight running notification",
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

    private fun evaluateImage(bitmap: Bitmap) {
        Log.d(TAG, "evaluateImage: STUB")
        // TODO: send image to gemini
    }
}