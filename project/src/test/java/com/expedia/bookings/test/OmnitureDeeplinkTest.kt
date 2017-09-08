package com.expedia.bookings.test

import android.content.Intent
import android.net.Uri
import com.expedia.bookings.ADMS_Measurement
import com.expedia.bookings.activity.DeepLinkRouterActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.util.ActivityController
import kotlin.test.assertEquals
import kotlin.test.assertNull


@RunWith(RobolectricRunner::class)
class OmnitureDeeplinkTest {

    lateinit var deepLinkRouterActivityController: ActivityController<DeepLinkRouterActivity>
    lateinit var adms: ADMS_Measurement


    @Before
    fun setup() {
        deepLinkRouterActivityController = createSystemUnderTest()
        val context = RuntimeEnvironment.application
        adms = ADMS_Measurement.sharedInstance(context)
    }

    @Test
    fun emlcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&emlcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK")
        assertEquals("EML.TEST_BRAD_MDPCID_UNIVERSAL_LINK", adms.getEvar(22))
    }

    @Test
    fun caseInsensitive() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&EmlcId=TEST_BRAD_MDPCID_UNIVERSAL_LINK")
        assertEquals("EML.TEST_BRAD_MDPCID_UNIVERSAL_LINK", adms.getEvar(22))
    }

    @Test
    fun valuePreserveCase() {
        // Lettercase of value should match url parameter
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&emlcid=mIxEd_cAsE")
        assertEquals("EML.mIxEd_cAsE", adms.getEvar(22))
    }

    @Test
    fun semcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&semcid=TEST_BRAD_SEMCID_UNIVERSAL_LINK")
        assertEquals("SEM.TEST_BRAD_SEMCID_UNIVERSAL_LINK", adms.getEvar(22))
    }

    @Test
    fun brandcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&brandcid=TEST_BRAD_BRANDCID_UNIVERSAL_LINK")
        assertEquals("Brand.TEST_BRAD_BRANDCID_UNIVERSAL_LINK", adms.getEvar(22))
    }

    @Test
    fun seocid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&seocid=TEST_BRAD_SEOCID_UNIVERSAL_LINK")
        assertEquals("SEO.TEST_BRAD_SEOCID_UNIVERSAL_LINK", adms.getEvar(22))
    }

    @Test
    fun kword() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?kword=Brads_Super_Duper_Test_App_Links")
        assertEquals("Brads_Super_Duper_Test_App_Links", adms.getEvar(15))
        assertNull(adms.getEvar(22))
    }

    @Test
    fun semcidAndKword() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?semcid=TEST_BRAD_KWORDPARAMETER_APP_LINKS&kword=Brads_Super_Duper_Test_App_Links")
        assertEquals("Brads_Super_Duper_Test_App_Links", adms.getEvar(15))
        assertEquals("SEM.TEST_BRAD_KWORDPARAMETER_APP_LINKS", adms.getEvar(22))
    }

    @Test
    fun mdpcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&mdpcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK")
        assertEquals("MDP.TEST_BRAD_MDPCID_UNIVERSAL_LINK", adms.getEvar(22))
    }

    @Test
    fun mdpcidAndMdpdtl() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&mdpcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK&mdpdtl=TEST_BRAD_MDPDTL_UNIVERSAL_LINK")
        assertEquals("MDP.TEST_BRAD_MDPCID_UNIVERSAL_LINK&MDPDTL=TEST_BRAD_MDPDTL_UNIVERSAL_LINK", adms.getEvar(22))
    }

    @Test
    fun mdpdtlWithoutmdpcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033mdpdtl=TEST_BRAD_MDPDTL_UNIVERSAL_LINK")
        assertNull(adms.getEvar(22))
    }

    @Test
    fun olacid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&olacid=TEST_BRAD_OLACID_UNIVERSAL_LINK")
        assertEquals("OLA.TEST_BRAD_OLACID_UNIVERSAL_LINK", adms.getEvar(22))
    }

    @Test
    fun olacidAndOladtl() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&olacid=TEST_BRAD_OLACID_UNIVERSAL_LINK&oladtl=TEST_BRAD_OLADTL_UNIVERSAL_LINK")
        assertEquals("OLA.TEST_BRAD_OLACID_UNIVERSAL_LINK&OLADTL=TEST_BRAD_OLADTL_UNIVERSAL_LINK", adms.getEvar(22))
    }

    @Test
    fun oladtlWithoutOlacid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&oladtl=TEST_BRAD_OLADTL_UNIVERSAL_LINK")
        assertNull(adms.getEvar(22))
    }

    @Test
    fun affcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&affcid=TEST_BRAD_AFFCID_UNIVERSAL_LINK")
        assertEquals("AFF.TEST_BRAD_AFFCID_UNIVERSAL_LINK", adms.getEvar(22))
    }

    @Test
    fun affcidAndafflid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&affcid=TEST_BRAD_AFFCID_UNIVERSAL_LINK&afflid=TEST_BRAD_AFFLID_UNIVERSAL_LINK")
        assertEquals("AFF.TEST_BRAD_AFFCID_UNIVERSAL_LINK&AFFLID=TEST_BRAD_AFFLID_UNIVERSAL_LINK", adms.getEvar(22))
    }

    @Test
    fun afflidWithoutAffcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&afflid=TEST_BRAD_AFFLID_UNIVERSAL_LINK")
        assertNull(adms.getEvar(22))
    }

    @Test
    fun icmcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&icmcid=TEST_BRAD_ICMCID_UNIVERSAL_LINK")
        assertEquals("ICM.TEST_BRAD_ICMCID_UNIVERSAL_LINK", adms.getEvar(22))
    }

    @Test
    fun icmcidAndIcmdtl() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&icmcid=TEST_BRAD_ICMCID_UNIVERSAL_LINK&icmdtl=TEST_BRAD_ICMDTL_UNIVERSAL_LINK")
        assertEquals("ICM.TEST_BRAD_ICMCID_UNIVERSAL_LINK&ICMDTL=TEST_BRAD_ICMDTL_UNIVERSAL_LINK", adms.getEvar(22))
    }

    @Test
    fun icmdtlWithoutIcmcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&icmdtl=TEST_BRAD_ICMDTL_UNIVERSAL_LINK")
        assertNull(adms.getEvar(22))
    }

    @Test
    fun gclid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?startDate=12/27/2017&endDate=01/03/2018&regionId=602231&gclid=SEMGCLID_KRABI_TEST_GCLID")
        assertEquals("SEMGCLID_KRABI_TEST_GCLID", adms.getEvar(26))
    }

    @Test
    fun gclidAndSemcid() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?startDate=12/27/2017&endDate=01/03/2018&regionId=602231&semcid=SEM_KRABI_TEST_GCLID&gclid=SEMGCLID_KRABI_TEST_GCLID")
        assertEquals("SEMGCLID_KRABI_TEST_GCLID", adms.getEvar(26))
        assertEquals("SEM.SEM_KRABI_TEST_GCLID", adms.getEvar(22))
    }

    private fun trackDeepLink(url :String) {
        // This is what sets the omniture variables we're interested in testing
        setIntentOnActivity(deepLinkRouterActivityController, url)
        deepLinkRouterActivityController.setup()
        // We need to kick off any event to actually set the omniture variables.
        // We could actually parse/use the tested deeplink, but just picking an arbitrary event is easier and doesn't affect our tested variables
        OmnitureTracking.trackAccountPageLoad()
    }

    private fun createSystemUnderTest(): ActivityController<DeepLinkRouterActivity> {
        val deepLinkRouterActivityController = Robolectric.buildActivity(DeepLinkRouterActivity::class.java)
        return deepLinkRouterActivityController
    }

    private fun setIntentOnActivity(deepLinkRouterActivityController: ActivityController<DeepLinkRouterActivity>, sharedItinUrl: String) {
        val uri = Uri.parse(sharedItinUrl)
        val intent = Intent("", uri)
        deepLinkRouterActivityController.withIntent(intent)
    }

}
