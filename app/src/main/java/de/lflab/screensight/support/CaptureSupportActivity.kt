package de.lflab.screensight.support

import android.content.Context
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
import de.lflab.screensight.databinding.ActivityCaptureSupportBinding
import de.lflab.screensight.util.screenHeight
import de.lflab.screensight.util.screenWidth
import java.io.File
import java.io.FileOutputStream

private const val TAG = "CaptureSupportActivity"

class CaptureSupportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCaptureSupportBinding

    private lateinit var mImageReader: ImageReader

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCaptureSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mImageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            5
        )
        val onImageAvailableListener = OnImageAvailableListener {
            // TODO: Skip a few frames to not include the screen capture pop-up
            val screenshot = captureScreenshotFromVirtualDisplay()
            stopMediaProjection()

            val destination = File(filesDir, "screenshot.png")

            try {
                val out = FileOutputStream(destination)
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            finish()
        }
        mImageReader.setOnImageAvailableListener(onImageAvailableListener, null)

        startMediaProjection()
    }

    private fun startMediaProjection() {
        val mediaProjectionManager: MediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val startMediaProjection = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                handleMediaProjectionActivityResult(mediaProjectionManager, result)
            } else {
                Log.e(TAG, "Failed initiate media projection session!")
            }
        }

        val startForegroundServiceBroadcast = Intent("action_start_foreground")
        sendBroadcast(startForegroundServiceBroadcast)
        Log.d(TAG, "startForegroundServiceBroadcast sent")

        startMediaProjection.launch(mediaProjectionManager.createScreenCaptureIntent())
    }

    private fun handleMediaProjectionActivityResult(
        mediaProjectionManager: MediaProjectionManager,
        result: ActivityResult
    ) {
        mediaProjection = mediaProjectionManager.getMediaProjection(result.resultCode, result.data!!)
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
        val image = mImageReader.acquireLatestImage()
        val planes = image.planes
        val buffer = planes.first().buffer

        var bitmap: Bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)

        image.close()
        return bitmap
    }

    private fun createVirtualDisplay() {
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {}, null)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "FullScreenCapture",
            screenWidth,
            screenHeight,
            resources.displayMetrics.densityDpi,
            VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, // Mirror the primary display
            mImageReader.surface,
            null,
            null,
        )
    }
}
