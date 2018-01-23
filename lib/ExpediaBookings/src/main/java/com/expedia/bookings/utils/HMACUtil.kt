package com.expedia.bookings.utils

import okhttp3.HttpUrl
import okio.ByteString
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import java.util.Random
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HMACUtil {

    companion object {

        fun getXDate(dateTime: DateTime): String {
            val fmt = DateTimeFormat.forPattern("E, dd MMM yyyy HH:mm:ss zzz").withLocale(Locale.ENGLISH)
            return fmt.print(dateTime)
        }

        fun generateSalt(length: Int): String {
            val alphaNumericChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val salt = StringBuilder()
            for (i in 0 until length) {
                salt.append(alphaNumericChars[Random().nextInt(alphaNumericChars.length)])
            }
            return salt.toString()
        }

        fun getAuthorizationHeaderValue(url: HttpUrl, method: String, date: String, salt: String, storedKey: String, username: String): String {
            var pathAndQuery = url.encodedPath()
            url.encodedQuery()?.let {
                pathAndQuery += "?$it"
            }
            val requestLine = "$method $pathAndQuery HTTP/1.1"
            val stringToSign = "$requestLine\nx-date: $date\nsalt: $salt"
            val hmac = createHmac(unObfuscateKey(storedKey), stringToSign)
            return "hmac username=\"$username\",algorithm=\"hmac-sha1\",headers=\"request-line x-date salt\",signature=\"$hmac\""
        }

        fun createHmac(key: String, data: String): String {
            val mac = Mac.getInstance("HmacSHA1")
            val secretKeySpec = SecretKeySpec(key.toByteArray(), mac.algorithm)
            mac.init(secretKeySpec)
            val digest = mac.doFinal(data.toByteArray())
            val byteString = ByteString.of(digest, 0, digest.size)
            return byteString.base64()
        }

        private fun unObfuscateKey(storedKey: String): String {
            val key = StringBuilder()
            for (a in storedKey.reversed()) {
                val value = a.toInt() xor 28
                key.append(value.toChar())
            }
            return key.toString()
        }
    }
}
