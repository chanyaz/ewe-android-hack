package com.expedia.bookings.launch.widget

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.vm.launch.ActiveItinViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(sdk = intArrayOf(21), shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class ItinLaunchCardTest {

    val activity = Robolectric.buildActivity(Activity::class.java).create().get()
    private lateinit var sut: ItinLaunchCard

    @Test
    fun bindFirstLineSecondLineSignedIn() {
        val expectedFirstLine = "You Have An Upcoming Trip!"
        val expectedSecondLine = "Access your itineraries on the go and stay up to date on changes"

        createSystemUnderTest()
        givenCustomerSignedIn()
        val activeItinViewModel = ActiveItinViewModel(expectedFirstLine, expectedSecondLine)
        sut.bind(activity, activeItinViewModel)

        assertEquals(expectedFirstLine, sut.firstLine.text)
        assertEquals(expectedSecondLine, sut.secondLine.text)
    }

    @Test
    fun onClickItinerariesViewShown() {
        val shadowActivity = Shadows.shadowOf(activity)
        createSystemUnderTest()

        sut.itemView.performClick()

        val intentResult = shadowActivity.nextStartedActivity
        val intentExtras = intentResult.extras
        assertTrue(intentExtras.getBoolean(PhoneLaunchActivity.ARG_FORCE_SHOW_ITIN), "itinerary list view should have launched")
    }

    private fun createSystemUnderTest() {
        activity.setTheme(R.style.LaunchTheme)
        val itemView = LayoutInflater.from(activity).inflate(R.layout.launch_active_itin, null)
        val itinLaunchCard = ItinLaunchCard(itemView, activity)
        sut = itinLaunchCard
    }

    private fun givenCustomerSignedIn() {
        val mockUser = UserLoginTestUtil.mockUser()
        UserLoginTestUtil.setupUserAndMockLogin(mockUser, activity)
    }
}
