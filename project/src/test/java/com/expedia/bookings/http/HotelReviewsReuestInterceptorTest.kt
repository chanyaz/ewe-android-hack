package com.expedia.bookings.http

import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.interceptors.MockChain
import com.expedia.bookings.server.EndpointProvider
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelReviewRequestInterceptorTest {

    private val context = RuntimeEnvironment.application
    private lateinit var endpointProvider: EndpointProvider
    private lateinit var interceptor: HotelReviewsRequestInterceptor

    @Before
    fun before() {
        val serverUrlPath = ProductFlavorFeatureConfiguration.getInstance()
                .serverEndpointsConfigurationPath
        val serverUrlStream = context.assets.open(serverUrlPath)

        endpointProvider = EndpointProvider(context, serverUrlStream)
        interceptor = HotelReviewsRequestInterceptor(context)
    }

    @Test
    fun testIntercept() {
        val chain = MockChain()
        val response = interceptor.intercept(chain)

        assertEquals("http://endpoint.com/path", response.request().url().toString())
        assertEquals("MOBILE-APP-ANDROID", response.request().header("clientId"))
        assertEquals("0601x448cd474e227077mlq964842293", response.request().header("apiKey"))
    }
}
