package de.lflab.screensight.network

import android.graphics.Bitmap

// TODO: Add different models besides Gemini
interface GenerativeAiRepository {
    suspend fun generateContent(image: Bitmap, prompt: String): String?
}