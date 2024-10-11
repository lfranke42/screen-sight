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
import de.lflab.screensight.capture.CaptureSupportActivity
import de.lflab.screensight.network.GenerativeAiRepository
import de.lflab.screensight.ui.conversation.ConversationActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.FileNotFoundException

class ScreenSightAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)

    private val aiRepository: GenerativeAiRepository by inject()

    private var internalAccessibilityButtonController: AccessibilityButtonController? = null
    private var accessibilityButtonCallback: AccessibilityButtonCallback? = null
    private var isAccessibilityButtonAvailable: Boolean = false

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.i(TAG, "Received Intent for $action")

            when (action) {
                ACTION_SCREENSHOT_SAVED -> {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    val filename = intent.getStringExtra("image")
                    if (filename == null) {
                        Log.e(TAG, "No image received!")
                        return
                    }
                    try {
                        val inputStream = openFileInput(filename)
                        val screenshotBitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream.close()
                        evaluateImage(screenshotBitmap)
                    } catch (e: FileNotFoundException) {
                        Log.e(TAG, "File not found! ${e.message}")
                        return
                    }
                }
            }
        }
    }

    override fun onCreate() {
        // Start foreground service if permission is given
        val intentFilter = IntentFilter(ACTION_START_FOREGROUND)
        ContextCompat.registerReceiver(
            this,
            broadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )

        // Handle successful screenshot creation
        val screenshotReceiveIntentFilter = IntentFilter(ACTION_SCREENSHOT_SAVED)
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
            internalAccessibilityButtonController?.isAccessibilityButtonAvailable == true

        serviceInfo = serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_ACCESSIBILITY_BUTTON
        }

        accessibilityButtonCallback = object : AccessibilityButtonCallback() {
            override fun onClicked(controller: AccessibilityButtonController?) {
                startCaptureActivity()
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

    private fun startCaptureActivity() {
        startRunningNotification()

        val intent = Intent(this, CaptureSupportActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
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
        val prompt =
            "You are talking to a visually impaired user. " +
                    "Take a look at this screenshot that was taken on an android device. " +
                    "Ignore the system UI and only pay attention to the actual app content. " +
                    "Describe what is being displayed, focus on elements that could be potentially inaccessible to the user"

        serviceScope.launch {
            val response = aiRepository.generateContent(bitmap, prompt)
            response?.let {
                Log.d(TAG, it)
                launchConversationActivity(it)
            }
        }

    }

    private fun launchConversationActivity(response: String) {
        val intent = Intent(this, ConversationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(AI_RESPONSE_KEY, response)
        startActivity(intent)
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        // Not used
    }

    override fun onInterrupt() {
        // Not used
    }

    companion object {
        private const val TAG = "ScreenSightAccessibilityService"
        const val ACTION_SCREENSHOT_SAVED = "action_screenshot_saved"
        const val ACTION_START_FOREGROUND = "action_start_foreground"
        const val AI_RESPONSE_KEY = "ai_response"
    }
}
