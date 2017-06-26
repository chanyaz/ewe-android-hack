package com.expedia.bookings.test

import android.content.Intent
import android.net.Uri
import com.adobe.adms.measurement.ADMS_Measurement
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
    fun verifyTrackingCalledOnDeepLink() {
        val url = "https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=178307&langid=1033&emlcid=TEST_BRAD_MDPCID_UNIVERSAL_LINK"
        // This is what sets the omniture variables we're interested in testing
        setIntentOnActivity(deepLinkRouterActivityController, url)
        deepLinkRouterActivityController.setup()
        // We need to kick off any event to actually set the omniture variables.
        // We could actually parse/use the tested deeplink, but just picking an arbitrary event is easier and doesn't affect our tested variables
        OmnitureTracking.trackAccountPageLoad()
        assertEquals("EML.TEST_BRAD_MDPCID_UNIVERSAL_LINK", adms.getEvar(22))
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
