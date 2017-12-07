package com.expedia.bookings.tracking

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.FlightTestUtil
import com.expedia.bookings.test.robolectric.FlightTestUtil.Companion.getFlightCreateTripResponse
import com.expedia.bookings.test.robolectric.FlightTestUtil.Companion.getFlightItinDetailsResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.hotel.PageUsableData
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class OmnitureTrackingFlightTest {
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        setupDb()
    }

    @Test
    fun testTrackWebFlightConfirmation() {
        val pageUsableData = PageUsableData()
        pageUsableData.markPageLoadStarted(10000)
        pageUsableData.markAllViewsLoaded(10000)
        OmnitureTracking.trackWebFlightCheckoutConfirmation(getFlightItinDetailsResponse(), pageUsableData)
        val expectedEvars = mapOf(18 to "App.Flight.Checkout.Confirmation",
                50 to "app.phone.android")
        val expectedProps = mapOf(2 to "Flight",
                3 to "SFO",
                4 to "DTW",
                8 to "5678|1234",
                71 to "5678",
                72 to "111111")
        val expectedProducts = ";Flight:null:OW;2;223.00;;eVar30=Merchant:FLT:SFO-DTW"
        val expectedEvents = "purchase,event220,event221=0.00"
        assertWebFlightConfirmationStateTracked(expectedEvars, expectedProps, expectedProducts, expectedEvents)
    }

    private fun setupDb() {
        Db.setFlightSearchParams(FlightTestUtil.getFlightSearchParams(isRoundTrip = false, includeChild = false))
        val flightTripItem = TripBucketItemFlightV2(getFlightCreateTripResponse())
        Db.getTripBucket().add(flightTripItem)
    }

    private fun assertWebFlightConfirmationStateTracked(expectedEvars: Map<Int, String>, expectedProps: Map<Int, String>, expectedProducts: String, expectedEvents: String) {
        OmnitureTestUtils.assertStateTracked("App.Flight.Checkout.Confirmation", OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked("App.Flight.Checkout.Confirmation", OmnitureMatchers.withProps(expectedProps), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked("App.Flight.Checkout.Confirmation", OmnitureMatchers.withProductsString(expectedProducts, shouldExactlyMatch = false), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked("App.Flight.Checkout.Confirmation", OmnitureMatchers.withEventsString(expectedEvents), mockAnalyticsProvider)
    }
}
