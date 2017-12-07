package com.expedia.bookings.itin.widget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.itin.vm.FlightItinBookingInfoViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import kotlinx.android.synthetic.main.widget_flight_itin_booking_details_widget.view.booking_info_container
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinBookingDetailsWidgetTest {

    lateinit var sut: FlightItinBookingDetailsWidget
    lateinit var intent: Intent
    lateinit var params: FlightItinBookingInfoViewModel.WidgetParams
    lateinit var context: Context
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    val activity = Robolectric.buildActivity(Activity::class.java).create().get()
    val testItinCardData = ItinCardDataFlightBuilder().build()


    @Before
    fun before() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(activity).inflate(R.layout.test_flight_itin_booking_details_widget, null) as FlightItinBookingDetailsWidget
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testManageBookingCard() {
        updateWidgetWithShareParams()
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        sut.manageBookingCard.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.ManageBooking", mockAnalyticsProvider)
        assertEquals(context.getString(R.string.itin_hotel_manage_booking_header), sut.manageBookingCard.getHeadingText())
        assertEquals(context.getString(R.string.itin_hotel_details_manage_booking_subheading), sut.manageBookingCard.getSubHeadingText())
        assertEquals(View.VISIBLE, sut.manageBookingCard.getSubheadingVisibility())
    }

    @Test
    fun testPriceSummaryCard() {
        updateWidgetWithShareParams()
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        sut.priceSummaryCard.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.PriceSummary", mockAnalyticsProvider)
        assertEquals(context.getString(R.string.itin_hotel_details_price_summary_heading), sut.priceSummaryCard.getHeadingText())
        assertEquals(View.GONE, sut.priceSummaryCard.getSubheadingVisibility())
    }

    @Test
    fun testAdditionalInfoCard() {
        updateWidgetWithShareParams()
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        sut.additionalInfoCard.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Info.Additional", mockAnalyticsProvider)
        assertEquals(context.getString(R.string.itin_hotel_details_additional_info_heading), sut.additionalInfoCard.getHeadingText())
        assertEquals(View.GONE, sut.additionalInfoCard.getSubheadingVisibility())
    }

    @Test
    fun testTravelerInfoCard() {
        updateWidgetWithShareParams()
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        sut.travelerInfoCard.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.TravelerInfo", mockAnalyticsProvider)
        assertEquals(context.getString(R.string.itin_flight_traveler_info), sut.travelerInfoCard.getHeadingText())
        assertEquals("Jim Bob", sut.travelerInfoCard.getSubHeadingText())
        assertEquals(View.VISIBLE, sut.travelerInfoCard.getSubheadingVisibility())
    }

    @Test
    fun testNonSharedVisibility() {
        updateWidgetWithShareParams()
        assertEquals(View.VISIBLE, sut.booking_info_container.visibility)
    }

    @Test
    fun testSharedItinVisibility() {
        updateWidgetWithShareParams(true)
        assertEquals(View.GONE, sut.booking_info_container.visibility)
    }

    private fun updateWidgetWithShareParams(isShared: Boolean = false ) {
        sut.viewModel = FlightItinBookingInfoViewModel(activity, "TEST_ITIN_ID")
        params = FlightItinBookingInfoViewModel.WidgetParams(
                "Jim Bob",
                isShared,
                null,
                testItinCardData.id
        )
        sut.viewModel.updateBookingInfoWidget(params)
    }
}