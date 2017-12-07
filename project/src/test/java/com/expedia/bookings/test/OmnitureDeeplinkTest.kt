package com.expedia.bookings.test

import android.content.Intent
import android.net.Uri
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.activity.DeepLinkRouterActivity
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEvars
import com.expedia.bookings.test.OmnitureMatchers.Companion.withoutEvars
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.util.ActivityController


@RunWith(RobolectricRunner::class)
class OmnitureDeeplinkTest {

    lateinit var mockAnalyticsProvider: AnalyticsProvider
    lateinit var deepLinkRouterActivityController: ActivityController<TestDeepLinkRouterActivity>

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun emlcidTrackedInEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&emlcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK")
        assertEvar22TrackedAs("EML.TEST_BRAD_MDPCID_UNIVERSAL_LINK")
    }

    @Test
    fun emlcidKeyIsCaseInsensitive() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&EmlcId=TEST_BRAD_MDPCID_UNIVERSAL_LINK")
        assertEvar22TrackedAs("EML.TEST_BRAD_MDPCID_UNIVERSAL_LINK")
    }

    @Test
    fun emlcidValueCaseIsPreserved() {
        // Lettercase of value should match url parameter
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&emlcid=mIxEd_cAsE")
        assertEvar22TrackedAs("EML.mIxEd_cAsE")
    }

    @Test
    fun semcidTrackedInEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&semcid=TEST_BRAD_SEMCID_UNIVERSAL_LINK")
        assertEvar22TrackedAs("SEM.TEST_BRAD_SEMCID_UNIVERSAL_LINK")
    }

    @Test
    fun brandcidTrackedInEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&brandcid=TEST_BRAD_BRANDCID_UNIVERSAL_LINK")
        assertEvar22TrackedAs("Brand.TEST_BRAD_BRANDCID_UNIVERSAL_LINK")
    }

    @Test
    fun seocidTrackedInEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&seocid=TEST_BRAD_SEOCID_UNIVERSAL_LINK")
        assertEvar22TrackedAs("SEO.TEST_BRAD_SEOCID_UNIVERSAL_LINK")
    }

    @Test
    fun kwordTrackedInEvar15AndEvar22Empty() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?kword=Brads_Super_Duper_Test_App_Links")
        assertEvar22NotTrackedAndEvar15TrackedAs("Brads_Super_Duper_Test_App_Links")
    }

    @Test
    fun semcidTrackedInEvar22AndKwordTrackedInEvar15() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?semcid=TEST_BRAD_KWORDPARAMETER_APP_LINKS&kword=Brads_Super_Duper_Test_App_Links")
        assertEvar15And22TrackedAs("Brads_Super_Duper_Test_App_Links", "SEM.TEST_BRAD_KWORDPARAMETER_APP_LINKS")
    }

    @Test
    fun mdpcidTrackedInEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&mdpcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK")
        assertEvar22TrackedAs("MDP.TEST_BRAD_MDPCID_UNIVERSAL_LINK")
    }

    @Test
    fun mdpcidAndMdpdtlTrackedInEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&mdpcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK&mdpdtl=TEST_BRAD_MDPDTL_UNIVERSAL_LINK")
        assertEvar22TrackedAs("MDP.TEST_BRAD_MDPCID_UNIVERSAL_LINK&MDPDTL=TEST_BRAD_MDPDTL_UNIVERSAL_LINK")
    }

    @Test
    fun mdpdtlWithoutmdpcidTrackedWithoutEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033mdpdtl=TEST_BRAD_MDPDTL_UNIVERSAL_LINK")
        assertEvar22NotTracked()
    }

    @Test
    fun olacidTrackedInEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&olacid=TEST_BRAD_OLACID_UNIVERSAL_LINK")
        assertEvar22TrackedAs("OLA.TEST_BRAD_OLACID_UNIVERSAL_LINK")
    }

    @Test
    fun olacidAndOladtlTrackedInEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&olacid=TEST_BRAD_OLACID_UNIVERSAL_LINK&oladtl=TEST_BRAD_OLADTL_UNIVERSAL_LINK")
        assertEvar22TrackedAs("OLA.TEST_BRAD_OLACID_UNIVERSAL_LINK&OLADTL=TEST_BRAD_OLADTL_UNIVERSAL_LINK")
    }

    @Test
    fun oladtlWithoutOlacidTrackedWithoutEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&oladtl=TEST_BRAD_OLADTL_UNIVERSAL_LINK")
        assertEvar22NotTracked()
    }

    @Test
    fun affcidTrackedInEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&affcid=TEST_BRAD_AFFCID_UNIVERSAL_LINK")
        assertEvar22TrackedAs("AFF.TEST_BRAD_AFFCID_UNIVERSAL_LINK")
    }

    @Test
    fun affcidAndafflidTrackedInEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&affcid=TEST_BRAD_AFFCID_UNIVERSAL_LINK&afflid=TEST_BRAD_AFFLID_UNIVERSAL_LINK")
        assertEvar22TrackedAs("AFF.TEST_BRAD_AFFCID_UNIVERSAL_LINK&AFFLID=TEST_BRAD_AFFLID_UNIVERSAL_LINK")
    }

    @Test
    fun afflidWithoutAffcidTrackedWithoutEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&afflid=TEST_BRAD_AFFLID_UNIVERSAL_LINK")
        assertEvar22NotTracked()
    }

    @Test
    fun icmcidTrackedInEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&icmcid=TEST_BRAD_ICMCID_UNIVERSAL_LINK")
        assertEvar22TrackedAs("ICM.TEST_BRAD_ICMCID_UNIVERSAL_LINK")
    }

    @Test
    fun icmcidAndIcmdtlTrackedInEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&icmcid=TEST_BRAD_ICMCID_UNIVERSAL_LINK&icmdtl=TEST_BRAD_ICMDTL_UNIVERSAL_LINK")
        assertEvar22TrackedAs("ICM.TEST_BRAD_ICMCID_UNIVERSAL_LINK&ICMDTL=TEST_BRAD_ICMDTL_UNIVERSAL_LINK")
    }

    @Test
    fun icmdtlWithoutIcmcidTrackedWithoutEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&icmdtl=TEST_BRAD_ICMDTL_UNIVERSAL_LINK")
        assertEvar22NotTracked()
    }

    @Test
    fun gclidTrackedInEvar26() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?startDate=12/27/2017&endDate=01/03/2018&regionId=602231&gclid=SEMGCLID_KRABI_TEST_GCLID")
        assertEvar26TrackedAs("SEMGCLID_KRABI_TEST_GCLID")
    }

    @Test
    fun gclidTrackedInEvar26AndSemcidTrackedInEvar22() {
        trackDeepLink("https://www.expedia.com/mobile/deeplink/Hotel-Search?startDate=12/27/2017&endDate=01/03/2018&regionId=602231&semcid=SEM_KRABI_TEST_GCLID&gclid=SEMGCLID_KRABI_TEST_GCLID")
        assertEvar22And26TrackedAs("SEM.SEM_KRABI_TEST_GCLID", "SEMGCLID_KRABI_TEST_GCLID")
    }

    private fun trackDeepLink(url :String) {
        // This is what sets the omniture variables we're interested in testing
        deepLinkRouterActivityController = createSystemUnderTestWithIntent(createIntent(url))
        deepLinkRouterActivityController.setup()
        // We need to kick off any event to actually set the omniture variables.
        // We could actually parse/use the tested deeplink, but just picking an arbitrary event is easier and doesn't affect our tested variables
        OmnitureTracking.trackAccountPageLoad()
    }

    private fun createSystemUnderTestWithIntent(intent: Intent): ActivityController<TestDeepLinkRouterActivity> =
            Robolectric.buildActivity(TestDeepLinkRouterActivity::class.java, intent)

    private fun createIntent(sharedItinUrl: String): Intent {
        val uri = Uri.parse(sharedItinUrl)
        return Intent("", uri)
    }

    private fun assertEvar22NotTracked() {
        assertStateTracked(withoutEvars(22), mockAnalyticsProvider)
    }

    private fun assertEvar22TrackedAs(expectedValue: String) {
        assertStateTracked(withEvars(mapOf(22 to expectedValue)), mockAnalyticsProvider)
    }

    private fun assertEvar26TrackedAs(expectedValue: String) {
        assertStateTracked(withEvars(mapOf(26 to expectedValue)), mockAnalyticsProvider)
    }

    private fun assertEvar22And26TrackedAs(expectedValue22: String, expectedValue26: String) {
        assertStateTracked(withEvars(mapOf(22 to expectedValue22, 26 to expectedValue26)), mockAnalyticsProvider)
    }

    private fun assertEvar15And22TrackedAs(expectedValue15: String, expectedValue22: String) {
        assertStateTracked(withEvars(mapOf(15 to expectedValue15, 22 to expectedValue22)), mockAnalyticsProvider)
    }

    private fun assertEvar22NotTrackedAndEvar15TrackedAs(expectedValue15: String) {
        assertStateTracked(
                allOf(withEvars(mapOf(15 to expectedValue15)),
                        withoutEvars(22)),
                mockAnalyticsProvider)
    }

    class TestDeepLinkRouterActivity : DeepLinkRouterActivity() {
        override fun getFirebaseDynamicLinksInstance(): FirebaseDynamicLinks? = null
    }
}
