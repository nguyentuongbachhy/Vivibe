package com.example.vivibe.pages

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.vivibe.MainActivity

import com.example.vivibe.R
import com.example.vivibe.manager.UserManager
import com.example.vivibe.router.HomeRouter
import com.example.vivibe.router.LibraryRouter
import com.example.vivibe.router.UpgradeRouter
import kotlinx.coroutines.launch

class MyAccount(private val context: Context) {
    @Composable
    fun MyAccountScreen(
        navController: NavController,
        userManager: UserManager,
        onLogOut: () -> Unit
    ) {
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101010))  // Màu nền tối hơn một chút
                .padding(horizontal = 20.dp, vertical = 24.dp)  // Padding lớn hơn
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)  // Thêm padding bottom
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF202020))  // Màu nền cho button
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Account",
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )

                // Spacer cân đối với IconButton
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Navigation list với spacing tốt hơn
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),  // Tăng spacing
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationItem(
                    iconId = R.drawable.ic_download,
                    text = "Downloaded Music",
                    onClick = { navController.navigate(LibraryRouter.route) }
                )

                if (userManager.isPremium() == 0) {
                    NavigationItem(
                        iconId = R.drawable.ic_premium_filled,
                        text = "Upgrade Premium",
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2196F3))  // Highlight Premium button
                            .padding(vertical = 4.dp),
                        onClick = { navController.navigate(UpgradeRouter.route) }
                    )
                }

                NavigationItem(
                    iconId = R.drawable.ic_question,
                    text = "Help & Feedback",
                    onClick = { navController.navigate(HomeRouter.route) }
                )

                NavigationItem(
                    iconId = R.drawable.ic_logout,
                    text = "Log Out",
                    textColor = Color(0xFFFF5252),  // Màu đỏ cho logout
                    iconTint = Color(0xFFFF5252),
                    onClick = {
                        scope.launch {
                            onLogOut()
                            (context as? MainActivity)?.reloadActivity()
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun NavigationItem(
        iconId: Int,
        text: String,
        modifier: Modifier = Modifier,
        textColor: Color = Color.White,
        iconTint: Color = Color.White,
        onClick: () -> Unit
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 16.dp)  // Padding lớn hơn
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = text,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.body1.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = textColor
            )
        }
    }
}