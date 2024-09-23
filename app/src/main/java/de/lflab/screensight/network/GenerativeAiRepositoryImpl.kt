package de.lflab.screensight.network

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GoogleGenerativeAIException
import com.google.ai.client.generativeai.type.content
import de.lflab.screensight.BuildConfig

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
            return null
        }
    }
}