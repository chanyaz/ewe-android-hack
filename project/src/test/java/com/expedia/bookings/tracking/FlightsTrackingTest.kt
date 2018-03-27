package com.expedia.bookings.tracking

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.Constants
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class FlightsTrackingTest {
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testShoppingErrorTrackingHasErrorCodeAndAPICall() {
        val errorDetails = ApiCallFailing.FlightSearch(Constants.NO_INTERNET_ERROR_CODE)
        val controlEvar = mapOf(18 to "App.Flight.Shopping.Error")
        val prop36 = mapOf(36 to "NO_INTERNET|FLIGHT_SEARCH")

        FlightsV2Tracking.trackFlightShoppingError(errorDetails)

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withProps(prop36), mockAnalyticsProvider)
    }
}
