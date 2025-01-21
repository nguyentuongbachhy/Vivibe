package com.example.vivibe.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.vivibe.R

class Upgrade {
    @Composable
    fun UpgradeScreen(navController: NavController) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image with opacity
            Image(
                painter = painterResource(id = R.drawable.background_premium),
                contentDescription = "Premium Background",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.9f),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            // Content box with dark overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80101010))
                    .padding(top = 70.dp, bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo_foreground),
                        contentDescription = "Logo",
                        modifier = Modifier.size(100.dp)
                    )

                    // Main description
                    Text(
                        text = "Buy Music Premium to listen to music without ads, offline, and even with the screen off.",
                        color = Color.White,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Subscription details
                    Text(
                        text = "Music Premium for 85,000 VND/month - cancel anytime",
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Quick upgrade button
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .height(64.dp)
                            .width(320.dp)
                            .clickable { navController.navigate("payment?months=1") }
                            .background(Color(0xFF7FFFD4), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Try Premium for 1 month",
                            color = Color.Black,
                            fontSize = 18.sp,
                        )
                    }

                    // Terms and conditions
                    Text(
                        text = "Subscription payment. By continuing, you confirm that you are at least 18 years old and agree to the terms and conditions. We will not refund for incomplete payment cycles.",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Features heading
                    Text(
                        text = "Thousands of songs, live performances, and more, all at your fingertips.",
                        color = Color.White,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 100.dp)
                    )

                    // Premium features
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.padding(30.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_premium_outline),
                            contentDescription = "Ad-free music",
                            modifier = Modifier.size(100.dp),
                        )
                        Text(
                            text = "Listen to your favorite songs and artists without ads",
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.padding(30.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_premium_filled),
                            contentDescription = "Offline listening",
                            modifier = Modifier.size(100.dp),
                        )
                        Text(
                            text = "Download to listen offline, no internet needed, or play in the background for an uninterrupted experience",
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }

                    // Plans section
                    Text(
                        text = "Choose the plan that suits you",
                        color = Color.White,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 30.dp)
                    )

                    // Subscription plans
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Monthly plan
                        Box(
                            modifier = Modifier
                                .height(220.dp)
                                .fillMaxWidth()
                                .background(Color.DarkGray, shape = RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "1 Month",
                                    color = Color.White,
                                    style = MaterialTheme.typography.h6
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Monthly plan\n85,000 VND/ 1 month",
                                    color = Color.White,
                                    style = MaterialTheme.typography.body1
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { navController.navigate("payment?months=1") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7FFFD4)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "Use Premium for One Month", color = Color.Black)
                                }
                            }
                        }

                        // Quarterly plan
                        Box(
                            modifier = Modifier
                                .height(220.dp)
                                .fillMaxWidth()
                                .background(Color.DarkGray, shape = RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "3 Months",
                                    color = Color.White,
                                    style = MaterialTheme.typography.h6
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Quarterly plan\n240,000 VND/ 3 months",
                                    color = Color.White,
                                    style = MaterialTheme.typography.body1
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { navController.navigate("payment?months=3") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "Use Premium for Three Months", color = Color(0xFF7FFFD4))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}