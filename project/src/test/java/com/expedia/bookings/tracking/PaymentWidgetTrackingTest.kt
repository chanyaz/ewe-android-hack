package com.expedia.bookings.tracking

import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class PaymentWidgetTrackingTest {

    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testPaymentEditTrackingForFlights() {
        val eventName = "App.Flight.Checkout.Payment.Edit.Card"
        OmnitureTracking.trackCheckoutPayment(LineOfBusiness.FLIGHTS_V2)
        OmnitureTestUtils.assertStateTracked(eventName, OmnitureMatchers.withEvars(mapOf(18 to eventName)), mockAnalyticsProvider)
    }

    @Test
    fun testPaymentEditTrackingForPackages() {
        val eventName = "App.Package.Checkout.Payment.Edit.Card"
        OmnitureTracking.trackCheckoutPayment(LineOfBusiness.PACKAGES)
        OmnitureTestUtils.assertStateTracked(eventName, OmnitureMatchers.withEvars(mapOf(18 to eventName)), mockAnalyticsProvider)
    }

    @Test
    fun testPaymentEditTrackingForHotels() {
        val eventName = "App.Hotels.Checkout.Payment.Edit.Card"
        OmnitureTracking.trackCheckoutPayment(LineOfBusiness.HOTELS)
        OmnitureTestUtils.assertStateTracked(eventName, OmnitureMatchers.withEvars(mapOf(18 to eventName)), mockAnalyticsProvider)
    }

    @Test
    fun testPaymentEditTrackingForLX() {
        val eventName = "App.LX.Checkout.Payment.Edit.Info"
        OmnitureTracking.trackCheckoutPayment(LineOfBusiness.LX)
        OmnitureTestUtils.assertStateTracked(eventName, OmnitureMatchers.withEvars(mapOf(18 to eventName)), mockAnalyticsProvider)
    }

    @Test
    fun testPaymentEditTrackingForTransport() {
        val eventName = "App.LX-GT.Checkout.Payment.Edit.Info"
        OmnitureTracking.trackCheckoutPayment(LineOfBusiness.TRANSPORT)
        OmnitureTestUtils.assertStateTracked(eventName, OmnitureMatchers.withEvars(mapOf(18 to eventName)), mockAnalyticsProvider)
    }

    @Test
    fun testPaymentEnterNewCardTrackingForFlights() {
        OmnitureTracking.trackShowPaymentEnterNewCard(LineOfBusiness.FLIGHTS_V2)
        OmnitureTestUtils.assertLinkTracked("Flight Checkout", "App.Flight.CKO.Payment.EnterManually", OmnitureMatchers.withEvars(HashMap<Int, String>()), mockAnalyticsProvider)
    }

    @Test
    fun testPaymentEnterNewCardTrackingForPackages() {
        OmnitureTracking.trackShowPaymentEnterNewCard(LineOfBusiness.PACKAGES)
        OmnitureTestUtils.assertLinkTracked("Package Checkout", "App.Package.CKO.Payment.EnterManually", OmnitureMatchers.withEvars(HashMap<Int, String>()), mockAnalyticsProvider)
    }
}
