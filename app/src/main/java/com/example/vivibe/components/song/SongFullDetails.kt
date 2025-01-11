package com.example.vivibe.components.song

import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
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
            text = song.title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.basicMarquee(
                iterations = Int.MAX_VALUE,
                spacing = MarqueeSpacing(16.dp),
                repeatDelayMillis = 0,
                animationMode = MarqueeAnimationMode.Immediately
            )
        )

        Text(
            text = song.artist.name,
            color = Color.LightGray,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

