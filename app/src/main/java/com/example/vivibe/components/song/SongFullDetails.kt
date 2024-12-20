package com.example.vivibe.components.song

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vivibe.model.PlaySong

@Composable
fun SongFullDetails(
    song: PlaySong,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(10.dp)
    ) {
        Text(
            text = song.title.replaceFirstChar { it.uppercaseChar() },
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = song.artist.name,
            color = Color.LightGray,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
        )
    }
}