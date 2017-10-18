package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import okhttp3.HttpUrl
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HMACUtilTests {

    val context: Context = RuntimeEnvironment.application

    @Test
    fun testAuthorizationString() {
        val salt = "abcdefghijklmnop"
        val xDate = "Thu, 29 Jun 2017 09:34:47 UTC"
        val url = HttpUrl.Builder().scheme("https").host("www.expedia.com").build()
        val method = "GET"
        val userName = context.getString(R.string.exp_u)
        val expectedAuthString = "hmac username=\"$userName\",algorithm=\"hmac-sha1\",headers=\"request-line x-date salt\",signature=\"gtQ/p2XxSVfK3tUDuF88vDyXbWo=\""
        val authString = HMACUtil.getAuthorization(context, url, method, xDate, salt)

        assertEquals(expectedAuthString, authString)
    }

    @Test
    fun testSalt() {
        val expectedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val salt = HMACUtil.generateSalt(16)
        assertEquals(16, salt.length)
        for (i in 0..salt.length - 1) {
            assertTrue(expectedChars.contains(salt[i]))
        }
    }

    @Test
    fun testXDate() {
        val testDate = DateTime(2017, 9, 26, 19, 5, 5 , 7, DateTimeZone.UTC)
        val expectedDate = "Tue, 26 Sep 2017 19:05:05 UTC"
        val xDate = HMACUtil.getXDate(testDate)
        assertEquals(expectedDate, xDate)
    }
}