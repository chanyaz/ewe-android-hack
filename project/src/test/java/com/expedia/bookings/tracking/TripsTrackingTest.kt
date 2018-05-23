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

    @Test
    fun testTrackItinEmpty() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinEmpty()
        OmnitureTestUtils.assertStateTracked("App.Itinerary.Empty", OmnitureMatchers.withEvars(mapOf(18 to "App.Itinerary.Empty")), mockAnalyticsProvider)
    }

    @Test
    fun testTrackItinError() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinError()
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(mapOf(18 to "App.Itinerary.Error")), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withProps(mapOf(36 to "itin:unable to retrieve trip summary")), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEventsString("event98"), mockAnalyticsProvider)
    }

    @Test
    fun testTrackFindGuestItin() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackFindGuestItin()
        OmnitureTestUtils.assertStateTracked("App.Itinerary.Find.Guest", OmnitureMatchers.withEvars(mapOf(18 to "App.Itinerary.Find.Guest")), mockAnalyticsProvider)
    }

    @Test
    fun testTrackItinChangePOS() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinChangePOS()
        assertItinLinkTracked("App.Itinerary.POSa")
    }

    @Test
    fun testTrackItinSignIn() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinSignIn()
        assertItinLinkTracked("App.Itinerary.Login.Start")
    }

    @Test
    fun testTrackItinRefresh() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinRefresh()
        assertItinLinkTracked("App.Itinerary.User.Refresh")
    }

    @Test
    fun testTrackAddGuestItin() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinGuestAdd()
        assertItinLinkTracked("App.Itinerary.Guest.Itin")
    }

    @Test
    fun trackItinRedeemVoucherTest() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinLxRedeemVoucher()
        assertItinLinkTracked("App.Itinerary.Activity.Redeem")
    }

    @Test
    fun trackItinLxDetailsMapTest() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinLxDetailsMap()
        assertItinLinkTracked("App.Itinerary.Activity.Map")
    }

    @Test
    fun trackItinLxDetailsDirectionsTest() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinLxDetailsDirections()
        assertItinLinkTracked("App.Itinerary.Activity.Directions")
    }

    @Test
    fun testTrackItinLxCallSupportClicked() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinLxCallSupportClicked()
        assertItinLinkTracked("App.Itinerary.Activity.Manage.Call.Activity")
    }

    @Test
    fun testTrackItinLxMoreHelpClicked() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinLxMoreHelpClicked()
        assertItinLinkTracked("App.Itinerary.Activity.MoreHelp")
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
