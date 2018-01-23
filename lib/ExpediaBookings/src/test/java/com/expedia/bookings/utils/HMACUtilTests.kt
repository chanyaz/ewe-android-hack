package com.expedia.bookings.utils

import okhttp3.HttpUrl
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HMACUtilTests {

    @Test
    fun testAuthorizationString() {
        val salt = "abcdefghijklmnop"
        val xDate = "Thu, 29 Jun 2017 09:34:47 UTC"
        val url = HttpUrl.Builder().scheme("https").host("www.expedia.com").build()
        val method = "GET"
        val expectedAuthString = "hmac username=\"fe627e29-3afb-4384-9fc0-a1d2a4dcd30c\",algorithm=\"hmac-sha1\",headers=\"request-line x-date salt\",signature=\"GI7T6jfxRzNLWQyy/C7pfxcu2p0=\""
        val authString = HMACUtil.getAuthorizationHeaderValue(url, method, xDate, salt, "R(Y_O/y]tn)z/m-O", "fe627e29-3afb-4384-9fc0-a1d2a4dcd30c")
        assertEquals(expectedAuthString, authString)
    }

    @Test
    fun testSalt() {
        val expectedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val salt = HMACUtil.generateSalt(16)
        assertEquals(16, salt.length)
        for (i in 0 until salt.length) {
            assertTrue(expectedChars.contains(salt[i]))
        }
    }

    @Test
    fun testXDate() {
        val testDate = DateTime(2017, 9, 26, 19, 5, 5, 7, DateTimeZone.UTC)
        val expectedDate = "Tue, 26 Sep 2017 19:05:05 UTC"
        val xDate = HMACUtil.getXDate(testDate)
        assertEquals(expectedDate, xDate)
    }
}
