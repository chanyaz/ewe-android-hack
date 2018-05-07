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
        assertNoTrackingHasOccurred()

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
        assertNoTrackingHasOccurred()

        AbacusTestUtils.bucketTests(AbacusUtils.TripFoldersFragment)
        TripsTracking.trackTripFolderAbTest()
        assertLinkTrackedWithAbTestExposure("25538.0.1")

        AbacusTestUtils.unbucketTests(AbacusUtils.TripFoldersFragment)
        TripsTracking.trackTripFolderAbTest()
        assertLinkTrackedWithAbTestExposure("25538.0.0")
    }

    @Test
    fun trackItinHotelViewReceiptTest() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinHotelViewReceipt()
        assertItinLinkTracked("App.Itinerary.Hotel.PricingRewards.ViewReceipt")
    }

    @Test
    fun trackHotelItinPricingRewardsClick() {
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        TripsTracking.trackItinHotelViewRewards()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.PricingRewards.ViewRewards", mockAnalyticsProvider)
    }

    @Test
    fun testTrackHotelItinManageBookingClick() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackHotelItinManageBookingClick()
        assertItinLinkTracked("App.Itinerary.Hotel.ManageBooking")
    }

    @Test
    fun testTrackHotelTaxiCardClick() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackHotelTaxiCardClick()
        assertItinLinkTracked("App.Itinerary.Hotel.TaxiCard")
    }

    fun assertItinLinkTracked(rfrrId: String) {
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", rfrrId, mockAnalyticsProvider)
    }

    fun assertNoTrackingHasOccurred() {
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    fun assertLinkTrackedWithAbTestExposure(analyticsString: String, rfrrId: String = "App.Trips") {
        OmnitureTestUtils.assertLinkTrackedWithAbTestExposure("Itinerary Action", rfrrId, analyticsString, mockAnalyticsProvider)
    }
}
