package com.expedia.bookings.utils

import android.content.Context
import android.util.Base64
import android.util.Log
import okhttp3.HttpUrl
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.Random
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HMACUtil {

    companion object {

        fun getXDate(): String {
            val fmt = DateTimeFormat.forPattern("E, dd MMM yyyy HH:mm:ss zzz")
            val date = fmt.print(DateTime.now(DateTimeZone.UTC))
            return date
        }

        fun generateSalt(length: Int): String {
            val alphaNumericChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val salt = StringBuilder()
            for (i in 0..length) {
                salt.append(alphaNumericChars[Random().nextInt(alphaNumericChars.length)])
            }
            return salt.toString()
        }

        fun getAuthorization(context: Context, url: HttpUrl, method: String, date: String, salt: String): String {
            val pathAndQuery = url.encodedPath() + "?" + url.encodedQuery()
            val requestLine = "$method $pathAndQuery HTTP/1.1"
            val stringToSign = "$requestLine\nx-date: $date\nsalt: $salt"
            val hmac = createHmac(getKey(context), stringToSign)
            val userName = ServicesUtil.getHmacUserName(context)
            val authString = "hmac username=\"$userName\",algorithm=\"hmac-sha1\",headers=\"request-line x-date salt\",signature=\"$hmac\""
            return authString
        }

        fun createHmac(key: String, data: String): String {
            val mac = Mac.getInstance("HmacSHA1")
            val secret_key = SecretKeySpec(key.toByteArray(), mac.algorithm)
            mac.init(secret_key)
            val digest = mac.doFinal(data.toByteArray())
            return Base64.encodeToString(digest, Base64.NO_WRAP)
        }

        private fun getKey(context: Context): String {
            val stored = ServicesUtil.getHmacSecretKey(context)
            val key = StringBuilder()
            for (a in stored.reversed()) {
                val value = a.toInt() xor 28
                key.append(value.toChar())
            }
            return key.toString()
        }

    }

}