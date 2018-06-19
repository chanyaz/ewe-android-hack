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
        val chain = MockChain(params = "?clientId=clientid&langId=langid")
        val response = interceptor.intercept(chain)

        assertEquals("http://endpoint.com/path?clientId=androidHotel&langId=1033", response.request().url().toString())
        assertEquals("MTA3ZTc3ZmYtZjZiMi00M2IxLWFjODEtYzY3ZjcwMmFlY2Iy", response.request().header("client-token"))
    }

    @Test
    fun testInterceptOverrideDefaultClientIdLangId() {
        val chain = MockChain(params = "?clientid=clientid&langid=langid")
        val response = interceptor.intercept(chain)

        assertEquals("http://endpoint.com/path?clientId=androidHotel&langId=1033", response.request().url().toString())
        assertEquals("MTA3ZTc3ZmYtZjZiMi00M2IxLWFjODEtYzY3ZjcwMmFlY2Iy", response.request().header("client-token"))
    }
}
