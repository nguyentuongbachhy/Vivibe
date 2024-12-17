package com.example.vivibe.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class Search {
    @Composable
    fun SearchScreen() {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF101010)),
        ) {
            Text(
                text = "Search",
                color = Color.White
            )
        }
    }
}