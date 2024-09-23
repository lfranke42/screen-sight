package de.lflab.screensight.support

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import de.lflab.screensight.databinding.ActivityFloatingServiceSupportBinding
import java.io.File
import java.io.FileOutputStream


class FloatingServiceSupportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFloatingServiceSupportBinding

    private val mImageReader: ImageReader by lazy {
        ImageReader.newInstance(
            width,
            height,
            PixelFormat.RGBA_8888,
            5
        )
    }

    private val width = resources.displayMetrics.widthPixels
    private val height = resources.displayMetrics.heightPixels

    private var virtualDisplay: VirtualDisplay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFloatingServiceSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                Log.e("SupportActivity", "Failed initiate media projection session!")
            }
        }

        startMediaProjection.launch(mediaProjectionManager.createScreenCaptureIntent())
    }

    private fun handleMediaProjectionActivityResult(
        mediaProjectionManager: MediaProjectionManager,
        result: ActivityResult
    ) {
        val mediaProjection = mediaProjectionManager.getMediaProjection(result.resultCode, result.data!!)
        createVirtualDisplay(mediaProjection)
        val screenshot = captureScreenshotFromVirtualDisplay()
        stopMediaProjection(mediaProjection)

        val destination = File(filesDir, "screenshot.png")

        try {
            val out = FileOutputStream(destination)
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopMediaProjection(mediaProjection: MediaProjection) {
        virtualDisplay?.apply {
            release()
            virtualDisplay = null
            Log.i("SupportActivity", "Virtual Display released")
        }
        mediaProjection.apply {
            stop()
            Log.i("SupportActivity", "Media Projection stopped")
        }
    }

    private fun captureScreenshotFromVirtualDisplay(): Bitmap {
        val image = mImageReader.acquireLatestImage()
        val planes = image.planes
        val buffer = planes.first().buffer

        var bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)

        image.close()
        return bitmap
    }

    private fun createVirtualDisplay(mediaProjection: MediaProjection) {
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "FullScreenCapture",
            width,
            height,
            resources.displayMetrics.densityDpi,
            VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, // Mirror the primary display
            mImageReader.surface,
            null,
            null,
        )
    }
}

class MediaProjectionException : Exception()