package com.expedia.bookings.itin.activity

import android.content.Intent
import android.text.format.DateUtils
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinDetailsActivityTest {

    private lateinit var activity: FlightItinDetailsActivity
    private lateinit var itinCardData: ItinCardDataFlight
    private val flightBuilder = ItinCardDataFlightBuilder()

    @Before
    fun setup() {
        val intent = Intent()
        intent.putExtra("FLIGHT_ITIN_ID", "FLIGHT_ITIN_ID")
        activity = Robolectric.buildActivity(FlightItinDetailsActivity::class.java, intent).create().get()
        itinCardData = flightBuilder.build()
        activity.viewModel.itinCardDataFlight = itinCardData
    }

    @Test
    fun testShareClickOmniture() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        activity.viewModel.itinCardDataFlight.flightLeg.shareInfo.shortSharableDetailsUrl = "http://e.xp.co"
        activity.toolbarViewModel.shareIconClickedSubject.onNext(Unit)

        OmnitureTestUtils.assertLinkTracked("Itinerary Sharing", "App.Itinerary.Flight.Share.Start", mockAnalyticsProvider)
    }

    @RunForBrands(brands = [(MultiBrand.EXPEDIA)])
    @Test
    fun testShareClickIntent() {
        activity.viewModel.itinCardDataFlight.flightLeg.shareInfo.shortSharableDetailsUrl = "http://e.xp.co"
        activity.toolbarViewModel.shareIconClickedSubject.onNext(Unit)

        val shadowActivity = shadowOf(activity)
        val intent = shadowActivity.peekNextStartedActivityForResult().intent

        val startTime = flightBuilder.startTime
        val formattedStartTime = DateUtils.formatDateTime(activity, startTime.millis, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_NUMERIC_DATE)
        val expectedShareText = "I'm flying to Las Vegas on $formattedStartTime! http://e.xp.co"

        assertEquals(expectedShareText, intent.getStringExtra(Intent.EXTRA_TEXT))
    }
}
