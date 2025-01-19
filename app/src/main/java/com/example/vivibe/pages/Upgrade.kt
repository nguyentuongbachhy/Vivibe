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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
                    .fillMaxSize() // Make the image cover the entire screen
                    .graphicsLayer(alpha = 0.9f), // Apply 70% opacity
                contentScale = androidx.compose.ui.layout.ContentScale.Crop // Ensure the image covers the full screen, maintaining aspect ratio
            )

            // Content box with dark overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80101010))
                    .padding(top = 70.dp, bottom = 100.dp), // Dark overlay
                contentAlignment = Alignment.Center // Center content
            ) {
                // Use verticalScroll to enable scrolling
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp) // Padding around the column
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
                    verticalArrangement = Arrangement.Center // Center content vertically
                ) {
                    // Logo and text
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo_foreground),
                        contentDescription = "Logo",
                        modifier = Modifier.size(100.dp) // Set logo size
                    )


                    // Description in English
                    Text(
                        text = "Buy Music Premium to listen to music without ads, offline, and even with the screen off.",
                        color = Color.White,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold, // Bold text
                        modifier = Modifier.padding(bottom = 16.dp) // Add space below
                    )

                    // Subscription details
                    Text(
                        text = "Music Premium for 85,000 VND/month - cancel anytime",
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp) // Add space below
                    )

                    // Upgrade button
                    Button(
                        onClick = { navController.navigate("payment?months=1") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7FFFD4)),
                        modifier = Modifier.padding(20.dp)
                            .height(50.dp)
                            .width(320.dp)
                    ) {
                        Text(
                            text = " Try Premium for 1 months",
                            color = Color.Black,
                            fontSize = 18.sp,
                        )
                    }

                    // Terms and conditions
                    Text(
                        text = "Subscription payment. By continuing, you confirm that you are at least 18 years old and agree to the terms and conditions. We will not refund for incomplete payment cycles.",
                        color = Color.Black,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 0.dp) // Add padding for readability
                    )

                    Text(
                        text = "Thousands of songs, live performances, and more, all at your fingertips.",
                        color = Color.White,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold, // Bold text
                        modifier = Modifier.padding(top = 100.dp) // Add space below
                    )

                    // Premium features
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.padding(30.dp) // Add spacing below
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_premium1),
                            contentDescription = "Logo",
                            modifier = Modifier.size(100.dp),
                        )
                        Text(
                            text = "Listen to your favorite songs and artists without ads",
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 10.dp) // Add space between logo and text
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.padding(30.dp) // Add spacing below
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_premium2),
                            contentDescription = "Logo",
                            modifier = Modifier.size(100.dp),
                        )
                        Text(
                            text = "Download to listen offline, no internet needed, \nor play in the background for an uninterrupted experience",
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 10.dp) // Add space between logo and text
                        )
                    }

                    Text(
                        text = "Choose the plan that suits you",
                        color = Color.White,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold, // Bold text
                        modifier = Modifier.padding(bottom = 30.dp) // Add space below
                    )

                    // Subscription plans
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp), // Add space between the plans
                        modifier = Modifier.padding(16.dp) // Add padding for the column
                    ) {
                        Box(
                            modifier = Modifier
                                .height(220.dp)
                                .width(360.dp)
                                .background(Color.DarkGray, shape = RoundedCornerShape(16.dp)), // Dark overlay
                            contentAlignment = Alignment.Center // Center content
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp) // Add padding to the content
                            ) {
                                Text(
                                    text = "1 Month",
                                    color = Color.White,
                                    style = MaterialTheme.typography.h6
                                )
                                Spacer(modifier = Modifier.height(8.dp)) // Add space between title and content
                                Text(
                                    text = "Monthly plan\n85,000 VND/ 1 month",
                                    color = Color.White,
                                    style = MaterialTheme.typography.body1
                                )
                                Spacer(modifier = Modifier.height(16.dp)) // Add space before the button
                                Button(
                                    onClick = { navController.navigate("payment?months=1") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7FFFD4)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "Use Premium for One Month", color = Color.Black)
                                }
                            }
                        }
                        Box(
                            modifier = Modifier

                                .background(Color.DarkGray, shape = RoundedCornerShape(16.dp))
                                , // Dark overlay
                            contentAlignment = Alignment.Center // Center content
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            // Add padding to the content
                            ) {
                                Text(
                                    text = "3 Months",
                                    color = Color.White,
                                    style = MaterialTheme.typography.h6
                                )
                                Spacer(modifier = Modifier.height(8.dp)) // Add space between title and content
                                Text(
                                    text = "Quarterly plan\n240,000 VND/ 3 months",
                                    color = Color.White,
                                    style = MaterialTheme.typography.body1
                                )
                                Spacer(modifier = Modifier.height(16.dp)) // Add space before the button
                                Button(
                                    onClick = { navController.navigate("payment?months=3")},
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black), // Black background
                                    modifier = Modifier.fillMaxWidth()

                                ) {
                                    Text(text = "Use Premium for Three Months", color = Color(0xFF7FFFD4)) // Aquamarine text
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}