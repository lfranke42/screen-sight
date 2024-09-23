package de.lflab.screensight.network

import android.graphics.Bitmap

interface GenerativeAiRepository {
    suspend fun generateContent(image: Bitmap, prompt: String): String?
}