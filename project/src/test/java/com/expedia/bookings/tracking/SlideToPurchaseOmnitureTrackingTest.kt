package com.expedia.bookings.tracking

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertLinkTracked
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEvars
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
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testPackagesSlideToPurchase() {
        PackagesTracking().trackCheckoutSlideToPurchase(PaymentType.CARD_VISA, "FLEX")
        val expectedEvars = mapOf(
                18 to "App.Package.Checkout.SlideToPurchase",
                37 to "Visa",
                44 to "FLEX",
                61 to PointOfSale.getPointOfSale().tpid.toString()
        )
        assertLinkTracked("Universal Checkout", "App.CKO.SlideToBook", withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testFlightsSlideToPurchase() {
        FlightsV2Tracking.trackSlideToPurchase(PaymentType.CARD_VISA, "NO_FLEX")
        val expectedEvars = mapOf(
                18 to "App.Flight.Checkout.SlideToPurchase",
                37 to "Visa",
                44 to "NO_FLEX",
                61 to PointOfSale.getPointOfSale().tpid.toString()
        )
        assertLinkTracked("Universal Checkout", "App.CKO.SlideToBook", withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testHotelsSlideToPurchaseWithPartiallyPayableWithCard() {
        HotelTracking.trackHotelSlideToPurchase(PaymentType.CARD_VISA, PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD)
        val expectedEvars = mapOf(
                18 to "App.Hotels.Checkout.SlideToPurchase",
                37 to "Visa + Points",
                61 to PointOfSale.getPointOfSale().tpid.toString()
        )
        assertLinkTracked("Universal Checkout", "App.CKO.SlideToBook", withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testHotelsSlideToPurchaseWithFullyPayableWithCard() {
        HotelTracking.trackHotelSlideToPurchase(PaymentType.CARD_VISA, PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD)
        val expectedEvars = mapOf(
                18 to "App.Hotels.Checkout.SlideToPurchase",
                37 to "Visa",
                61 to PointOfSale.getPointOfSale().tpid.toString()
        )
        assertLinkTracked("Universal Checkout", "App.CKO.SlideToBook", withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testHotelsSlideToPurchaseWithFullyPayableWithPoints() {
        HotelTracking.trackHotelSlideToPurchase(PaymentType.CARD_VISA, PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT)
        val expectedEvars = mapOf(
                18 to "App.Hotels.Checkout.SlideToPurchase",
                37 to "Points",
                61 to PointOfSale.getPointOfSale().tpid.toString()
        )
        assertLinkTracked("Universal Checkout", "App.CKO.SlideToBook", withEvars(expectedEvars), mockAnalyticsProvider)
    }

}
