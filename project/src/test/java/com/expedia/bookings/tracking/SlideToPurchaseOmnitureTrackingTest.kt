package com.expedia.bookings.tracking

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.tracking.hotel.HotelTracking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class SlideToPurchaseOmnitureTrackingTest {

    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        Db.getTripBucket().clearAirAttach()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testPackagesSlideToPurchase() {
        PackagesTracking().trackCheckoutSlideToPurchase(PaymentType.CARD_VISA, "FLEX")
        val expectedEvars = HashMap<Int, String>();
        expectedEvars.put(18, "App.Package.Checkout.SlideToPurchase")
        expectedEvars.put(37, "Visa")
        expectedEvars.put(44, "FLEX")
        OmnitureTestUtils.assertStateTrackedWithEventsAndEvars(mockAnalyticsProvider, "App.Checkout.SlideToPurchase", events = null, evarsMap = expectedEvars)
    }

    @Test
    fun testFlightsSlideToPurchase() {
        FlightsV2Tracking.trackSlideToPurchase(PaymentType.CARD_VISA, "NO_FLEX")
        val expectedEvars = HashMap<Int, String>();
        expectedEvars.put(18, "App.Flight.Checkout.SlideToPurchase")
        expectedEvars.put(37, "Visa")
        expectedEvars.put(44, "NO_FLEX")
        OmnitureTestUtils.assertStateTrackedWithEventsAndEvars(mockAnalyticsProvider, "App.Checkout.SlideToPurchase", events = null, evarsMap = expectedEvars)
    }

    @Test
    fun testHotelsSlideToPurchaseWithPartiallyPayableWithCard() {
        HotelTracking.trackHotelSlideToPurchase(PaymentType.CARD_VISA, PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD)
        val expectedEvars = HashMap<Int, String>();
        expectedEvars.put(18, "App.Hotels.Checkout.SlideToPurchase")
        expectedEvars.put(37, "Visa + Points")
        OmnitureTestUtils.assertStateTrackedWithEventsAndEvars(mockAnalyticsProvider, "App.Checkout.SlideToPurchase", events = null, evarsMap = expectedEvars)
    }

    @Test
    fun testHotelsSlideToPurchaseWithFullyPayableWithCard() {
        HotelTracking.trackHotelSlideToPurchase(PaymentType.CARD_VISA, PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD)
        val expectedEvars = HashMap<Int, String>();
        expectedEvars.put(18, "App.Hotels.Checkout.SlideToPurchase")
        expectedEvars.put(37, "Visa")
        OmnitureTestUtils.assertStateTrackedWithEventsAndEvars(mockAnalyticsProvider, "App.Checkout.SlideToPurchase", events = null, evarsMap = expectedEvars)
    }

    @Test
    fun testHotelsSlideToPurchaseWithFullyPayableWithPoints() {
        HotelTracking.trackHotelSlideToPurchase(PaymentType.CARD_VISA, PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT)
        val expectedEvars = HashMap<Int, String>();
        expectedEvars.put(18, "App.Hotels.Checkout.SlideToPurchase")
        expectedEvars.put(37, "Points")
        OmnitureTestUtils.assertStateTrackedWithEventsAndEvars(mockAnalyticsProvider, "App.Checkout.SlideToPurchase", events = null, evarsMap = expectedEvars)
    }

}
