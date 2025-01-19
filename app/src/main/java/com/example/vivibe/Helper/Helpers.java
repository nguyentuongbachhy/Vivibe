package com.example.vivibe.Helper;

import android.annotation.SuppressLint;


import com.example.vivibe.Helper.HMac.HMacUtil;

import org.jetbrains.annotations.NotNull;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Helpers {
    private static int transIdDefault = 1;

    @NotNull
    @SuppressLint("DefaultLocale")
     public static String getAppTransId() {
        if (transIdDefault >= 100000) {
            transIdDefault = 1;
        }

        transIdDefault += 1;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatDateTime = new SimpleDateFormat("yyMMdd_hhmmss");
        String timeString = formatDateTime.format(new Date());
        return String.format("%s%06d", timeString, transIdDefault);
    }

    @NotNull
    public static String getMac(String key, String data) throws Exception {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] macBytes = sha256_HMAC.doFinal(data.getBytes("UTF-8"));

            // Use String.format for consistent hex formatting
            StringBuilder hexString = new StringBuilder();
            for (byte b : macBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new Exception("MAC Generation failed: " + e.getMessage(), e);
        }
    }
}
