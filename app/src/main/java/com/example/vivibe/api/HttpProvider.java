package com.example.vivibe.api;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.TlsVersion;

public class HttpProvider {
    public static JSONObject sendPost(String URL, RequestBody formBody) {
        try {
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .cipherSuites(
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectionSpecs(Collections.singletonList(spec))
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(URL)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(formBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    Log.e("HttpProvider", "Response body is null");
                    return new JSONObject().put("return_code", "-1")
                            .put("return_message", "Empty response from server");
                }

                String responseString = responseBody.string();
                Log.d("HttpProvider", "Response: " + responseString);

                if (!response.isSuccessful()) {
                    Log.e("HttpProvider", "Request failed with code: " + response.code());
                    return new JSONObject()
                            .put("return_code", "-1")
                            .put("return_message", "HTTP " + response.code() + ": " + responseString);
                }

                return new JSONObject(responseString);
            }

        } catch (IOException e) {
            Log.e("HttpProvider", "IO Exception: " + e.getMessage());
            e.printStackTrace();
            try {
                return new JSONObject()
                        .put("return_code", "-1")
                        .put("return_message", "Network error: " + e.getMessage());
            } catch (JSONException je) {
                Log.e("HttpProvider", "JSON Exception while handling IO Exception", je);
            }
        } catch (JSONException e) {
            Log.e("HttpProvider", "JSON Exception: " + e.getMessage());
            e.printStackTrace();
            try {
                return new JSONObject()
                        .put("return_code", "-1")
                        .put("return_message", "Invalid JSON response: " + e.getMessage());
            } catch (JSONException je) {
                Log.e("HttpProvider", "JSON Exception while handling JSON Exception", je);
            }
        }

        // Fallback error response
        try {
            return new JSONObject()
                    .put("return_code", "-1")
                    .put("return_message", "Unknown error occurred");
        } catch (JSONException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
    }
}