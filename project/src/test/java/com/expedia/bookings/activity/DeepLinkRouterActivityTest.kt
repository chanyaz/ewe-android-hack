package com.expedia.bookings.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.DeprecatedHotelSearchParams
import com.expedia.bookings.data.FlightSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.features.Feature
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.packages.activity.PackageActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.FlightsV2DataUtil
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.ui.FlightActivity
import com.expedia.ui.HotelActivity
import com.expedia.util.ForceBucketPref
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class DeepLinkRouterActivityTest {

    val context: Context = RuntimeEnvironment.application

    @Test
    fun hotelSearchDeepLink() {
        val hotelSearchUrl = "${BuildConfig.DEEPLINK_SCHEME}://hotelSearch"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(hotelSearchUrl)

        val params = DeprecatedHotelSearchParams()
        val v2params = HotelsV2DataUtil.getHotelV2SearchParams(context, params)
        val gson = HotelsV2DataUtil.generateGson()

        val startedIntent = Shadows.shadowOf(deepLinkRouterActivity).peekNextStartedActivity()
        assertIntentForActivity(HotelActivity::class.java, startedIntent)
        assertBooleanExtraEquals(true, Codes.FROM_DEEPLINK, startedIntent)
        assertStringExtraEquals(gson.toJson(v2params), HotelExtras.EXTRA_HOTEL_SEARCH_PARAMS, startedIntent)
        assertBooleanExtraEquals(true, Codes.TAG_EXTERNAL_SEARCH_PARAMS, startedIntent)
    }

    @Test
    fun retrieveCarnivalMessageIsCalledOnPushNotificationTap() {
        val homeUrl = "${BuildConfig.DEEPLINK_SCHEME}://home"

        val intent = createIntent(homeUrl)

        val bundle = Bundle()
        bundle.putBoolean("carnival", true)
        intent.putExtras(bundle)

        val deepLinkRouterActivityController = createSystemUnderTestWithIntent(intent)
        val deepLinkRouterActivity = deepLinkRouterActivityController.get()
        deepLinkRouterActivityController.setup()

        assertEquals(1, deepLinkRouterActivity.retrieveCarnivalMessageCount)
    }

    private fun assertBooleanExtraEquals(expected: Boolean, extraName: String, startedIntent: Intent) {
        assertEquals(expected, startedIntent.getBooleanExtra(extraName, !expected))
    }

    private fun assertStringExtraEquals(expected: String, extraName: String, startedIntent: Intent) {
        assertEquals(expected, startedIntent.getStringExtra(extraName))
    }

    private fun assertIntentForActivity(expectedActivityClass: Class<*>, startedIntent: Intent) {
        assertEquals(expectedActivityClass.name, startedIntent.component.className)
    }

    @Test
    fun flightSearchDeepLink() {
        val hotelSearchUrl = "${BuildConfig.DEEPLINK_SCHEME}://flightSearch"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(hotelSearchUrl)

        val params = FlightSearchParams()
        val gson = FlightsV2DataUtil.generateGson()

        val startedIntent = Shadows.shadowOf(deepLinkRouterActivity).peekNextStartedActivity()
        assertIntentForActivity(FlightActivity::class.java, startedIntent)
        assertStringExtraEquals(gson.toJson(params), Codes.SEARCH_PARAMS, startedIntent)
    }

    @Test
    fun homeDeepLink() {
        val homeUrl = "${BuildConfig.DEEPLINK_SCHEME}://home"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(homeUrl)

        val startedIntent = Shadows.shadowOf(deepLinkRouterActivity).peekNextStartedActivity()

        assertIntentForActivity(PhoneLaunchActivity::class.java, startedIntent)
        assertBooleanExtraEquals(true, PhoneLaunchActivity.ARG_FORCE_SHOW_WATERFALL, startedIntent)
    }

    @Test
    fun webDeepLinkWithFeatureOn() {
        val url = "https://www." + PointOfSale.getPointOfSale().getUrl() + "/mobile/deeplink/mobileTest"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(url, true)
        val startedIntent = Shadows.shadowOf(deepLinkRouterActivity).peekNextStartedActivity()
        assertIntentForActivity(DeepLinkWebViewActivity::class.java, startedIntent)
        assertBooleanExtraEquals(true, WebViewActivity.ARG_USE_WEB_VIEW_TITLE, startedIntent)
    }

    @Test
    fun webDeepLinkWithFeatureOff() {
        val url = "https://www." + PointOfSale.getPointOfSale().getUrl() + "/mobile/deeplink/mobileTest"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(url, false)

        val startedIntent = Shadows.shadowOf(deepLinkRouterActivity).peekNextStartedActivity()

        assertIntentForActivity(PhoneLaunchActivity::class.java, startedIntent)
        assertBooleanExtraEquals(true, PhoneLaunchActivity.ARG_FORCE_SHOW_WATERFALL, startedIntent)
    }

    @Test
    fun forceBucketDeepLink() {
        val forceBucketUrl = "${BuildConfig.DEEPLINK_SCHEME}://forceBucket?key=1111&value=0"
        getDeepLinkRouterActivity(forceBucketUrl)
        assertEquals(0, ForceBucketPref.getForceBucketedTestValue(context, 1111, -1))
    }

    @Test
    fun memberPricingDeepLink() {
        val memberPricingUrl = "https://www." + PointOfSale.getPointOfSale().getUrl() + "/mobile/deeplink/member-pricing"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(memberPricingUrl)
        assertEquals(1, deepLinkRouterActivity.memberPricingCount)
    }

    @Test
    fun signInDeepLink() {
        val signInUrl = "${BuildConfig.DEEPLINK_SCHEME}://signIn"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(signInUrl)
        assertEquals(1, deepLinkRouterActivity.signInCallsCount)
    }

    @Test
    fun signInDeepLinkVariations() {
        val signInUrl = "https://www." + PointOfSale.getPointOfSale().getUrl() + "/mobile/deeplink/anything/signin"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(signInUrl)
        assertEquals(1, deepLinkRouterActivity.signInCallsCount)
    }

    @Test
    fun supportEmailDeepLink() {
        val supportEmailUrl = "${BuildConfig.DEEPLINK_SCHEME}://supportEmail"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(supportEmailUrl)
        assertEquals(1, deepLinkRouterActivity.supportEmailCallsCount)
    }

    @Test
    fun sharedItineraryDeepLink() {
        val sharedItinUrl = "https://www." + PointOfSale.getPointOfSale().getUrl() + "/m/trips/shared/0y5Ht7LVY1gqSwdrngvC0MCAdQKn-QHMK5hNDlKKtt6jwSkXTR2TnYs9xISPHASFzitz_Tty083fguArrsJbxx6j"
        val deepLinkRouterActivityController = createSystemUnderTestWithIntent(createIntent(sharedItinUrl))
        val mockItineraryManager = createMockItineraryManager()
        val deepLinkRouterActivity = deepLinkRouterActivityController.get()

        deepLinkRouterActivity.mockItineraryManager = mockItineraryManager

        deepLinkRouterActivityController.setup()
        Mockito.verify(mockItineraryManager).fetchSharedItin(Mockito.eq(sharedItinUrl))
        assertPhoneLaunchActivityStartedToItin(deepLinkRouterActivity, null)
    }

    @Test
    fun shortUrlDeepLink() {
        val shortUrl = "https://" + ProductFlavorFeatureConfiguration.getInstance().getHostnameForShortUrl() + "/0y5Ht7LVY1gqSwdrngvC0MCAdQKn"
        val sharedItinUrl = "https://www." + PointOfSale.getPointOfSale().getUrl() + "/m/trips/shared/0y5Ht7LVY1gqSwdrngvC0MCAdQKn-QHMK5hNDlKKtt6jwSkXTR2TnYs9xISPHASFzitz_Tty083fguArrsJbxx6j"
        val deepLinkRouterActivityController = createSystemUnderTestWithIntent(createIntent(shortUrl))
        val mockItineraryManager = createMockItineraryManager()
        val deepLinkRouterActivity = deepLinkRouterActivityController.get()

        deepLinkRouterActivity.mockItineraryManager = mockItineraryManager
        deepLinkRouterActivityController.setup()
        Mockito.verify(mockItineraryManager).fetchSharedItin(Mockito.eq(sharedItinUrl))
        assertPhoneLaunchActivityStartedToItin(deepLinkRouterActivity, null)
    }

    @Test
    fun tripDeepLink() {
        val tripUrl = "${BuildConfig.DEEPLINK_SCHEME}://trips?itinNum=7238447666975"
        val deepLinkRouterActivityController = createSystemUnderTestWithIntent(createIntent(tripUrl))
        val mockItineraryManager = createMockItineraryManager()
        val deepLinkRouterActivity = deepLinkRouterActivityController.get()

        deepLinkRouterActivity.mockItineraryManager = mockItineraryManager

        deepLinkRouterActivityController.setup()
        assertPhoneLaunchActivityStartedToItin(deepLinkRouterActivity, "7238447666975")
    }

    @Test
    fun packageSearchDeeplink() {
        val packageSearchUrl = "${BuildConfig.DEEPLINK_SCHEME}://packageSearch"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(packageSearchUrl)
        val startedIntent = Shadows.shadowOf(deepLinkRouterActivity).peekNextStartedActivity()
        assertIntentForActivity(PackageActivity::class.java, startedIntent)
    }

    private fun getDeepLinkRouterActivity(deepLinkUrl: String, universalWebviewDeepLinkEnabled: Boolean = false): TestDeepLinkRouterActivity {
        val deepLinkRouterActivityController = createSystemUnderTestWithIntent(createIntent(deepLinkUrl))
        val mockItineraryManager = createMockItineraryManager()
        val deepLinkRouterActivity = deepLinkRouterActivityController.get()
        deepLinkRouterActivity.setUniversalWebviewDeepLinkFeature(FakeFeature(universalWebviewDeepLinkEnabled))

        deepLinkRouterActivity.mockItineraryManager = mockItineraryManager
        deepLinkRouterActivityController.setup()

        return deepLinkRouterActivity
    }

    private fun assertPhoneLaunchActivityStartedToItin(deepLinkRouterActivity: TestDeepLinkRouterActivity, itinNum: String?) {
        val startedIntent = Shadows.shadowOf(deepLinkRouterActivity).peekNextStartedActivity()

        assertIntentForActivity(PhoneLaunchActivity::class.java, startedIntent)
        assertBooleanExtraEquals(true, PhoneLaunchActivity.ARG_FORCE_SHOW_ITIN, startedIntent)
        if (itinNum != null) {
            assertStringExtraEquals(itinNum, PhoneLaunchActivity.ARG_ITIN_NUM, startedIntent)
        }
    }

    private fun createMockItineraryManager(): ItineraryManager =
            Mockito.mock(ItineraryManager::class.java)

    private fun createSystemUnderTestWithIntent(intent: Intent): ActivityController<TestDeepLinkRouterActivity> =
            Robolectric.buildActivity(TestDeepLinkRouterActivity::class.java, intent)

    private fun createIntent(deepLinkUrl: String): Intent {
        val uri = Uri.parse(deepLinkUrl)
        return Intent("", uri)
    }

    class FakeFeature(private val isEnabled: Boolean) : Feature {
        override val name: String
            get() = "someFeature"

        override fun enabled(): Boolean = isEnabled
    }

    class TestDeepLinkRouterActivity : DeepLinkRouterActivity() {

        var signInCallsCount = 0
        var supportEmailCallsCount = 0
        var memberPricingCount = 0
        var retrieveCarnivalMessageCount = 0
        lateinit var mockItineraryManager: ItineraryManager

        override fun startProcessing() {
            handleDeeplink()
        }

        override fun handleMemberPricing() {
            memberPricingCount++
        }

        override fun handleSignIn() {
            signInCallsCount++
        }

        override fun handleSupportEmail() {
            supportEmailCallsCount++
        }

        override fun getItineraryManagerInstance(): ItineraryManager = mockItineraryManager

        override fun getFirebaseDynamicLinksInstance(): FirebaseDynamicLinks? = null

        override fun goFetchSharedItinWithShortUrl(shortUrl: String, runnable: OnSharedItinUrlReceiveListener) {
            runnable.onSharedItinUrlReceiveListener("https://www." + PointOfSale.getPointOfSale().getUrl() + "/m/trips/shared/0y5Ht7LVY1gqSwdrngvC0MCAdQKn-QHMK5hNDlKKtt6jwSkXTR2TnYs9xISPHASFzitz_Tty083fguArrsJbxx6j")
        }

        override fun retrieveCarnivalMessage() {
            retrieveCarnivalMessageCount++
        }
    }
}
