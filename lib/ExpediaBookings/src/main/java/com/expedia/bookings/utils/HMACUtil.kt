package com.expedia.bookings.utils

import okio.ByteString
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HMACUtil {
    companion object {
        fun createHmac(key: String, data: String): String {
            val mac = Mac.getInstance("HmacSHA1")
            val secretKeySpec = SecretKeySpec(key.toByteArray(), mac.algorithm)
            mac.init(secretKeySpec)
            val digest = mac.doFinal(data.toByteArray())
            val byteString = ByteString.of(digest, 0, digest.size)
            return byteString.base64()
        }
    }
}
