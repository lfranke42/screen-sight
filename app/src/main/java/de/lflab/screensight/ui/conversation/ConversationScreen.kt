package de.lflab.screensight.ui.conversation

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConversationScreen(
    response: String,
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Title()
        // Only here for debug purposes
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.width(200.dp)
        )
        GenerativeAiResponse(response)
    }
}

@Composable
fun GenerativeAiResponse(response: String) {
    Text(text = response, style = TextStyle(fontSize = 18.sp))
}

@Composable
private fun Title(modifier: Modifier = Modifier) {
    Text(text = "Screen Sight", style = TextStyle(fontSize = 26.sp), modifier = modifier)
}