package com.example.vivibe.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import com.example.vivibe.R

class Explore {
    @Preview
    @Composable
    fun ExploreScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101010))
                .padding(16.dp,76.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Row of three buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SquareButton("Bản phát hành mới", R.drawable.ic_new_release)
                SquareButton("Bảng xếp hạng", R.drawable.ic_rank)
                SquareButton("Tâm trạng và Thể loại", R.drawable.ic_happy)
            }

            // Textblock: "Tâm trạng và thể loại"
            Text(
                text = "Tâm trạng và Thể loại",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .align(Alignment.Start), // Align left
                textAlign = TextAlign.Start // Align text to the left
            )

            // Grid for genres
            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp) // Adjust height for 2 rows
            ) {
                items(listOf("Happy", "Relax", "Sad", "Excited", "Calm", "Romantic", "Chill", "Energetic", "Focused")) { genre ->
                    GenreButton(genre)
                }
            }

            // Textblock: "Nhạc mới"
            Text(
                text = "Nhạc mới",
                color = Color.White,
                fontSize = 16.sp,

                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .align(Alignment.Start),
                textAlign = TextAlign.Center
            )

            LazyHorizontalGrid(
                rows = GridCells.Fixed(1),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp) // Adjust height for 2 rows
            ) {
                items(listOf("Chúng ta của hiện tại","ABC song","Die With A Smile")) { song ->
                    newReleaseButton(song)
                }
            }
        }
    }

    @Composable
    fun SquareButton(text: String, iconResId: Int) {
        Button(
            onClick = { /* TODO: Handle button click */ },
            modifier = Modifier
                .size(100.dp)
                .padding(0.dp), // Remove any padding
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1F1F))
        ) {
            Column(
            ) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 12.sp,
                )
            }
        }
    }

    @Composable
    fun GenreButton(genre: String) {
        Box(
            modifier = Modifier
                .drawBehind {
                    // Draw border for the left side
                    drawLine(
                        color = Color.Yellow, // Border color
                        start = Offset(0f, 0f), // Starting from the top
                        end = Offset(0f, size.height), // End at the bottom
                        strokeWidth = 8f // Border thickness
                    )
                }
                .padding(0.dp) // Remove extra padding
        ) {
            Button(
                onClick = { /* TODO: Handle genre click */ },
                modifier = Modifier
                    .height(35.dp)
                    .width(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C))
            ) {
                Text(
                    text = genre,
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    fun newReleaseButton(songName: String) {
        Box(
            modifier = Modifier
                .padding(0.dp) // Ensure no extra padding
        ) {
            Button(
                onClick = { /* TODO: Handle genre click */ },
                modifier = Modifier
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C))
            ) {
                Text(
                    text = songName,
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
