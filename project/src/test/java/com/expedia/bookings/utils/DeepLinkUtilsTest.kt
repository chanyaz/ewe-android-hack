package com.expedia.bookings.utils

import android.net.Uri
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.ClientLogServices
import com.expedia.bookings.services.IClientLogServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.schedulers.Schedulers
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class DeepLinkUtilsTest {
    var server: MockWebServer = MockWebServer()
        @Rule get

    lateinit var clientLogServices: IClientLogServices
    lateinit var clientLogRequest: String

    @Before
    fun setup() {
        clientLogRequest = ""
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        clientLogServices = ClientLogServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())

        val dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                clientLogRequest = request.path
                return MockResponse()
            }
        }
        server.setDispatcher(dispatcher)
    }

    @Test
    fun emlcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&emlcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("emlcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("emlcid")
    }

    @Test
    fun caseInsensitive() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&EmlcId=TEST_BRAD_MDPCID_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("emlcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("emlcid")
    }

    @Test
    fun valuePreserveCase() {
        // Lettercase of value should match url parameter
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&emlcid=mIxEd_cAsE")
        assertTrue(clientLogRequest.contains("emlcid=mIxEd_cAsE"))
        assertOmnitureDeepLinkArgsSetup("emlcid")
    }

    @Test
    fun semcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&semcid=TEST_BRAD_SEMCID_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("semcid=TEST_BRAD_SEMCID_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("semcid")
    }

    @Test
    fun brandcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&brandcid=TEST_BRAD_BRANDCID_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("brandcid=TEST_BRAD_BRANDCID_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("brandcid")
    }

    @Test
    fun seocid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&seocid=TEST_BRAD_SEOCID_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("seocid=TEST_BRAD_SEOCID_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("seocid")
    }

    @Test
    fun kword() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?kword=Brads_Super_Duper_Test_App_Links")
        assertTrue(clientLogRequest.contains("kword=Brads_Super_Duper_Test_App_Links"))
        assertOmnitureDeepLinkArgsSetup("kword")
    }

    @Test
    fun semcidAndKword() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?semcid=TEST_BRAD_KWORDPARAMETER_APP_LINKS&kword=Brads_Super_Duper_Test_App_Links")
        assertTrue(clientLogRequest.contains("semcid=TEST_BRAD_KWORDPARAMETER_APP_LINKS&kword=Brads_Super_Duper_Test_App_Links"))
        assertOmnitureDeepLinkArgsSetup("semcid")
        assertOmnitureDeepLinkArgsSetup("kword")
    }

    @Test
    fun mdpcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&mdpcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("mdpcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("mdpcid")
    }

    @Test
    fun mdpcidAndMdpdtl() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&mdpcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK&mdpdtl=TEST_BRAD_MDPDTL_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("mdpcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK&mdpdtl=TEST_BRAD_MDPDTL_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("mdpcid")
        assertOmnitureDeepLinkArgsSetup("mdpdtl")
    }

    @Test
    fun mdpdtlWithoutmdpcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&mdpdtl=TEST_BRAD_MDPDTL_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("mdpdtl=TEST_BRAD_MDPDTL_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("mdpdtl")
    }

    @Test
    fun olacid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&olacid=TEST_BRAD_OLACID_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("olacid=TEST_BRAD_OLACID_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("olacid")
    }

    @Test
    fun olacidAndOladtl() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&olacid=TEST_BRAD_OLACID_UNIVERSAL_LINK&oladtl=TEST_BRAD_OLADTL_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("oladtl=TEST_BRAD_OLADTL_UNIVERSAL_LINK&olacid=TEST_BRAD_OLACID_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("oladtl")
        assertOmnitureDeepLinkArgsSetup("olacid")
    }

    @Test
    fun oladtlWithoutOlacid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&oladtl=TEST_BRAD_OLADTL_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("oladtl=TEST_BRAD_OLADTL_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("oladtl")
    }

    @Test
    fun affcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&affcid=TEST_BRAD_AFFCID_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("affcid=TEST_BRAD_AFFCID_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("affcid")
    }

    @Test
    fun affcidAndafflid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&affcid=TEST_BRAD_AFFCID_UNIVERSAL_LINK&afflid=TEST_BRAD_AFFLID_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("afflid=TEST_BRAD_AFFLID_UNIVERSAL_LINK&affcid=TEST_BRAD_AFFCID_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("afflid")
        assertOmnitureDeepLinkArgsSetup("affcid")
    }

    @Test
    fun afflidWithoutAffcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&afflid=TEST_BRAD_AFFLID_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("afflid=TEST_BRAD_AFFLID_UNIVERSAL_LINK"))
        assertFalse(clientLogRequest.contains("affcid"))
        assertOmnitureDeepLinkArgsSetup("afflid")
    }

    @Test
    fun icmcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&icmcid=TEST_BRAD_ICMCID_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("icmcid=TEST_BRAD_ICMCID_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("icmcid")
    }

    @Test
    fun icmcidAndIcmdtl() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&icmcid=TEST_BRAD_ICMCID_UNIVERSAL_LINK&icmdtl=TEST_BRAD_ICMDTL_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("icmcid=TEST_BRAD_ICMCID_UNIVERSAL_LINK&icmdtl=TEST_BRAD_ICMDTL_UNIVERSAL_LINK"))
        assertOmnitureDeepLinkArgsSetup("icmcid")
        assertOmnitureDeepLinkArgsSetup("icmdtl")
    }

    @Test
    fun icmdtlWithoutIcmcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&icmdtl=TEST_BRAD_ICMDTL_UNIVERSAL_LINK")
        assertTrue(clientLogRequest.contains("icmdtl=TEST_BRAD_ICMDTL_UNIVERSAL_LINK"))
        assertFalse(clientLogRequest.contains("icmcid"))
        assertOmnitureDeepLinkArgsSetup("icmdtl")
    }

    @Test
    fun gclid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?startDate=12/27/2017&endDate=01/03/2018&regionId=602231&gclid=SEMGCLID_KRABI_TEST_GCLID")
        assertTrue(clientLogRequest.contains("gclid=SEMGCLID_KRABI_TEST_GCLID"))
        assertOmnitureDeepLinkArgsSetup("icmdtl")
    }

    @Test
    fun gclidAndSemcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?startDate=12/27/2017&endDate=01/03/2018&regionId=602231&semcid=SEM_KRABI_TEST_GCLID&gclid=SEMGCLID_KRABI_TEST_GCLID")
        assertTrue(clientLogRequest.contains("gclid=SEMGCLID_KRABI_TEST_GCLID&semcid=SEM_KRABI_TEST_GCLID"))
        assertOmnitureDeepLinkArgsSetup("gclid")
        assertOmnitureDeepLinkArgsSetup("semcid")
    }

    @Test
    fun testNoDeeplinkArgs() {
        val trackingArgsSizeBefore = OmnitureTracking.getDeepLinkArgs().size
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033")
        assertEquals("", clientLogRequest)
        val trackingArgsSizeAfter = OmnitureTracking.getDeepLinkArgs().size
        assertEquals(trackingArgsSizeBefore, trackingArgsSizeAfter)
    }

    fun trackDeepLink(uri: String) {
        val data = Uri.parse(uri)
        val queryData = StrUtils.getQueryParameterNames(data)
        DeepLinkUtils.parseAndTrackDeepLink(clientLogServices, data, queryData)
    }

    private fun assertOmnitureDeepLinkArgsSetup(key: String) {
        assertTrue(OmnitureTracking.getDeepLinkArgs().containsKey(key))
    }
}
