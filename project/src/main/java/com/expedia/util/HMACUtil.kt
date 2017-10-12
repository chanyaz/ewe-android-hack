package com.expedia.bookings.utils

import android.content.Context
import android.util.Base64
import com.expedia.bookings.R
import okhttp3.HttpUrl
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import java.util.Random
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HMACUtil {

    companion object {

        @JvmStatic fun getXDate(dateTime: DateTime): String {
            val fmt = DateTimeFormat.forPattern("E, dd MMM yyyy HH:mm:ss zzz").withLocale(Locale.ENGLISH)
            val date = fmt.print(dateTime)
            return date
        }

        @JvmStatic fun generateSalt(length: Int): String {
            val alphaNumericChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val salt = StringBuilder()
            for (i in 0..length - 1) {
                salt.append(alphaNumericChars[Random().nextInt(alphaNumericChars.length)])
            }
            return salt.toString()
        }

        @JvmStatic fun getAuthorization(context: Context, url: HttpUrl, method: String, date: String, salt: String): String {
            val pathAndQuery = url.encodedPath() + "?" + url.encodedQuery()
            val requestLine = "$method $pathAndQuery HTTP/1.1"
            val stringToSign = "$requestLine\nx-date: $date\nsalt: $salt"
            val hmac = createHmac(getKey(context), stringToSign)
            val userName = context.resources.getString(R.string.exp_u)
            val authString = "hmac username=\"$userName\",algorithm=\"hmac-sha1\",headers=\"request-line x-date salt\",signature=\"$hmac\""
            return authString
        }

        private fun createHmac(key: String, data: String): String {
            val mac = Mac.getInstance("HmacSHA1")
            val secret_key = SecretKeySpec(key.toByteArray(), mac.algorithm)
            mac.init(secret_key)
            val digest = mac.doFinal(data.toByteArray())
            return Base64.encodeToString(digest, Base64.NO_WRAP)
        }

        private fun getKey(context: Context): String {
            val stored = context.resources.getString(R.string.exp_k)
            val key = StringBuilder()
            for (a in stored.reversed()) {
                val value = a.toInt() xor 28
                key.append(value.toChar())
            }
            return key.toString()
        }

        @JvmStatic fun getSignedKrazyGlueUrl(baseUrl: String, key: String, destinationCode: String, destinationDateTime: String) : String {
            val urlWithParams = "$baseUrl?partnerId=expedia-hot-mobile-conf&outboundEndDateTime=$destinationDateTime&destinationTla=$destinationCode&fencedResponse=true"
            val signature = createHmac(key, urlWithParams).replace("+", "-").replace("/", "_").removeSuffix("=")
            val signedUrl = "$urlWithParams&signature=$signature"
            return signedUrl
        }
    }

}