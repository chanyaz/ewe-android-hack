package com.expedia.bookings.dagger

import android.content.Context
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.server.EndpointProvider
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.Ui
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightModuleTest {

    private val context: Context by lazy {
        RuntimeEnvironment.application
    }

    var server: MockWebServer = MockWebServer()
        @Rule get

    private val mockEndpointProvider = Mockito.mock(EndpointProvider::class.java)

    @Before
    fun setup() {
        server.enqueue(MockResponse().setBody("""{"q":"chicago","rid":"some-random-guid","rc":"OK","sr":[]}"""))

        Mockito.`when`(mockEndpointProvider.essEndpointUrl).thenReturn("http://localhost:" + server.port)
        Mockito.`when`(mockEndpointProvider.gaiaEndpointUrl).thenReturn("http://localhost:" + server.port)
    }

    @Test
    fun essDeviceIsMobileForPhone() {
        val sut = givenSuggestionServicesInitialized()

        sut.getAirports("chicago", true, TestObserver(), "guid")

        kotlin.test.assertEquals("mobile", server.takeRequest().requestUrl.queryParameter("device"))
    }

    @Test
    @Config(qualifiers = "sw600dp")
    fun essDeviceIsTabletForTablet() {
        val sut = givenSuggestionServicesInitialized()

        sut.getAirports("chicago", true, TestObserver(), "guid")

        kotlin.test.assertEquals("tablet", server.takeRequest().requestUrl.queryParameter("device"))
    }

    @Test
    fun essCommonParamsAreCorrect() {
        val sut = givenSuggestionServicesInitialized()

        sut.getAirports("chicago", true, TestObserver(), "guid")

        val requestUrl = server.takeRequest().requestUrl
        kotlin.test.assertEquals(PointOfSale.getSuggestLocaleIdentifier(), requestUrl.queryParameter("locale"))
        kotlin.test.assertEquals(PointOfSale.getPointOfSale().siteId, Integer.valueOf(requestUrl.queryParameter("siteid")))
        kotlin.test.assertEquals(ServicesUtil.generateClient(context), requestUrl.queryParameter("client"))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testKongEndPointForUSPOS() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(RuntimeEnvironment.application, AbacusUtils.EBAndroidAppFlightsAPIKongEndPoint, 1)
        val flightServices = givenFlightServicesInitialized()
        assertEquals("https://apim.expedia.com/m/", flightServices.endpoint)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testExpediaEndPointForUSPOS() {
        val flightServices = givenFlightServicesInitialized()
        assertEquals("https://www.expedia.com/", flightServices.endpoint)
    }

    private fun givenFlightServicesInitialized(): FlightServices {
        val appComponent = Ui.getApplication(context).appComponent()
        val serverUrlPath = ProductFlavorFeatureConfiguration.getInstance().serverEndpointsConfigurationPath
        val serverUrlStream = context.assets.open(serverUrlPath)
        return FlightModule().provideFlightServices(context, EndpointProvider(context, serverUrlStream), appComponent.okHttpClient(),
                appComponent.requestInterceptor())
    }

    private fun givenSuggestionServicesInitialized(): SuggestionV4Services {
        val appComponent = Ui.getApplication(context).appComponent()

        return FlightSuggestionModule().provideSuggestionV4Services(mockEndpointProvider, OkHttpClient(),
                appComponent.requestInterceptor(), appComponent.essRequestInterceptor(),
                appComponent.gaiaRequestInterceptor())
    }
}
