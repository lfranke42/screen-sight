package de.lflab.screensight.ui.conversation

import android.graphics.BitmapFactory
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.lflab.screensight.ScreenSightAccessibilityService
import de.lflab.screensight.ui.theme.ScreenSightTheme
import java.io.FileNotFoundException

class ConversationActivity : ComponentActivity() {
    private lateinit var tts: TextToSpeech
    private var isTtsAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val response = intent.getStringExtra(ScreenSightAccessibilityService.AI_RESPONSE_KEY)
            ?: "Something went wrong, try again later!"

        tts = TextToSpeech(this, object : TextToSpeech.OnInitListener {
            override fun onInit(status: Int) {
                if (status == TextToSpeech.SUCCESS) {
                    convertTextToSpeech(response)
                } else {
                    isTtsAvailable = false
                }
            }
        })

        try {
            val inputStream = openFileInput("screenshot.png")
            val bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close()
            enableEdgeToEdge()
            setContent {
                ScreenSightTheme {
                    ConversationScreen(
                        response = response,
                        bitmap = bitmap,
                        onClose = ::onClose,
                    )
                }
            }
        } catch (_: FileNotFoundException) {
            // Don't do anything for now
        }
    }

    private fun onClose() {
        finish()
    }

    private fun convertTextToSpeech(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onPause() {
        super.onPause()
        onClose()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}


