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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

import com.example.vivibe.R
import com.example.vivibe.model.User

class MyAccount {
    @Composable
    fun MyAccountScreen(
        navController: NavController, // Thêm tham số NavController

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
        ) {
            // Header with close button and "Account" text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { navController.popBackStack() } // Quay lại màn hình trước
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(0.1f)) // Spacer pushes the title to the center
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.h6,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
//                Image(
//                    painter = rememberAsyncImagePainter(model = user.profilePictureUri),
//                    contentDescription = "User Profile",
//                    modifier = Modifier
//                        .size(64.dp)
//                        .clip(CircleShape)
//                )
//                Spacer(modifier = Modifier.width(16.dp))
//                Text(
//                    text = user.name, // Hiển thị tên người dùng
//                    style = MaterialTheme.typography.h6,
//                    color = Color.White
//                )
            }

            // Navigation list
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Download Content
                NavigationItem(
                    iconId = R.drawable.ic_download, // Replace with your icon resource
                    text = "Downloaded Music",
                    onClick = { navController.navigate("download") } // Điều hướng tới màn hình download
                )

                // Premium
                NavigationItem(
                    iconId = R.drawable.ic_premium_filled, // Replace with your icon resource
                    text = "Upgrade Premium",
                    onClick = { navController.navigate("upgrade") } // Điều hướng tới màn hình upgrade
                )

                // Help and Feedback
                NavigationItem(
                    iconId = R.drawable.ic_question, // Replace with your icon resource
                    text = "Help & Feedback",
                    onClick = { navController.navigate("help") } // Điều hướng tới màn hình help
                )

                // Logout
                NavigationItem(
                    iconId = R.drawable.ic_logout, // Replace with your icon resource
                    text = "Log Out",
                    onClick = { } // Gọi callback logout
                )
            }
        }
    }

    @Composable
    fun NavigationItem(
        iconId: Int,
        text: String,
        onClick: () -> Unit
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable(onClick = onClick)
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.body1,
                color = Color.White
            )
        }
    }
}