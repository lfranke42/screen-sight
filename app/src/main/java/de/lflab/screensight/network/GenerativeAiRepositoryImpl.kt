package de.lflab.screensight.network

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GoogleGenerativeAIException
import com.google.ai.client.generativeai.type.content
import de.lflab.screensight.BuildConfig

private const val TAG = "GenerativeAiRepositoryImpl"

class GenerativeAiRepositoryImpl : GenerativeAiRepository {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    override suspend fun generateContent(image: Bitmap, prompt: String): String? {
        try {
            val response = generativeModel.generateContent(
                content {
                    image(image)
                    text(prompt)
                }
            )
            return response.text
        } catch (e: GoogleGenerativeAIException) {
            Log.e(TAG, "Failed to get generate AI response")
            e.printStackTrace()
            return null
        }
    }
}