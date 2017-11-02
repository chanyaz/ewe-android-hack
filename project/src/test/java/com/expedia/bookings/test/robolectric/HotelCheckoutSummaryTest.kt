package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.View
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.HotelCheckoutSummaryWidget
import com.expedia.bookings.widget.TextView
import com.expedia.vm.HotelCheckoutSummaryViewModel
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@RunWith(RobolectricRunner::class)
class HotelCheckoutSummaryTest {

    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get
    lateinit private var hotelCheckoutSummaryWidget: HotelCheckoutSummaryWidget
    lateinit private var hotelCheckoutSummaryViewModel: HotelCheckoutSummaryViewModel
    lateinit private var paymentModel: PaymentModel<HotelCreateTripResponse>
    lateinit private var activity: Activity
    lateinit private var createTripResponse: HotelCreateTripResponse
    private var createTripResponseObservable = PublishSubject.create<HotelCreateTripResponse>()



    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        hotelCheckoutSummaryViewModel = HotelCheckoutSummaryViewModel(activity, paymentModel)
        createTripResponseObservable.subscribe(hotelCheckoutSummaryViewModel.createTripResponseObservable)
    }

    @Test
    fun testDateViewsVisibilityInHotelCheckoutSummaryWidgetByTurningOnABTest() {
        toggleABTestCheckinCheckoutDatesInline(true)
        hotelCheckoutSummaryWidget = HotelCheckoutSummaryWidget(activity, null, hotelCheckoutSummaryViewModel)
        getHappyCreateTripResponse()

        assertTrue(hotelCheckoutSummaryWidget.date.visibility == View.GONE)
        assertTrue(hotelCheckoutSummaryWidget.checkinCheckoutDateContainer.visibility == View.VISIBLE)
    }

    @Test
    fun testDateViewsVisibilityInHotelCheckoutSummaryWidgetByTurningOffABTest() {
        toggleABTestCheckinCheckoutDatesInline(false)
        hotelCheckoutSummaryWidget = HotelCheckoutSummaryWidget(activity, null, hotelCheckoutSummaryViewModel)
        getHappyCreateTripResponse()

        assertTrue(hotelCheckoutSummaryWidget.date.visibility == View.VISIBLE)
        assertTrue(hotelCheckoutSummaryWidget.checkinCheckoutDateContainer.visibility == View.GONE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun testValueOfDatesDisplayedInHotelCheckoutSummaryWithABTestTurnedOff() {
        toggleABTestCheckinCheckoutDatesInline(false)
        hotelCheckoutSummaryWidget = HotelCheckoutSummaryWidget(activity, null, hotelCheckoutSummaryViewModel)
        getHappyCreateTripResponse()

        assertEquals("Mar 22, 2013 - Mar 23, 2013", hotelCheckoutSummaryWidget.date.text)
        assertEquals("", hotelCheckoutSummaryWidget.checkinDate.text)
        assertEquals("", hotelCheckoutSummaryWidget.checkoutDate.text)

        getPayLaterOfferCreateTripResponse()

        assertEquals("Apr 19, 2016 - Apr 22, 2016", hotelCheckoutSummaryWidget.date.text)
        assertEquals("", hotelCheckoutSummaryWidget.checkinDate.text)
        assertEquals("", hotelCheckoutSummaryWidget.checkoutDate.text)
    }

    @Test
    fun testValueAddsNullCreatesZeroValueAddContainers() {
        hotelCheckoutSummaryWidget = HotelCheckoutSummaryWidget(activity, null, hotelCheckoutSummaryViewModel)

        val container = hotelCheckoutSummaryWidget.valueAddsContainer
        assertEquals(0, container.childCount)

        val valueAdds = ArrayList<HotelOffersResponse.ValueAdds>()
        hotelCheckoutSummaryViewModel.valueAddsListObservable.onNext(valueAdds)
        assertEquals(0, container.childCount)

        val valueAdd1 = HotelOffersResponse.ValueAdds()
        val description1 = "Value Add"
        valueAdd1.description = description1
        valueAdds.add(valueAdd1)
        hotelCheckoutSummaryViewModel.valueAddsListObservable.onNext(valueAdds)
        assertEquals(1, container.childCount)
        val textView = container.getChildAt(0) as TextView
        assertEquals(description1, actual = textView.text.toString())

        hotelCheckoutSummaryViewModel.valueAddsListObservable.onNext(emptyList())
        assertEquals(0, container.childCount)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun testValueOfDatesDisplayedInHotelCheckoutSummaryWithABTestTurnedOn() {
        toggleABTestCheckinCheckoutDatesInline(true)
        hotelCheckoutSummaryWidget = HotelCheckoutSummaryWidget(activity, null, hotelCheckoutSummaryViewModel)
        getHappyCreateTripResponse()

        assertEquals("", hotelCheckoutSummaryWidget.date.text)
        assertEquals("Fri, Mar 22", hotelCheckoutSummaryWidget.checkinDate.text)
        assertEquals("Sat, Mar 23", hotelCheckoutSummaryWidget.checkoutDate.text)

        getPayLaterOfferCreateTripResponse()

        assertEquals("", hotelCheckoutSummaryWidget.date.text)
        assertEquals("Tue, Apr 19", hotelCheckoutSummaryWidget.checkinDate.text)
        assertEquals("Fri, Apr 22", hotelCheckoutSummaryWidget.checkoutDate.text)
    }

    private fun toggleABTestCheckinCheckoutDatesInline(toggleOn: Boolean) {
        if (toggleOn) {
            AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline)
        }
        else {
            AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline)
        }
    }
    private fun getHappyCreateTripResponse() {
        createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        createTripResponseObservable.onNext(createTripResponse)
    }

    private fun getPayLaterOfferCreateTripResponse() {
        createTripResponse = mockHotelServiceTestRule.getPayLaterOfferCreateTripResponse()
        createTripResponseObservable.onNext(createTripResponse)
    }
}
