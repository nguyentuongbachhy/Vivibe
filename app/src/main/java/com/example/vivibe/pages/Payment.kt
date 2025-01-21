package com.example.vivibe.pages

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.example.vivibe.MainActivity
import com.example.vivibe.api.CreateOrder
import com.example.vivibe.api.user.UserClient
import com.example.vivibe.manager.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.userAgent
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener

class Payment : ComponentActivity() {


    @Composable
    fun PaymentScreen(navController: NavController) {
        ZaloPaySDK.init(2554, Environment.SANDBOX)
        Log.d("Payment", "ZaloPay SDK initialized")

        val months = navController.currentBackStackEntry
            ?.arguments
            ?.getString("months")?.toIntOrNull() ?: 1
        LaunchedEffect(Unit) {
            ZaloPaySDK.init(
                2553,
                Environment.SANDBOX
            ) // App ID và môi trường (Sandbox hoặc Production)
            Log.d("Payment", "ZaloPay SDK initialized")
        }
        val context = LocalContext.current
        val selectedPaymentMethod = remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80101010))
                    .padding(0.dp, 100.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Buy Music Premium",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Enjoy ad-free music for 85.000đ/months.",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Card(
                        modifier = Modifier.width(300.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Choose Payment Method",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            PaymentOption(
                                label = "ZaloPay",
                                isSelected = selectedPaymentMethod.value == "ZaloPay",
                                onSelect = { selectedPaymentMethod.value = "ZaloPay" }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                initializePayment(
                                    context,
                                    selectedPaymentMethod.value,
                                    months,
                                    userClient = UserClient(
                                        context,
                                        UserManager.getInstance(context).getToken().orEmpty()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7FFFD4)),
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Text(
                            text = "Confirm Payment",
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = "By continuing, you agree to our terms.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun PaymentOption(label: String, isSelected: Boolean, onSelect: () -> Unit) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = if (label == "Momo") Color.Red else Color.Green)
            )
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }

    fun initializePayment(
        context: Context,
        paymentMethod: String,
        months: Int,
        userClient: UserClient
    ) {
        // Sử dụng CoroutineScope để bọc việc gọi processPayment
        CoroutineScope(Dispatchers.Main).launch {
            processPayment(context, paymentMethod, months, userClient)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun processPayment(
        context: Context,
        paymentMethod: String,
        months: Int,
        userClient: UserClient
    ) {
        if (paymentMethod.isEmpty()) {
            Log.d("Payment", "Please select a payment method")
            return
        }
        val orderApi = CreateOrder()
        try {
            val total = when (months) {
                1 -> 85000 // Giá 1 tháng
                3 -> 240000 // Giá 3 tháng
                else -> 0 // Mặc định, không hợp lệ
            }
            val data = withContext(Dispatchers.IO) {
                orderApi.createOrder(java.lang.String.valueOf(total))
            }

            if (data == null) {
                Log.d("Payment", "Order creation failed!")
                Log.d("Payment", "Response is null")
                return
            }

            Log.d("Payment", "Order creation response: $data")

            // Changed from return_code to returncode to match API response
            val returnCode = data.optInt("returncode", Int.MIN_VALUE)
            Log.d("Payment", "Return code: $returnCode")

            when (returnCode) {
                1 -> {
                    val token = data.optString(
                        "zptranstoken",
                        ""
                    )  // Changed from zp_trans_token to match API
                    if (token.isEmpty()) {
                        Log.d("Payment", "Invalid response - missing token")
                        Log.d("Payment", "Payment initialization failed - missing token")
                        return
                    }

                    Log.d("Payment", "Got token: $token")

                    val activity = context as? Activity
                    if (activity == null) {
                        Log.d("Payment", "Invalid context")
                        return
                    }

                    withContext(Dispatchers.Main) {
                        val activity = context as? Activity
                        if (activity != null) {
                            ZaloPaySDK.getInstance().payOrder(
                                activity,
                                token,  // Đây là giá trị "zptranstoken" bạn nhận được từ API
                                "demozpdk://app",  // Callback URL (scheme đã khai báo trong manifest)
                                object : PayOrderListener {
                                    override fun onPaymentSucceeded(
                                        transactionId: String,
                                        zpTransToken: String,
                                        extraInfo: String
                                    ) {
                                        lifecycleScope.launch(Dispatchers.Main) {
                                            try {
                                                val userId = UserManager.getInstance(context).getId()
                                                Log.d("Payment", "User ID: $userId")

                                                if (userId.isNullOrEmpty()) {
                                                    Log.e("Payment", "User ID is null or empty")
                                                    return@launch
                                                }

                                                val upgraded = userClient.upgradeToPremium(userId)
                                                Log.d("Payment", "Upgrade result: $upgraded")

                                                if (upgraded == 1) {
                                                    UserManager.getInstance(context).updatePremium(1)
                                                    (context as MainActivity).reloadActivity()
                                                    Toast.makeText(context, "Upgrade to Premium successful!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Log.e("Payment", "Upgrade failed")
                                                    Toast.makeText(context, "Upgrade to Premium failed.", Toast.LENGTH_SHORT).show()
                                                }

                                                Log.d("Payment", "Payment succeeded!")
                                                Log.d("Payment", "Transaction ID: $transactionId, ZP Trans Token: $zpTransToken")
                                            } catch (e: Exception) {
                                                Log.e("Payment", "Error during upgrade: ${e.message}")
                                                Toast.makeText(context, "An error occurred during upgrade.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }

                                    override fun onPaymentCanceled(
                                        zpTransToken: String,
                                        appTransID: String
                                    ) {
                                        Log.d("Payment", "Payment was canceled")
                                        Log.d(
                                            "Payment",
                                            "ZP Trans Token: $zpTransToken, App Trans ID: $appTransID"
                                        )
                                    }

                                    override fun onPaymentError(
                                        error: ZaloPayError?,
                                        message: String?,
                                        details: String?
                                    ) {
                                        Log.d("Payment", "Payment failed: $message")
                                        Log.d("Payment", "Error Details: $details")
                                    }
                                }
                            )
                        } else {
                            Log.d("Payment", "Invalid context")
                        }
                    }


                }

                -2 -> {
                    Log.d("Payment", "Order creation failed: App ID or MAC authentication failed")
                }

                -39 -> {
                    Log.d("Payment", "Order creation failed: Invalid amount")
                }

                -146 -> {
                    Log.d("Payment", "Order creation failed: Duplicate transaction ID")
                }

                else -> {
                    val returnMessage = data.optString("returnmessage", "Unknown error")
                    Log.d("Payment", "Order creation failed: $returnMessage")
                }
            }
        } catch (e: Exception) {
            Log.d("Payment", "Payment error: ${e.message}")
        }
    }
}