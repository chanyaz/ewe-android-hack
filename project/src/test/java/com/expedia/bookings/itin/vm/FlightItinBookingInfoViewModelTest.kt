package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightItinBookingInfoViewModelTest {

    lateinit private var activity: Activity
    lateinit private var sut: FlightItinBookingInfoViewModel
    lateinit private var url: String
    lateinit private var context: Context


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
                testItinCardData.id
        ))
        createWidgetSharedSubject.assertValue(false)
        createTravelerlInfoSubject.assertValueCount(1)
        createManageBookingSubject.assertValueCount(1)
        createAdditionalInfoSubject.assertValueCount(1)
        createPriceSummarySubject.assertValueCount(1)
    }

    @Test
    fun testIntentForPriceSummary() {
        val intent = sut.buildWebViewIntent(R.string.itin_hotel_details_price_summary_heading, url, "price-summary")!!.intent
        assertEquals(context.getString(R.string.itin_hotel_details_price_summary_heading), intent.extras.getString("ARG_TITLE"))
        assertTrue(intent.extras.getString("ARG_URL").startsWith(url))
        assertTrue(intent.extras.getString("ARG_URL").endsWith("#price-summary"))
    }

    @Test
    fun testIntentForAdditionalInfo() {
        val intent = sut.buildWebViewIntent(R.string.itin_hotel_details_additional_info_heading, url, null)!!.intent
        assertEquals(context.getString(R.string.itin_hotel_details_additional_info_heading), intent.extras.getString("ARG_TITLE"))
        assertTrue(intent.extras.getString("ARG_URL").startsWith(url))
    }
}
