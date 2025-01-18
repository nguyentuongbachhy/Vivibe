package com.example.vivibe.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController

import com.example.vivibe.R

class Upgrade {

    @Composable
    fun UpgradeScreen(navController: NavController) {

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Background image with opacity
            Image(
                painter = painterResource(id = R.drawable.background_premium),
                contentDescription = "Premium Background",
                modifier = Modifier
                    .fillMaxSize() // Make the image cover the entire screen
                    .graphicsLayer(alpha = 0.7f), // Apply 70% opacity
                contentScale = androidx.compose.ui.layout.ContentScale.Crop // Ensure the image covers the full screen, maintaining aspect ratio
            )

            // Content box with dark overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80101010)), // Dark overlay
                contentAlignment = Alignment.Center // Center content
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center, // Center content vertically
                    modifier = Modifier.padding(30.dp) // Add padding to entire column
                ) {
                    Text(
                        text = "Buy Music Premium to listen to music without ads, without internet and even with the screen off",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold, // Bold text
                        modifier = Modifier
                            .padding(bottom = 16.dp) // Space below the text
                            .align(Alignment.CenterHorizontally) // Center the text horizontally
                    )

                    Text(
                        text = "Music premium với 85.000đ/ tháng - hủy bất cứ lúc nào",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .padding(bottom = 32.dp) // Space below this text
                            .align(Alignment.CenterHorizontally) // Center the text horizontally
                    )

                    Button(
                        onClick = { navController.navigate("payment") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue), // Set button color
                        modifier = Modifier.padding(16.dp) // Add padding to the button
                    ) {
                        Text(
                            text = "Upgrade Premium",
                            color = Color.White,
                            fontSize = 18.sp,
                        )
                    }
                    Text(
                        text = "Thanh toán định kỳ. Bằng việc tiếp tục, bạn xác nhận rằng bạn đã đủ 18 tuổi và đồng ý với những điều khoản. Chúng tôi sẽ không hoàn tiền cho chu trình thanh toán không trọn vẹn",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(bottom = 32.dp) // Space below this text
                            .align(Alignment.CenterHorizontally) // Center the text horizontally
                    )
                }
            }
        }
    }
}
