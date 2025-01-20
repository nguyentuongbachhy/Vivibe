//package com.example.vivibe.pages
//
//import android.app.Activity
//import android.content.Context
//import android.os.Bundle
//import android.os.StrictMode
//import android.os.StrictMode.ThreadPolicy
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.media3.common.MediaItem
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.ui.PlayerView
//import com.example.vivibe.api.CreateOrder
//import vn.zalopay.sdk.Environment
//import vn.zalopay.sdk.ZaloPayError
//import vn.zalopay.sdk.ZaloPaySDK
//import vn.zalopay.sdk.listeners.PayOrderListener
//
//class Payment : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Allow network operations on main thread (not recommended for production)
//        val policy = ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)
//
//        // Initialize ZaloPay SDK
//        ZaloPaySDK.init(553, Environment.SANDBOX) // Use your App ID
//
//        enableEdgeToEdge()
//        setContent {
//            PaymentScreen()
//        }
//    }
//}
//@Preview
//@Composable
//fun PaymentScreen() {
//    val context = LocalContext.current // Get current context
//    val selectedPaymentMethod = remember { mutableStateOf("") } // Track selected method
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        // Video background
//
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color(0x80101010)) // Semi-transparent overlay
//                .padding(0.dp, 100.dp),
//            contentAlignment = Alignment.TopCenter
//        ) {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                modifier = Modifier.padding(24.dp)
//            ) {
//                Text(
//                    text = "Buy Music Premium",
//                    color = Color.White,
//                    fontSize = 22.sp,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
//
//                Text(
//                    text = "Enjoy ad-free music for $14.98/month.",
//                    color = Color.White,
//                    fontSize = 16.sp,
//                    modifier = Modifier.padding(bottom = 24.dp)
//                )
//
//                // Payment options card
//                Card(
//                    modifier = Modifier.width(300.dp),
//                    shape = RoundedCornerShape(12.dp)
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text(
//                            text = "Choose Payment Method",
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 18.sp,
//                            color = Color.Black,
//                            modifier = Modifier.padding(bottom = 16.dp)
//                        )
//
//                        PaymentOption(
//                            label = "Momo",
//                            isSelected = selectedPaymentMethod.value == "Momo",
//                            onSelect = { selectedPaymentMethod.value = "Momo" }
//                        )
//                        PaymentOption(
//                            label = "ZaloPay",
//                            isSelected = selectedPaymentMethod.value == "ZaloPay",
//                            onSelect = { selectedPaymentMethod.value = "ZaloPay" }
//                        )
//                    }
//                }
//
//                // Confirm Payment Button
//                Button(
//                    onClick = {
//                        handlePayment(context, selectedPaymentMethod.value)
//                    },
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
//                    modifier = Modifier.padding(top = 24.dp)
//                ) {
//                    Text(
//                        text = "Confirm Payment",
//                        color = Color.White,
//                        fontSize = 16.sp
//                    )
//                }
//
//                Text(
//                    text = "By continuing, you agree to our terms.",
//                    color = Color.LightGray,
//                    fontSize = 12.sp,
//                    modifier = Modifier.padding(top = 16.dp)
//                )
//            }
//        }
//    }
//}
//
//
//@Composable
//fun PaymentOption(label: String, isSelected: Boolean, onSelect: () -> Unit) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier.padding(vertical = 8.dp)
//    ) {
//        RadioButton(
//            selected = isSelected,
//            onClick = onSelect,
//            colors = RadioButtonDefaults.colors(selectedColor = if (label == "Momo") Color.Red else Color.Green)
//        )
//        Text(
//            text = label,
//            fontSize = 16.sp,
//            color = Color.Black,
//            modifier = Modifier.padding(start = 8.dp)
//        )
//    }
//}
//
//fun handlePayment(context: android.content.Context, paymentMethod: String) {
//    if (paymentMethod.isEmpty()) {
//        Toast.makeText(context, "Please select a payment method", Toast.LENGTH_SHORT).show()
//        return
//    }
//
//    val total = 59000 // Amount in smallest unit
//    val orderApi = CreateOrder()
//
//    try {
//        val data = orderApi.createOrder(total.toString())
//        Log.d("Payment", "Order Data: $data")
//        val returnCode = data.getString("return_code")
//        if (returnCode == "1") {
//            val token = data.getString("zp_trans_token")
//            // Cast Context to Activity
//            val activity = context as? Activity
//            activity?.let {
//                ZaloPaySDK.getInstance().payOrder(
//                    it,  // Pass the Activity instance
//                    token,
//                    "demozpdk://app",
//                    object : PayOrderListener {
//                        // Payment success handler
//                        override fun onPaymentSucceeded(transactionId: String, zpTransToken: String, extraInfo: String) {
//                            Log.d("Payment", "Success: $transactionId, Token: $zpTransToken, Info: $extraInfo")
//                            Toast.makeText(context, "Payment succeeded!", Toast.LENGTH_SHORT).show()
//                        }
//
//                        // Implement the missing onPaymentCanceled method
//                        override fun onPaymentCanceled(zpTransToken: String, appTransID: String) {
//                            Log.d("Payment", "Canceled: Token: $zpTransToken, AppTransID: $appTransID")
//                            Toast.makeText(context, "Payment was canceled", Toast.LENGTH_SHORT).show()
//                        }
//
//                        override fun onPaymentError(p0: ZaloPayError?, p1: String?, p2: String?) {
//                            TODO("Not yet implemented")
//                        }
//                    }
//                )
//            }
//        } else {
//            Toast.makeText(context, "Payment error: Invalid return code.", Toast.LENGTH_SHORT).show()
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//        Toast.makeText(context, "An error occurred.", Toast.LENGTH_SHORT).show()
//    }
//}
