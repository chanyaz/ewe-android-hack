package com.expedia.bookings.itin.activity

import android.content.Intent
import android.text.format.DateUtils
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManagerInterface
import com.expedia.bookings.itin.flight.details.FlightItinDetailsActivity
import com.expedia.bookings.services.TestObserver
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
    private lateinit var itinCardDataTwo: ItinCardDataFlight
    private val flightBuilder = ItinCardDataFlightBuilder()
    private val itinCardDataSubscriber = TestObserver<ItinCardDataFlight>()

    @Before
    fun setup() {
        val intent = Intent()
        intent.putExtra("FLIGHT_ITIN_ID", "FLIGHT_ITIN_ID")
        activity = Robolectric.buildActivity(FlightItinDetailsActivity::class.java, intent).create().get()
        itinCardData = flightBuilder.build()
        itinCardDataTwo = flightBuilder.build(multiSegment = true)
        activity.toolbarViewModel.itinCardData = itinCardData
    }

    @Test
    fun testShareClickOmniture() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        activity.toolbarViewModel.itinCardData.flightLeg.shareInfo.shortSharableDetailsUrl = "http://e.xp.co"
        activity.toolbarViewModel.shareIconClickedSubject.onNext(Unit)

        OmnitureTestUtils.assertLinkTracked("Itinerary Sharing", "App.Itinerary.Flight.Share.Start", mockAnalyticsProvider)
    }

    @RunForBrands(brands = [(MultiBrand.EXPEDIA)])
    @Test
    fun testShareClickIntent() {
        activity.toolbarViewModel.itinCardData.flightLeg.shareInfo.shortSharableDetailsUrl = "http://e.xp.co"
        activity.toolbarViewModel.shareIconClickedSubject.onNext(Unit)

        val shadowActivity = shadowOf(activity)
        val intent = shadowActivity.peekNextStartedActivityForResult().intent

        val startTime = flightBuilder.startTime
        val formattedStartTime = DateUtils.formatDateTime(activity, startTime.millis, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_NUMERIC_DATE)
        val expectedShareText = "I'm flying to Las Vegas on $formattedStartTime! http://e.xp.co"

        assertEquals(expectedShareText, intent.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun onSyncFinishTest() {
        activity.viewModel.itineraryManager = TestItinManager()
        activity.viewModel.itinCardDataFlightObservable.subscribe(itinCardDataSubscriber)
        itinCardDataSubscriber.assertNoValues()
        activity.onSyncFinish()
        itinCardDataSubscriber.assertValuesAndClear(itinCardData)
        activity.onSyncFinish()
        itinCardDataSubscriber.assertValue(itinCardDataTwo)
    }

    inner class TestItinManager : ItineraryManagerInterface {
        var first = true
        override fun getItinCardDataFromItinId(id: String?): ItinCardData {
            return if (first) {
                first = false
                itinCardData
            } else {
                itinCardDataTwo
            }
        }
    }
}
