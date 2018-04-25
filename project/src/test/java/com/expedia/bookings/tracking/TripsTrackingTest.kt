package com.expedia.bookings.tracking

import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class TripsTrackingTest {
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testTrackTripListVisit() {
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        TripsTracking.trackTripListVisit(0)
        OmnitureTestUtils.assertStateTracked("App.Trips.Upcoming", OmnitureMatchers.withEventsString("event63"), mockAnalyticsProvider)

        TripsTracking.trackTripListVisit(1)
        OmnitureTestUtils.assertStateTracked("App.Trips.Past", OmnitureMatchers.withEventsString("event63"), mockAnalyticsProvider)

        TripsTracking.trackTripListVisit(2)
        OmnitureTestUtils.assertStateTracked("App.Trips.Cancelled", OmnitureMatchers.withEventsString("event63"), mockAnalyticsProvider)

        TripsTracking.trackTripListVisit(-1)
        OmnitureTestUtils.assertStateTracked("", OmnitureMatchers.withEventsString("event63"), mockAnalyticsProvider)
    }

    @Test
    fun testTrackTripFolderAbTest() {
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        AbacusTestUtils.bucketTests(AbacusUtils.TripFoldersFragment)
        TripsTracking.trackTripFolderAbTest()
        OmnitureTestUtils.assertLinkTrackedWithAbTestExposure("Itinerary Action", "App.Trips", "25538.0.1", mockAnalyticsProvider)

        AbacusTestUtils.unbucketTests(AbacusUtils.TripFoldersFragment)
        TripsTracking.trackTripFolderAbTest()
        OmnitureTestUtils.assertLinkTrackedWithAbTestExposure("Itinerary Action", "App.Trips", "25538.0.0", mockAnalyticsProvider)
    }

    @Test
    fun testTrackHotelItinManageBookingClick() {
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        TripsTracking.trackHotelItinManageBookingClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.ManageBooking", mockAnalyticsProvider)
    }
}
