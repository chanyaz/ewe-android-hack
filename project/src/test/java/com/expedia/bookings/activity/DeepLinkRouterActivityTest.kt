package com.expedia.bookings.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.FlightSearchParams
import com.expedia.bookings.data.HotelSearchParams
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.launch.activity.NewPhoneLaunchActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.FlightsV2DataUtil
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.ui.FlightActivity
import com.expedia.ui.HotelActivity
import com.expedia.util.ForceBucketPref
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.util.ActivityController
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class DeepLinkRouterActivityTest {

    val context: Context = RuntimeEnvironment.application

    @Test
    fun hotelSearchDeepLink() {
        val hotelSearchUrl = "expda://hotelSearch"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(hotelSearchUrl)

        val expectedIntent = Intent(deepLinkRouterActivity, HotelActivity::class.java)
        expectedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        expectedIntent.putExtra(Codes.FROM_DEEPLINK, true)
        val params = HotelSearchParams()
        val v2params = HotelsV2DataUtil.getHotelV2SearchParams(context, params)
        val gson = HotelsV2DataUtil.generateGson()
        expectedIntent.putExtra(HotelActivity.EXTRA_HOTEL_SEARCH_PARAMS, gson.toJson(v2params))
        expectedIntent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true)

        val nextStartedActivity = Shadows.shadowOf(deepLinkRouterActivity).peekNextStartedActivity()
        assertEquals(expectedIntent, nextStartedActivity)
    }

    @Test
    fun flightSearchDeepLink() {
        val hotelSearchUrl = "expda://flightSearch"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(hotelSearchUrl)

        val expectedIntent = Intent(deepLinkRouterActivity, FlightActivity::class.java)
        val params = FlightSearchParams()
        val gson = FlightsV2DataUtil.generateGson()
        expectedIntent.putExtra(Codes.SEARCH_PARAMS, gson.toJson(params))

        val nextStartedActivity = Shadows.shadowOf(deepLinkRouterActivity).peekNextStartedActivity()
        assertEquals(expectedIntent, nextStartedActivity)
    }

    @Test
    fun homeDeepLink() {
        val homeUrl = "expda://home"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(homeUrl)

        val nextStartedActivity = Shadows.shadowOf(deepLinkRouterActivity).peekNextStartedActivity()
        val expectedIntent = Intent(deepLinkRouterActivity, NewPhoneLaunchActivity::class.java)
        expectedIntent.putExtra(NewPhoneLaunchActivity.ARG_FORCE_SHOW_WATERFALL, true)
        assertEquals(expectedIntent, nextStartedActivity)
    }

    @Test
    fun forceBucketDeepLink() {
        val forceBucketUrl = "expda://forceBucket?key=1111&value=0"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(forceBucketUrl)
        assertEquals(0, ForceBucketPref.getForceBucketedTestValue(context, "1111", -1))
    }

    @Test
    fun signInDeepLink() {
        val signInUrl = "expda://signIn"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(signInUrl)
        assertEquals(1, deepLinkRouterActivity.signInCallsCount)
    }

    @Test
    fun supportEmailDeepLink() {
        val supportEmailUrl = "expda://supportEmail"
        val deepLinkRouterActivity = getDeepLinkRouterActivity(supportEmailUrl)
        assertEquals(1, deepLinkRouterActivity.supportEmailCallsCount)
    }

    @Test
    fun sharedItineraryDeepLink() {
        val deepLinkRouterActivityController = createSystemUnderTest()
        val mockItineraryManager = createMockItineraryManager()
        val deepLinkRouterActivity = deepLinkRouterActivityController.get()

        deepLinkRouterActivity.mockItineraryManager = mockItineraryManager
        val sharedItinUrl = "https://www.expedia.com/m/trips/shared/0y5Ht7LVY1gqSwdrngvC0MCAdQKn-QHMK5hNDlKKtt6jwSkXTR2TnYs9xISPHASFzitz_Tty083fguArrsJbxx6j"
        setIntentOnActivity(deepLinkRouterActivityController, sharedItinUrl)
        deepLinkRouterActivityController.setup()
        Mockito.verify(mockItineraryManager).fetchSharedItin(Mockito.eq(sharedItinUrl))
        assertPhoneLaunchActivityStartedToItin(deepLinkRouterActivity)
    }

    @Test
    fun shortUrlDeepLink() {
        val deepLinkRouterActivityController = createSystemUnderTest()
        val mockItineraryManager = createMockItineraryManager()
        val deepLinkRouterActivity = deepLinkRouterActivityController.get()

        deepLinkRouterActivity.mockItineraryManager = mockItineraryManager
        val shortUrl = "http://e.xpda.co/0y5Ht7LVY1gqSwdrngvC0MCAdQKn"
        val sharedItinUrl = "http://www.expedia.com/m/trips/shared/0y5Ht7LVY1gqSwdrngvC0MCAdQKn-QHMK5hNDlKKtt6jwSkXTR2TnYs9xISPHASFzitz_Tty083fguArrsJbxx6j"
        setIntentOnActivity(deepLinkRouterActivityController, shortUrl)
        deepLinkRouterActivityController.setup()
        Mockito.verify(mockItineraryManager).fetchSharedItin(Mockito.eq(sharedItinUrl))
        assertPhoneLaunchActivityStartedToItin(deepLinkRouterActivity)
    }

    @Test
    fun tripDeepLink() {
        val deepLinkRouterActivityController = createSystemUnderTest()
        val mockItineraryManager = createMockItineraryManager()
        val deepLinkRouterActivity = deepLinkRouterActivityController.get()

        deepLinkRouterActivity.mockItineraryManager = mockItineraryManager

        val tripUrl = "expda://trips?itinNum=7238447666975"

        setIntentOnActivity(deepLinkRouterActivityController, tripUrl)
        deepLinkRouterActivityController.setup()
        Mockito.verify(mockItineraryManager).getDeepLinkItinIdByTripNumber(Mockito.eq("7238447666975"))
        assertPhoneLaunchActivityStartedToItin(deepLinkRouterActivity)
    }

    private fun getDeepLinkRouterActivity(deepLinkUrl : String): TestDeepLinkRouterActivity {
        val deepLinkRouterActivityController = createSystemUnderTest()
        val mockItineraryManager = createMockItineraryManager()
        val deepLinkRouterActivity = deepLinkRouterActivityController.get()

        deepLinkRouterActivity.mockItineraryManager = mockItineraryManager
        setIntentOnActivity(deepLinkRouterActivityController, deepLinkUrl)
        deepLinkRouterActivityController.setup()

        return deepLinkRouterActivity
    }

    private fun assertPhoneLaunchActivityStartedToItin(deepLinkRouterActivity: TestDeepLinkRouterActivity) {
        val nextStartedActivity = Shadows.shadowOf(deepLinkRouterActivity).peekNextStartedActivity()
        val expectedIntent = Intent(deepLinkRouterActivity, NewPhoneLaunchActivity::class.java)
        expectedIntent.putExtra(NewPhoneLaunchActivity.ARG_FORCE_SHOW_ITIN, true)
        assertEquals(expectedIntent, nextStartedActivity)
    }

    private fun createMockItineraryManager(): ItineraryManager {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        return mockItineraryManager
    }

    private fun createSystemUnderTest(): ActivityController<TestDeepLinkRouterActivity> {
        val deepLinkRouterActivityController = Robolectric.buildActivity(TestDeepLinkRouterActivity::class.java)
        return deepLinkRouterActivityController
    }

    private fun setIntentOnActivity(deepLinkRouterActivityController: ActivityController<TestDeepLinkRouterActivity>, deepLinkUrl: String) {
        val uri = Uri.parse(deepLinkUrl)
        val intent = Intent("", uri)
        deepLinkRouterActivityController.withIntent(intent)
    }

    class TestDeepLinkRouterActivity() : DeepLinkRouterActivity() {

        var signInCallsCount = 0
        var supportEmailCallsCount = 0
        lateinit var mockItineraryManager: ItineraryManager

        override fun handleSignIn() {
            signInCallsCount++
        }

        override fun handleSupportEmail() {
            supportEmailCallsCount++
        }

        override fun getItineraryManagerInstance(): ItineraryManager {
            return mockItineraryManager
        }

        override fun goFetchSharedItinWithShortUrl(shortUrl: String, runnable: OnSharedItinUrlReceiveListener) {
            runnable.onSharedItinUrlReceiveListener("http://www.expedia.com/m/trips/shared/0y5Ht7LVY1gqSwdrngvC0MCAdQKn-QHMK5hNDlKKtt6jwSkXTR2TnYs9xISPHASFzitz_Tty083fguArrsJbxx6j")
        }
    }
}