package com.expedia.bookings.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.view.View
import android.widget.TextView
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.itin.ItinPageUsableTracking
import com.expedia.bookings.itin.activity.NewAddGuestItinActivity
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.FrameLayout
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.stubbing.Answer
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.mockito.Mockito.`when` as whenever


@RunWith(RobolectricRunner::class)
class ItinItemListFragmentTest {

    private lateinit var activity: ExpediaSupportFragmentTestUtil.FragmentUtilActivity
    private lateinit var sut: TestableItinItemListFragment
    private lateinit var mockItinManager: ItineraryManager
    private lateinit var mockPageUsableTracking: ItinPageUsableTracking

    @Before
    fun before() {
        activity = Robolectric.buildActivity(ExpediaSupportFragmentTestUtil.FragmentUtilActivity::class.java).create().start().resume().visible().get()
        mockItinManager = Mockito.mock(ItineraryManager::class.java)
        mockPageUsableTracking = Mockito.spy(ItinPageUsableTracking())
        Mockito.doAnswer(Answer<String?> {
            if (it.callRealMethod() != null) {
                return@Answer "0.10"
            } else {
                return@Answer null
            }
        }).`when`(mockPageUsableTracking).getLoadTimeInSeconds()

        sut = TestableItinItemListFragment(mockItinManager, mockPageUsableTracking)

        ExpediaSupportFragmentTestUtil.startFragment(activity.supportFragmentManager, sut)
    }

    @Test
    fun hideLoadingViewWhenTripUpdated() {
        sut.showDeepRefreshLoadingView(true)
        assertLoadingViewVisible()

        sut.onTripUpdated(Trip())
        assertLoadingViewVisible(visible = false)
    }

    @Test
    fun showLoadingViewWhenTripUpdated() {
        sut.showDeepRefreshLoadingView(true)
        assertLoadingViewVisible()

        sut.onTripUpdateFailed(Trip())
        assertLoadingViewVisible(visible = false)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.AIRASIAGO, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS, MultiBrand.CHEAPTICKETS, MultiBrand.WOTIF, MultiBrand.MRJET, MultiBrand.TRAVELOCITY))
    fun reviewPromptOnlyShowsOnce() {
        SettingUtils.save(activity, R.string.preference_user_has_booked_hotel_or_flight, true)
        sut.showUserReview()

        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertEquals(true, alertDialog.isShowing)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.AIRASIAGO, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS, MultiBrand.CHEAPTICKETS, MultiBrand.WOTIF, MultiBrand.MRJET, MultiBrand.TRAVELOCITY))
    fun reviewPromptTextIsCorrect() {
        SettingUtils.save(activity, R.string.preference_user_has_booked_hotel_or_flight, true)
        sut.showUserReview()

        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertEquals(true, alertDialog.isShowing)
        assertEquals("Love Our App?", getDialogText(alertDialog, R.id.title_text))
        assertEquals("Rate App", getDialogText(alertDialog, R.id.review_btn))
        assertEquals("Email App Support", getDialogText(alertDialog, R.id.feedback_btn))
        assertEquals("No Thanks", getDialogText(alertDialog, R.id.no_btn))
    }

    @Test
    fun testShowCorrectAddGuestItinActivity() {
        sut.showAddGuestItinScreen()
        val startedIntent = Shadows.shadowOf(activity).nextStartedActivity
        assertIntentForActivity(NewAddGuestItinActivity::class.java, startedIntent)
    }

    @Test
    fun testManuallyAddGuestItinView() {
        val addGuestItinIntroText = activity.findViewById(R.id.add_guest_itin_intro_text) as com.expedia.bookings.widget.TextView
        assertEquals("Checked out without signing in? Find your trip by itinerary number.", addGuestItinIntroText.text)

        val addGuestItinButton = activity.findViewById(R.id.add_guest_itin_text_view) as com.expedia.bookings.widget.TextView
        assertEquals("Manually add guest booked trip", addGuestItinButton.text)
        addGuestItinButton.performClick()
        val startedIntent = Shadows.shadowOf(activity).nextStartedActivity
        assertIntentForActivity(NewAddGuestItinActivity::class.java, startedIntent)
    }

    @Test
    fun testManuallyAddGuestSignedOffItinView() {
        val addGuestItinIntroText = activity.findViewById(R.id.add_guest_itin_intro_text_view) as com.expedia.bookings.widget.TextView
        assertEquals("Checked out without signing in? Find your trip by itinerary number.", addGuestItinIntroText.text)

        val addGuestItinButton = activity.findViewById(R.id.add_guest_itin_text_button) as com.expedia.bookings.widget.TextView
        assertEquals("Manually add guest booked trip", addGuestItinButton.text)
        addGuestItinButton.performClick()
        val startedIntent = Shadows.shadowOf(activity).nextStartedActivity
        assertIntentForActivity(NewAddGuestItinActivity::class.java, startedIntent)
    }

    @Test
    fun fragmentVisibleToUserWithTripsIsTracked() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        givenItinsHaveBeenLoadedAlready()
        mockPageUsableTracking.markSuccessfulStartTime(System.currentTimeMillis())
        sut.userVisibleHint = true

        OmnitureTestUtils.assertStateTrackedWithEvents("App.Itinerary", "event63,event220,event221=0.10", mockAnalyticsProvider)
    }

    @Test
    fun fragmentVisibleToUserWithoutTripsIsNotTracked() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        givenNoAvailableItins()
        mockPageUsableTracking.markSuccessfulStartTime(System.currentTimeMillis())
        sut.userVisibleHint = true

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    @Test
    fun fragmentVisibleToUserWithoutStarTimeIsNotTracked() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        givenItinsHaveBeenLoadedAlready()
        sut.userVisibleHint = true

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    @Test
    fun fragmentVisibleToUserWithoutTripsPreservesStartTime() {
        givenNoAvailableItins()

        assertFalse(mockPageUsableTracking.hasStartTime())
        mockPageUsableTracking.markSuccessfulStartTime(System.currentTimeMillis())
        assertTrue(mockPageUsableTracking.hasStartTime())

        sut.userVisibleHint = true

        assertTrue(mockPageUsableTracking.hasStartTime())
    }

    private fun givenItinsHaveBeenLoadedAlready() {
        whenever(mockItinManager.itinCardData).thenReturn(listOf(ItinCardData(TripComponent())))
    }

    private fun givenNoAvailableItins() {
        whenever(mockItinManager.itinCardData).thenReturn(emptyList())
    }

    private fun assertLoadingViewVisible(visible: Boolean = true) {
        assertEquals(getLoadingView().visibility, if (visible) View.VISIBLE else View.GONE)
    }

    private fun assertIntentForActivity(expectedActivityClass: Class<*>, startedIntent: Intent) {
        assertEquals(expectedActivityClass.name, startedIntent.component.className)
    }

    private fun getLoadingView(): FrameLayout =
            sut.view?.findViewById(R.id.deep_refresh_loading_layout) as FrameLayout

    private fun getDialogText(alertDialog: AlertDialog, id: Int): String =
            (alertDialog.findViewById(id) as TextView).text.toString()

    @SuppressLint("ValidFragment")
    class TestableItinItemListFragment(val mockItinManager: ItineraryManager, val mockPageUsableTracking: ItinPageUsableTracking) : ItinItemListFragment() {
        override fun getItineraryManager(): ItineraryManager = mockItinManager
        override fun getItinPageUsableTracking(): ItinPageUsableTracking? = mockPageUsableTracking
    }
}
