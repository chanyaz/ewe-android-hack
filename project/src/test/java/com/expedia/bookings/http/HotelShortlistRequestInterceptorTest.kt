package com.expedia.bookings.http

import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.server.EndpointProvider
import com.expedia.bookings.test.robolectric.RobolectricRunner
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Connection
import okhttp3.Interceptor.Chain
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelShortlistRequestInterceptorTest {

    private val context = RuntimeEnvironment.application
    private lateinit var endpointProvider: EndpointProvider
    private lateinit var interceptor: HotelShortlistRequestInterceptor

    @Before
    fun before() {
        val serverUrlPath = ProductFlavorFeatureConfiguration.getInstance()
                .serverEndpointsConfigurationPath
        val serverUrlStream = context.assets.open(serverUrlPath)

        endpointProvider = EndpointProvider(context, serverUrlStream)
        interceptor = HotelShortlistRequestInterceptor(context, endpointProvider)
    }

    @Test
    fun testIntercept() {
        val chain = MockChain()
        val response = interceptor.intercept(chain)

        assertEquals("http://endpoint.com/path?clientId=androidHotel&langId=1033", response.request().url().toString())
        assertEquals("MTA3ZTc3ZmYtZjZiMi00M2IxLWFjODEtYzY3ZjcwMmFlY2Iy", response.request().header("client-token"))
    }

    @Test
    fun testInterceptOverrideClientIdLangId() {
        val chain = MockChain("?clientId=clientid&langId=langid")
        val response = interceptor.intercept(chain)

        assertEquals("http://endpoint.com/path?clientId=androidHotel&langId=1033", response.request().url().toString())
        assertEquals("MTA3ZTc3ZmYtZjZiMi00M2IxLWFjODEtYzY3ZjcwMmFlY2Iy", response.request().header("client-token"))
    }

    @Test
    fun testInterceptOverrideDefaultClientIdLangId() {
        val chain = MockChain("?clientid=clientid&langid=langid")
        val response = interceptor.intercept(chain)

        assertEquals("http://endpoint.com/path?clientId=androidHotel&langId=1033", response.request().url().toString())
        assertEquals("MTA3ZTc3ZmYtZjZiMi00M2IxLWFjODEtYzY3ZjcwMmFlY2Iy", response.request().header("client-token"))
    }

    private class MockChain(params: String = "") : Chain {

        private val requestBuilder = Request.Builder()
        private val responseBuilder = Response.Builder()

        init {
            requestBuilder.url("http://endpoint.com/path$params")

            responseBuilder.request(requestBuilder.build())
            responseBuilder.protocol(Protocol.HTTP_2)
            responseBuilder.code(200)
            responseBuilder.message("OK")
        }

        override fun request(): Request {
            return requestBuilder.build()
        }

        @Throws(IOException::class)
        override fun proceed(request: Request): Response {
            responseBuilder.request(request)
            return responseBuilder.build()
        }

        override fun connection(): Connection? {
            return null
        }

        override fun call(): Call {
            return MockCall(requestBuilder.build(), responseBuilder.build())
        }

        override fun connectTimeoutMillis(): Int {
            return 0
        }

        override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Chain {
            return this
        }

        override fun readTimeoutMillis(): Int {
            return 0
        }

        override fun withReadTimeout(timeout: Int, unit: TimeUnit): Chain {
            return this
        }

        override fun writeTimeoutMillis(): Int {
            return 0
        }

        override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Chain {
            return this
        }
    }

    private class MockCall(val request: Request, val response: Response) : Call {

        override fun request(): Request {
            return request
        }

        override fun execute(): Response {
            return response
        }

        override fun enqueue(responseCallback: Callback?) {
        }

        override fun cancel() {
        }

        override fun isExecuted(): Boolean {
            return true
        }

        override fun isCanceled(): Boolean {
            return true
        }

        override fun clone(): Call {
            return this
        }
    }
}
