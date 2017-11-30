package com.expedia.bookings.itin.activity

import android.content.Intent
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.ItinContentGenerator
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinDetailsActivityTest {

    lateinit private var activity: FlightItinDetailsActivity
    lateinit private var itinCardData: ItinCardDataFlight

    @Before
    fun setup() {
        val intent = Intent()
        intent.putExtra("FLIGHT_ITIN_ID", "FLIGHT_ITIN_ID")
        activity = Robolectric.buildActivity(FlightItinDetailsActivity::class.java, intent).create().get()
        itinCardData = ItinCardDataFlightBuilder().build()
        activity.viewModel.itinCardDataFlight = itinCardData
    }

    @Test
    fun testShareClickOmniture() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        activity.toolbarViewModel.shareIconClickedSubject.onNext(Unit)

        OmnitureTestUtils.assertLinkTracked("Itinerary Sharing", "App.Itinerary.Flight.Share.Start", mockAnalyticsProvider)
    }

    @Test
    fun testShareClickIntent() {
        activity.toolbarViewModel.shareIconClickedSubject.onNext(Unit)

        val shadowActivity = shadowOf(activity)
        val intent = shadowActivity.peekNextStartedActivityForResult().intent
        val mItinContentGenerator = ItinContentGenerator.createGenerator(activity, itinCardData)
        assertEquals(mItinContentGenerator.shareTextShort, intent.getStringExtra(Intent.EXTRA_TEXT))
    }
}