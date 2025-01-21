package com.example.vivibe.api

import com.example.vivibe.constant.AppInfo
import com.example.vivibe.helper.Helpers
import com.example.vivibe.api.HttpProvider.sendPost
import okhttp3.FormBody
import org.json.JSONObject
import java.util.Date

class CreateOrder {
    private data class CreateOrderData(
        val appId: String = AppInfo.APP_ID.toString(),
        val appUser: String = "Android_Demo",
        val appTime: String = Date().time.toString(),
        val amount: String,
        val appTransId: String = Helpers.getAppTransId(),
        val embedData: String = "{}",
        val items: String = "[]",
        val bankCode: String = "zalopayapp",
        val description: String = "Merchant pay for order #${Helpers.getAppTransId()}",
    ) {
        val mac: String = calculateMac()

        private fun calculateMac(): String {
            val inputHMac = buildString {
                append(appId)
                append("|")
                append(appTransId)
                append("|")
                append(appUser)
                append("|")
                append(amount)
                append("|")
                append(appTime)
                append("|")
                append(embedData)
                append("|")
                append(items)
            }
            return Helpers.getMac(AppInfo.MAC_KEY, inputHMac)
        }
    }

    @Throws(Exception::class)
    fun createOrder(amount: String): JSONObject? {
        val orderData = CreateOrderData(amount = amount)

        val formBody = FormBody.Builder().apply {
            add("appid", orderData.appId)
            add("appuser", orderData.appUser)
            add("apptime", orderData.appTime)
            add("amount", orderData.amount)
            add("apptransid", orderData.appTransId)
            add("embeddata", orderData.embedData)
            add("item", orderData.items)
            add("bankcode", orderData.bankCode)
            add("description", orderData.description)
            add("mac", orderData.mac)
        }.build()

        return sendPost(AppInfo.URL_CREATE_ORDER, formBody)
    }
}