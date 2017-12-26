package com.expedia.bookings.utils

import com.expedia.bookings.test.MockClientLogServices
import com.expedia.bookings.tracking.OmnitureTracking
import okhttp3.HttpUrl
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeepLinkUtilsTest {

    lateinit var mockClientLogServices: MockClientLogServices

    @Before
    fun setup() {
        mockClientLogServices = MockClientLogServices()
    }

    @Test
    fun emlcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&emlcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_MDPCID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("emlcid"))
        assertOmnitureDeepLinkArgsSetup("emlcid")
    }

    @Test
    fun caseInsensitive() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&EmlcId=TEST_BRAD_MDPCID_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_MDPCID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("emlcid"))
        assertOmnitureDeepLinkArgsSetup("emlcid")
    }

    @Test
    fun valuePreserveCase() {
        // Lettercase of value should match url parameter
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&emlcid=mIxEd_cAsE")
        assertEquals("mIxEd_cAsE", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("emlcid"))
        assertOmnitureDeepLinkArgsSetup("emlcid")
    }

    @Test
    fun semcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&semcid=TEST_BRAD_SEMCID_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_SEMCID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("semcid"))
        assertOmnitureDeepLinkArgsSetup("semcid")
    }

    @Test
    fun brandcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&brandcid=TEST_BRAD_BRANDCID_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_BRANDCID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("brandcid"))
        assertOmnitureDeepLinkArgsSetup("brandcid")
    }

    @Test
    fun seocid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&seocid=TEST_BRAD_SEOCID_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_SEOCID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("seocid"))
        assertOmnitureDeepLinkArgsSetup("seocid")
    }

    @Test
    fun kword() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?kword=Brads_Super_Duper_Test_App_Links")
        assertEquals("Brads_Super_Duper_Test_App_Links", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("kword"))
        assertOmnitureDeepLinkArgsSetup("kword")
    }

    @Test
    fun semcidAndKword() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?semcid=TEST_BRAD_KWORDPARAMETER_APP_LINKS&kword=Brads_Super_Duper_Test_App_Links")
        assertEquals("TEST_BRAD_KWORDPARAMETER_APP_LINKS", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("semcid"))
        assertEquals("Brads_Super_Duper_Test_App_Links", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("kword"))
        assertOmnitureDeepLinkArgsSetup("semcid")
        assertOmnitureDeepLinkArgsSetup("kword")
    }

    @Test
    fun mdpcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&mdpcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_MDPCID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("mdpcid"))
        assertOmnitureDeepLinkArgsSetup("mdpcid")
    }

    @Test
    fun mdpcidAndMdpdtl() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&mdpcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK&mdpdtl=TEST_BRAD_MDPDTL_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_MDPCID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("mdpcid"))
        assertEquals("TEST_BRAD_MDPDTL_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("mdpdtl"))
        assertOmnitureDeepLinkArgsSetup("mdpcid")
        assertOmnitureDeepLinkArgsSetup("mdpdtl")
    }

    @Test
    fun mdpdtlWithoutmdpcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&mdpdtl=TEST_BRAD_MDPDTL_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_MDPDTL_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("mdpdtl"))
        assertOmnitureDeepLinkArgsSetup("mdpdtl")
    }

    @Test
    fun olacid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&olacid=TEST_BRAD_OLACID_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_OLACID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("olacid"))
        assertOmnitureDeepLinkArgsSetup("olacid")
    }

    @Test
    fun olacidAndOladtl() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&olacid=TEST_BRAD_OLACID_UNIVERSAL_LINK&oladtl=TEST_BRAD_OLADTL_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_OLADTL_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("oladtl"))
        assertEquals("TEST_BRAD_OLACID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("olacid"))
        assertOmnitureDeepLinkArgsSetup("oladtl")
        assertOmnitureDeepLinkArgsSetup("olacid")
    }

    @Test
    fun oladtlWithoutOlacid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&oladtl=TEST_BRAD_OLADTL_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_OLADTL_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("oladtl"))
        assertOmnitureDeepLinkArgsSetup("oladtl")
    }

    @Test
    fun affcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&affcid=TEST_BRAD_AFFCID_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_AFFCID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("affcid"))
        assertOmnitureDeepLinkArgsSetup("affcid")
    }

    @Test
    fun affcidAndafflid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&affcid=TEST_BRAD_AFFCID_UNIVERSAL_LINK&afflid=TEST_BRAD_AFFLID_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_AFFLID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("afflid"))
        assertEquals("TEST_BRAD_AFFCID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("affcid"))
        assertOmnitureDeepLinkArgsSetup("afflid")
        assertOmnitureDeepLinkArgsSetup("affcid")
    }

    @Test
    fun afflidWithoutAffcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&afflid=TEST_BRAD_AFFLID_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_AFFLID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("afflid"))
        assertNull(mockClientLogServices.lastSeenDeepLinkQueryParams?.get("affcid"))
        assertOmnitureDeepLinkArgsSetup("afflid")
    }

    @Test
    fun icmcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&icmcid=TEST_BRAD_ICMCID_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_ICMCID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("icmcid"))
        assertOmnitureDeepLinkArgsSetup("icmcid")
    }

    @Test
    fun icmcidAndIcmdtl() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&icmcid=TEST_BRAD_ICMCID_UNIVERSAL_LINK&icmdtl=TEST_BRAD_ICMDTL_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_ICMCID_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("icmcid"))
        assertEquals("TEST_BRAD_ICMDTL_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("icmdtl"))
        assertOmnitureDeepLinkArgsSetup("icmcid")
        assertOmnitureDeepLinkArgsSetup("icmdtl")
    }

    @Test
    fun icmdtlWithoutIcmcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&icmdtl=TEST_BRAD_ICMDTL_UNIVERSAL_LINK")
        assertEquals("TEST_BRAD_ICMDTL_UNIVERSAL_LINK", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("icmdtl"))
        assertNull(mockClientLogServices.lastSeenDeepLinkQueryParams?.get("icmcid"))
        assertOmnitureDeepLinkArgsSetup("icmdtl")
    }

    @Test
    fun gclid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?startDate=12/27/2017&endDate=01/03/2018&regionId=602231&gclid=SEMGCLID_KRABI_TEST_GCLID")
        assertEquals("SEMGCLID_KRABI_TEST_GCLID", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("gclid"))
        assertOmnitureDeepLinkArgsSetup("icmdtl")
    }

    @Test
    fun gclidAndSemcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?startDate=12/27/2017&endDate=01/03/2018&regionId=602231&semcid=SEM_KRABI_TEST_GCLID&gclid=SEMGCLID_KRABI_TEST_GCLID")
        assertEquals("SEM_KRABI_TEST_GCLID", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("semcid"))
        assertEquals("SEMGCLID_KRABI_TEST_GCLID", mockClientLogServices.lastSeenDeepLinkQueryParams?.get("gclid"))
        assertOmnitureDeepLinkArgsSetup("gclid")
        assertOmnitureDeepLinkArgsSetup("semcid")
    }

    @Test
    fun testNoDeeplinkArgs() {
        val trackingArgsSizeBefore = OmnitureTracking.getDeepLinkArgs().size
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033")
        assertNull(mockClientLogServices.lastSeenDeepLinkQueryParams)
        val trackingArgsSizeAfter = OmnitureTracking.getDeepLinkArgs().size
        assertEquals(trackingArgsSizeBefore, trackingArgsSizeAfter)
    }

    private fun trackDeepLink(url: String) {
        DeepLinkUtils.parseAndTrackDeepLink(mockClientLogServices, HttpUrl.parse(url))
    }

    private fun assertOmnitureDeepLinkArgsSetup(key: String) {
        assertTrue(OmnitureTracking.getDeepLinkArgs().containsKey(key))
    }
}
