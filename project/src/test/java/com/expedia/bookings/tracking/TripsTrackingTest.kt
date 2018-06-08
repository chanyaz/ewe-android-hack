package com.expedia.bookings.tracking

import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.itin.flight.common.ItinOmnitureUtils
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import org.hamcrest.Matchers
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
    fun testTrackTripListUpcomingTabVisit() {
        assertNoTrackingHasOccurred()

        TripsTracking.trackTripListUpcomingTabVisit()
        OmnitureTestUtils.assertStateTracked("App.Trips.Upcoming", OmnitureMatchers.withEventsString("event63"), mockAnalyticsProvider)
    }

    @Test
    fun testTrackTripListPastTabVisit() {
        assertNoTrackingHasOccurred()

        TripsTracking.trackTripListPastTabVisit()
        OmnitureTestUtils.assertStateTracked("App.Trips.Past", OmnitureMatchers.withEventsString("event63"), mockAnalyticsProvider)
    }

    @Test
    fun testTrackTripListCancelledTabVisit() {
        assertNoTrackingHasOccurred()

        TripsTracking.trackTripListCancelledTabVisit()
        OmnitureTestUtils.assertStateTracked("App.Trips.Cancelled", OmnitureMatchers.withEventsString("event63"), mockAnalyticsProvider)
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
    fun trackItinHotelPricingRewardsPageload() {
        assertNoTrackingHasOccurred()
        val trip = ItinMocker.hotelDetailsHappy
        val omnitureValues = ItinOmnitureUtils.createOmnitureTrackingValuesNew(trip, ItinOmnitureUtils.LOB.HOTEL)
        TripsTracking.trackHotelItinPricingRewardsPageLoad(omnitureValues)
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(mapOf(18 to "App.Itinerary.Hotel.PricingRewards")), mockAnalyticsProvider)
    }

    @Test
    fun trackHotelItinPricingRewardsClick() {
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppTripsHotelPricing)
        TripsTracking.trackHotelItinPricingRewardsClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.PricingRewards",
                OmnitureMatchers.withAbacusTestBucketed(25537), mockAnalyticsProvider)

        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppTripsHotelPricing)
        TripsTracking.trackHotelItinPricingRewardsClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.PricingRewards",
                OmnitureMatchers.withAbacusTestControl(25537), mockAnalyticsProvider)
    }

    @Test
    fun trackItinHotelViewReceiptTest() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinHotelViewReceipt()
        assertItinLinkTracked("App.Itinerary.Hotel.PricingRewards.ViewReceipt")
    }

    @Test
    fun trackHotelItinPricingRewardsViewRewardsClick() {
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
    fun trackItinMapDirectionsButton() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinMapDirectionsButton()
        assertItinMapLinksTracked("App.Map.Directions.Drive")
    }

    @Test
    fun trackItinExpandedMapZoomPan() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinExpandedMapZoomPan()
        assertItinMapLinksTracked("App.Map.Directions.Pan")
    }

    @Test
    fun trackItinExpandedMapZoomIn() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinExpandedMapZoomIn()
        assertItinMapLinksTracked("App.Map.Directions.ZoomIn")
    }

    @Test
    fun trackItinExpandedMapZoomOut() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinExpandedMapZoomOut()
        assertItinMapLinksTracked("App.Map.Directions.ZoomOut")
    }

    @Test
    fun trackItinLxDetailsDirectionsTest() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinLxDetailsDirections()
        assertItinLinkTracked("App.Itinerary.Activity.Directions")
    }

    @Test
    fun testTrackItinLxCallSupplierSupportClicked() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinLxCallSupplierClicked()
        assertItinLinkTracked("App.Itinerary.Activity.Manage.Call.Activity")
    }

    @Test
    fun testTrackItinLxMoreHelpClicked() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinLxMoreHelpClicked()
        assertItinLinkTracked("App.Itinerary.Activity.MoreHelp")
    }

    @Test
    fun testTrackItinLxMoreHelpPageLoad() {
        assertNoTrackingHasOccurred()
        val omnitureValues = ItinOmnitureUtils.createOmnitureTrackingValuesNew(ItinMocker.lxDetailsAlsoHappy, ItinOmnitureUtils.LOB.LX)
        TripsTracking.trackItinLxMoreHelpPageLoad(omnitureValues)
        OmnitureTestUtils.assertStateTracked("App.Itinerary.Activity.MoreHelp",
                OmnitureMatchers.withEvars(mapOf(18 to "App.Itinerary.Activity.MoreHelp")), mockAnalyticsProvider)
    }

    @Test
    fun testTrackItinPageLoad() {
        assertNoTrackingHasOccurred()
        val omnitureValues = ItinOmnitureUtils.createOmnitureTrackingValuesNew(ItinMocker.hotelDetailsHappy, ItinOmnitureUtils.LOB.HOTEL)
        val s = OmnitureTracking.createTrackPageLoadEventBase("Itin.Page.Load")
        TripsTracking.trackItinPageLoad(s, omnitureValues)

        OmnitureTestUtils.assertStateTracked("Itin.Page.Load", Matchers.allOf(
                OmnitureMatchers.withEvars(mapOf(2 to "D=c2", 5 to "0.0", 6 to "4", 18 to "Itin.Page.Load")),
                OmnitureMatchers.withProps(mapOf(2 to "itinerary", 5 to "2018-03-12", 6 to "2018-03-16", 8 to "8065305197869|7280999576135")),
                OmnitureMatchers.withEventsString("event63"),
                OmnitureMatchers.withProductsString(";Hotel:17669432;4;10000.00")), mockAnalyticsProvider)
    }

    @Test
    fun testTrackItinTripRefreshCallMade() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinTripRefreshCallMade()
        OmnitureTestUtils.assertLinkTracked("Trips Call", "App.Itinerary.Call.Made", OmnitureMatchers.withEventsString("event286"), mockAnalyticsProvider)
    }

    @Test
    fun testTrackItinTripRefreshCallSuccess() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinTripRefreshCallSuccess()
        OmnitureTestUtils.assertLinkTracked("Trips Call", "App.Itinerary.Call.Success", OmnitureMatchers.withEventsString("event287"), mockAnalyticsProvider)
    }

    @Test
    fun trackItinCarDetailsDirectionsTest() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinCarDetailsDirections()
        assertItinLinkTracked("App.Itinerary.Car.Directions")
    }

    @Test
    fun trackItinCarDetailsMapTest() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinCarDetailsMap()
        assertItinLinkTracked("App.Itinerary.Car.Map")
    }

    @Test
    fun testTrackItinTripRefreshCallFailure() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinTripRefreshCallFailure("ERROR")
        OmnitureTestUtils.assertLinkTracked("Trips Call", "App.Itinerary.Call.Failure", Matchers.allOf(
                OmnitureMatchers.withEventsString("event288"),
                OmnitureMatchers.withProps(mapOf(36 to "ERROR"))
        ), mockAnalyticsProvider)
    }

    fun testTrackItinLxCallCustomerSupportClicked() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinLxCallCustomerSupportClicked()
        assertItinLinkTracked("App.Itinerary.Activity.Manage.Call.Expedia")
    }

    @Test
    fun testTrackItinLxCustomerServiceLinkClicked() {
        assertNoTrackingHasOccurred()
        TripsTracking.trackItinLxCustomerServiceLinkClicked()
        assertItinLinkTracked("App.Itinerary.Activity.Manage.CSP")
    }

    fun assertItinLinkTracked(rfrrId: String) {
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", rfrrId, mockAnalyticsProvider)
    }

    fun assertItinMapLinksTracked(rfrrId: String) {
        OmnitureTestUtils.assertLinkTracked("Map Action", rfrrId, mockAnalyticsProvider)
    }

    fun assertNoTrackingHasOccurred() {
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    fun assertLinkTrackedWithAbTestExposure(analyticsString: String, rfrrId: String = "App.Trips") {
        OmnitureTestUtils.assertLinkTrackedWithAbTestExposure("Itinerary Action", rfrrId, analyticsString, mockAnalyticsProvider)
    }
}
