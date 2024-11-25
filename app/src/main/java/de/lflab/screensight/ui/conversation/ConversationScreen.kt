package de.lflab.screensight.ui.conversation

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.lflab.screensight.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    response: String,
    bitmap: Bitmap,
    onClose: () -> Unit,
) {
    Scaffold(Modifier.fillMaxSize(), topBar = {
        CenterAlignedTopAppBar(
            title = { Title() },
            navigationIcon = { CloseButton(onClick = onClose) }
        )
    }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                // Only here for debug purposes
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.width(200.dp)
                )
            }
            item {
                GenerativeAiResponse(response)
            }
        }
    }
}

@Composable
private fun Title(modifier: Modifier = Modifier) {
    Text(text = "Screen Sight", style = TextStyle(fontSize = 26.sp), modifier = modifier)
}

@Composable
private fun GenerativeAiResponse(response: String) {
    Text(text = response, style = TextStyle(fontSize = 18.sp))
}

@Composable
fun CloseButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(painter = painterResource(R.drawable.icon_close_24px), contentDescription = null)
    }
}

