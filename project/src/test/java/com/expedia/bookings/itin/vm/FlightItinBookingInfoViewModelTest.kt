package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinLinkOffCardViewViewModel
import com.expedia.bookings.itin.flight.details.FlightItinBookingInfoViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightItinBookingInfoViewModelTest {

    private lateinit var activity: Activity
    private lateinit var sut: FlightItinBookingInfoViewModel
    private lateinit var url: String
    private lateinit var context: Context

    private val tripId = "12332"
    private val createWidgetSharedSubject = TestObserver<Boolean>()
    private val createAdditionalInfoSubject = TestObserver<ItinLinkOffCardViewViewModel.CardViewParams>()
    private val createTravelerlInfoSubject = TestObserver<ItinLinkOffCardViewViewModel.CardViewParams>()
    private val createPriceSummarySubject = TestObserver<ItinLinkOffCardViewViewModel.CardViewParams>()
    private val createManageBookingSubject = TestObserver<ItinLinkOffCardViewViewModel.CardViewParams>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinBookingInfoViewModel(activity, "TEST_ITIN_ID")
        url = "expedia.com"
        context = RuntimeEnvironment.application
    }

    @Test
    fun testUpdateWidget() {
        sut.widgetSharedSubject.subscribe(createWidgetSharedSubject)
        sut.additionalInfoCardViewWidgetVM.cardViewParamsSubject.subscribe(createAdditionalInfoSubject)
        sut.priceSummaryCardViewWidgetVM.cardViewParamsSubject.subscribe(createPriceSummarySubject)
        sut.manageBookingCardViewWidgetVM.cardViewParamsSubject.subscribe(createManageBookingSubject)
        sut.travelerInfoCardViewWidgetVM.cardViewParamsSubject.subscribe(createTravelerlInfoSubject)
        createWidgetSharedSubject.assertNoValues()
        createAdditionalInfoSubject.assertNoValues()
        createPriceSummarySubject.assertNoValues()
        createManageBookingSubject.assertNoValues()
        createTravelerlInfoSubject.assertNoValues()
        val testItinCardData = ItinCardDataFlightBuilder().build()
        sut.updateBookingInfoWidget(FlightItinBookingInfoViewModel.WidgetParams(
                "Jim Bob",
                false,
                "expedia.com",
                testItinCardData.id,
                testItinCardData.tripId
        ))
        createWidgetSharedSubject.assertValue(false)
        createTravelerlInfoSubject.assertValueCount(1)
        createManageBookingSubject.assertValueCount(1)
        createAdditionalInfoSubject.assertValueCount(1)
        createPriceSummarySubject.assertValueCount(1)
    }

    @Test
    fun testIntentForPriceSummary() {
        val intent = sut.buildWebViewIntent(R.string.itin_hotel_details_price_summary_heading, url, "price-summary", tripId)!!
        assertEquals(context.getString(R.string.itin_hotel_details_price_summary_heading), intent.extras.getString("ARG_TITLE"))
        assertTrue(intent.extras.getString("ARG_URL").startsWith(url))
        assertTrue(intent.extras.getString("ARG_URL").endsWith("#price-summary"))
        assertEquals(intent.extras.getString("ITIN_WEBVIEW_REFRESH_ON_EXIT_TRIP_NUMBER"), tripId)
        assertTrue(intent.getBooleanExtra("ARG_RETURN_FROM_HOTEL_ITIN_WEBVIEW", false))
    }

    @Test
    fun testIntentForAdditionalInfo() {
        val intent = sut.buildWebViewIntent(R.string.itin_hotel_details_additional_info_heading, url, null, tripId)!!
        assertEquals(context.getString(R.string.itin_hotel_details_additional_info_heading), intent.extras.getString("ARG_TITLE"))
        assertTrue(intent.extras.getString("ARG_URL").startsWith(url))
        assertEquals(intent.extras.getString("ITIN_WEBVIEW_REFRESH_ON_EXIT_TRIP_NUMBER"), tripId)
        assertTrue(intent.getBooleanExtra("ARG_RETURN_FROM_HOTEL_ITIN_WEBVIEW", false))
    }

    @Test
    fun `hide widget for shared itin`() {
        sut.widgetSharedSubject.subscribe(createWidgetSharedSubject)
        sut.additionalInfoCardViewWidgetVM.cardViewParamsSubject.subscribe(createAdditionalInfoSubject)
        sut.priceSummaryCardViewWidgetVM.cardViewParamsSubject.subscribe(createPriceSummarySubject)
        sut.manageBookingCardViewWidgetVM.cardViewParamsSubject.subscribe(createManageBookingSubject)
        sut.travelerInfoCardViewWidgetVM.cardViewParamsSubject.subscribe(createTravelerlInfoSubject)

        createWidgetSharedSubject.assertNoValues()
        createAdditionalInfoSubject.assertNoValues()
        createPriceSummarySubject.assertNoValues()
        createManageBookingSubject.assertNoValues()
        createTravelerlInfoSubject.assertNoValues()

        sut.updateBookingInfoWidget(FlightItinBookingInfoViewModel.WidgetParams(
                "Jim Bob",
                true,
                "expedia.com",
                "asda",
                "12312"
        ))

        createWidgetSharedSubject.assertValue(true)
        createAdditionalInfoSubject.assertNoValues()
        createPriceSummarySubject.assertNoValues()
        createManageBookingSubject.assertNoValues()
        createTravelerlInfoSubject.assertNoValues()
    }

    @Test
    fun `build webview intent trip number is null`() {
        val intent = sut.buildWebViewIntent(R.string.itin_hotel_details_additional_info_heading, url, null, null)!!
        assertEquals(context.getString(R.string.itin_hotel_details_additional_info_heading), intent.extras.getString("ARG_TITLE"))
        assertTrue(intent.extras.getString("ARG_URL").startsWith(url))
        assertEquals(intent.extras.getString("ITIN_WEBVIEW_REFRESH_ON_EXIT_TRIP_NUMBER"), null)
        assertFalse(intent.getBooleanExtra("ARG_RETURN_FROM_HOTEL_ITIN_WEBVIEW", false))
    }

    @Test
    fun `build webview intent trip number is empty`() {
        val intent = sut.buildWebViewIntent(R.string.itin_hotel_details_additional_info_heading, url, null, "")!!
        assertEquals(context.getString(R.string.itin_hotel_details_additional_info_heading), intent.extras.getString("ARG_TITLE"))
        assertTrue(intent.extras.getString("ARG_URL").startsWith(url))
        assertEquals(intent.extras.getString("ITIN_WEBVIEW_REFRESH_ON_EXIT_TRIP_NUMBER"), null)
        assertFalse(intent.getBooleanExtra("ARG_RETURN_FROM_HOTEL_ITIN_WEBVIEW", false))
    }
}
