package com.expedia.bookings.activity

import android.content.Intent
import android.net.Uri
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.ui.NewPhoneLaunchActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.util.ActivityController
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class DeepLinkRouterActivityTest {

    @Test
    fun sharedItinDeeplink() {
        val deepLinkRouterActivityController = createSystemUnderTest()
        val mockItineraryManager = createMockItineraryManager()
        val deepLinkRouterActivity = deepLinkRouterActivityController.get()

        deepLinkRouterActivity.mockItineraryManager = mockItineraryManager
        val sharedItinUrl = "https://www.expedia.com/m/trips/shared/0y5Ht7LVY1gqSwdrngvC0MCAdQKn-QHMK5hNDlKKtt6jwSkXTR2TnYs9xISPHASFzitz_Tty083fguArrsJbxx6j"
        setIntentOnActivity(deepLinkRouterActivityController, sharedItinUrl)

        deepLinkRouterActivityController.setup()

        Mockito.verify(mockItineraryManager).fetchSharedItin(Mockito.eq(sharedItinUrl))
        assertPhoneLaunchActivityStarted(deepLinkRouterActivity)
    }

    private fun assertPhoneLaunchActivityStarted(deepLinkRouterActivity: TestDeepLinkRouterActivity) {
        val nextStartedActivity = Shadows.shadowOf(deepLinkRouterActivity).peekNextStartedActivity()
        val expectedIntent = Intent(deepLinkRouterActivity, NewPhoneLaunchActivity::class.java)
        expectedIntent.putExtra(PhoneLaunchActivity.ARG_FORCE_SHOW_ITIN, true)
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

    private fun setIntentOnActivity(deepLinkRouterActivityController: ActivityController<TestDeepLinkRouterActivity>, sharedItinUrl: String) {
        val uri = Uri.parse(sharedItinUrl)
        val intent = Intent("", uri)
        deepLinkRouterActivityController.withIntent(intent)
    }

    class TestDeepLinkRouterActivity() : DeepLinkRouterActivity() {

        lateinit var mockItineraryManager: ItineraryManager

        override fun getItineraryManagerInstance(): ItineraryManager {
            return mockItineraryManager
        }
    }

}