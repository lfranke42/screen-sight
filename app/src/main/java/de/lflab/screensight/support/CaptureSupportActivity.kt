package de.lflab.screensight.support

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import de.lflab.screensight.ScreenSightAccessibilityService
import de.lflab.screensight.databinding.ActivityCaptureSupportBinding
import de.lflab.screensight.util.deviceHeight
import de.lflab.screensight.util.deviceWidth
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream

private const val TAG = "CaptureSupportActivity"

class CaptureSupportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCaptureSupportBinding

    private lateinit var mImageReader: ImageReader

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null

    private var imageRead = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageRead = false

        binding = ActivityCaptureSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mImageReader = ImageReader.newInstance(
            deviceWidth,
            deviceHeight,
            PixelFormat.RGBA_8888,
            1,
        )
        val onImageAvailableListener = OnImageAvailableListener { frame ->
            if (imageRead) return@OnImageAvailableListener

            val screenshot = captureScreenshotFromVirtualDisplay()
            stopMediaProjection()

            val filename = "screenshot.png"
            val destination = File(filesDir, filename)

            try {
                val out = FileOutputStream(destination)
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()

                val screenshotIntent =
                    Intent(ScreenSightAccessibilityService.ACTION_SCREENSHOT_SAVED)
                screenshotIntent.putExtra("image", filename)
                sendBroadcast(screenshotIntent)

                imageRead = true
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mImageReader.setOnImageAvailableListener(onImageAvailableListener, null)

        startMediaProjection()
    }

    private fun startMediaProjection() {
        val mediaProjectionManager: MediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val startMediaProjection = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                handleMediaProjectionActivityResult(mediaProjectionManager, result)
            } else {
                Log.e(TAG, "Failed initiate media projection session!")
            }
        }

        val startForegroundServiceBroadcast =
            Intent(ScreenSightAccessibilityService.ACTION_START_FOREGROUND)
        sendBroadcast(startForegroundServiceBroadcast)
        Log.d(TAG, "startForegroundServiceBroadcast sent")

        startMediaProjection.launch(mediaProjectionManager.createScreenCaptureIntent())
    }

    private fun handleMediaProjectionActivityResult(
        mediaProjectionManager: MediaProjectionManager,
        result: ActivityResult,
    ) {
        mediaProjection =
            mediaProjectionManager.getMediaProjection(result.resultCode, result.data!!)
        createVirtualDisplay()
    }

    private fun stopMediaProjection() {
        virtualDisplay?.apply {
            release()
            virtualDisplay = null
            Log.i(TAG, "Virtual Display released")
        }
        mediaProjection?.apply {
            stop()
            Log.i(TAG, "Media Projection stopped")
        }
    }

    private fun captureScreenshotFromVirtualDisplay(): Bitmap {
        // Workaround to ensure that we don't capture the screen recording prompt
        runBlocking {
            delay(500)
        }
        val image = mImageReader.acquireLatestImage()
        val planes = image.planes
        val buffer = planes.first().buffer

        var bitmap: Bitmap =
            Bitmap.createBitmap(mImageReader.width, mImageReader.height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)

        image.close()
        return bitmap
    }

    private fun createVirtualDisplay() {
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {}, null)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "FullScreenCapture",
            deviceWidth,
            deviceHeight,
            resources.displayMetrics.densityDpi,
            VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, // Mirror the primary display
            mImageReader.surface,
            null,
            null,
        )
    }
}
