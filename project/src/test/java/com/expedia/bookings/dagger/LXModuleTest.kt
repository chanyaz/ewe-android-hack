package com.expedia.bookings.dagger

import android.content.Context
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.server.EndpointProvider
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.Ui
import io.reactivex.schedulers.Schedulers
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
class LXModuleTest {

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

        sut.getLxSuggestionsV4("chicago", TestObserver(), false, false)

        assertEquals("mobile", server.takeRequest().requestUrl.queryParameter("device"))
    }

    @Test
    @Config(qualifiers = "sw600dp")
    fun essDeviceIsTabletForTablet() {
        val sut = givenSuggestionServicesInitialized()

        sut.getLxSuggestionsV4("chicago", TestObserver(), false, false)

        assertEquals("tablet", server.takeRequest().requestUrl.queryParameter("device"))
    }

    @Test
    fun essCommonParamsAreCorrect() {
        val sut = givenSuggestionServicesInitialized()

        sut.getLxSuggestionsV4("chicago", TestObserver(), false, false)

        val requestUrl = server.takeRequest().requestUrl
        assertEquals(PointOfSale.getSuggestLocaleIdentifier(), requestUrl.queryParameter("locale"))
        assertEquals(PointOfSale.getPointOfSale().siteId, Integer.valueOf(requestUrl.queryParameter("siteid")))
        assertEquals(ServicesUtil.generateClient(context), requestUrl.queryParameter("client"))
    }

    @Test
    fun testEssRegionTypeParam() {
        val sut = givenSuggestionServicesInitialized()
        sut.getLxSuggestionsV4("chicago", TestObserver(), false, false)
        server.takeRequest()
        assertEquals(30, Integer.valueOf(server.takeRequest().requestUrl.queryParameter("regiontype")))
    }

    @Test
    fun testEssRegionTypeParamWithFeatureEnabled() {
        val sut = givenSuggestionServicesInitialized()
        sut.getLxSuggestionsV4("chicago", TestObserver(), false, true)
        server.takeRequest()
        assertEquals(287, Integer.valueOf(server.takeRequest().requestUrl.queryParameter("regiontype")))
    }

    private fun givenSuggestionServicesInitialized(): SuggestionV4Services {
        val appComponent = Ui.getApplication(context).appComponent()
        return SuggestionV4Services(mockEndpointProvider.essEndpointUrl, mockEndpointProvider.gaiaEndpointUrl, OkHttpClient(),
                appComponent.requestInterceptor(), appComponent.essRequestInterceptor(),
                appComponent.gaiaRequestInterceptor(), Schedulers.trampoline(), Schedulers.trampoline())
    }
}
