package com.expedia.bookings.utils

import okhttp3.Call
import okhttp3.Connection
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HMACInterceptorTest {

    @Test
    fun testAuthorizationHeader() {
        val expectedAuthString = "hmac username=\"fe627e29-3afb-4384-9fc0-a1d2a4dcd30c\",algorithm=\"hmac-sha1\",headers=\"request-line x-date salt\",signature=\"GI7T6jfxRzNLWQyy/C7pfxcu2p0=\""
        val interceptor = injectedHMACInterceptor()
        val mockChain = MockChain()
        interceptor.intercept(mockChain)
        assertEquals(expectedAuthString, mockChain.lastSeenRequest.header("Authorization"))
    }

    @Test
    fun testSaltHeader() {
        val interceptor = injectedHMACInterceptor()
        val mockChain = MockChain()
        interceptor.intercept(mockChain)
        assertEquals("abcdefghijklmnop", mockChain.lastSeenRequest.header("salt"))
    }

    @Test
    fun testXDateHeader() {
        val interceptor = injectedHMACInterceptor()
        val mockChain = MockChain()
        interceptor.intercept(mockChain)
        assertEquals("Thu, 29 Jun 2017 09:34:47 UTC", mockChain.lastSeenRequest.header("x-date"))
    }

    @Test
    fun testAPIMSigned() {
        val interceptor = injectedHMACInterceptor()
        val mockChain = MockChain()
        mockChain.setMockHostToReturn("apim.expedia.com")
        interceptor.intercept(mockChain)
        assertNotNull(mockChain.lastSeenRequest.header("Authorization"))
    }

    @Test
    fun testCurrentDomainSigned() {
        val interceptor = injectedHMACInterceptor()
        val mockChain = MockChain()
        mockChain.setMockHostToReturn("www.expedia.com")
        interceptor.intercept(mockChain)
        assertNotNull(mockChain.lastSeenRequest.header("Authorization"))
    }

    @Test
    fun testThirdPartyDomainNotSigned() {
        val interceptor = injectedHMACInterceptor()
        val mockChain = MockChain()
        mockChain.setMockHostToReturn("do.not.touch.me.com")
        interceptor.intercept(mockChain)
        assertNull(mockChain.lastSeenRequest.header("Authorization"))
    }

    private fun injectedHMACInterceptor(): HMACInterceptor {
        return HMACInterceptor("fe627e29-3afb-4384-9fc0-a1d2a4dcd30c",
                "R(Y_O/y]tn)z/m-O",
                MockDateTimeSource(),
                MockSaltSource(),
                MockCurrentDomainSource())
    }

    class MockDateTimeSource : DateTimeSource {
        var dateTimeToReturn: DateTime = DateTime(2017, 6, 29, 9, 34, 47, 0, DateTimeZone.UTC)
        override fun now(zone: DateTimeZone): DateTime {
            return dateTimeToReturn
        }
    }

    class MockSaltSource : SaltSource {
        var saltToReturn: String = "abcdefghijklmnop"
        override fun salt(length: Int): String {
            return saltToReturn
        }
    }

    class MockCurrentDomainSource : CurrentDomainSource {
        override fun currentDomain(): String {
            return "expedia.com"
        }
    }

    class MockChain : Interceptor.Chain {

        lateinit var lastSeenRequest: Request
        @Throws(IOException::class)
        override fun proceed(request: Request): Response {
            lastSeenRequest = request
            return Response.Builder()
                    .code(200)
                    .protocol(Protocol.HTTP_1_1)
                    .request(request)
                    .message("")
                    .build()
        }

        private val someExpediaUrl = HttpUrl.Builder().scheme("https").host("www.expedia.com").build()
        var requestToReturn: Request = Request.Builder()
                .url(someExpediaUrl)
                .get()
                .build()

        fun setMockHostToReturn(host: String) {
            val url = HttpUrl.Builder().scheme("https").host(host).build()
            requestToReturn = Request.Builder()
                    .url(url)
                    .get()
                    .build()
        }

        override fun request(): Request {
            return requestToReturn
        }

        override fun writeTimeoutMillis(): Int {
            TODO("not implemented")
        }

        override fun call(): Call {
            TODO("not implemented")
        }

        override fun withWriteTimeout(timeout: Int, unit: TimeUnit?): Interceptor.Chain {
            TODO("not implemented")
        }

        override fun connectTimeoutMillis(): Int {
            TODO("not implemented")
        }

        override fun connection(): Connection? {
            TODO("not implemented")
        }

        override fun withConnectTimeout(timeout: Int, unit: TimeUnit?): Interceptor.Chain {
            TODO("not implemented")
        }

        override fun withReadTimeout(timeout: Int, unit: TimeUnit?): Interceptor.Chain {
            TODO("not implemented")
        }

        override fun readTimeoutMillis(): Int {
            TODO("not implemented")
        }
    }
}
