package com.expedia.bookings.test

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelCheckoutParamsMock
import com.expedia.bookings.data.hotels.HotelCheckoutV2Params
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.MiscellaneousParams
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.TripDetails
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.HotelCheckoutViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import io.reactivex.Observer
import com.expedia.bookings.services.TestObserver
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HotelCheckoutViewModelTests {

    var mockHotelTestServiceRule = MockHotelServiceTestRule()
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    lateinit var paymentModel: PaymentModel<HotelCreateTripResponse>
    lateinit var sut: HotelCheckoutViewModel
    lateinit var checkoutParams: HotelCheckoutV2Params
    lateinit var testSubscriber: TestObserver<HotelCheckoutResponse>
    lateinit var happyCreateTripResponse: HotelCreateTripResponse

    @Before
    fun setup() {
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        sut = HotelCheckoutViewModel(mockHotelTestServiceRule.services!!, paymentModel)
    }

    @Test
    fun unknownError() {
        val testSubscriber = TestObserver<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getUnknownErrorCheckoutResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.HOTEL_CHECKOUT_UNKNOWN, testSubscriber.values()[0].errorCode)
    }

    @Test
    fun invalidTravelerInput() {
        val testSubscriber = TestObserver<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getInvalidTravelerInputCheckoutResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.HOTEL_CHECKOUT_TRAVELLER_DETAILS, testSubscriber.values()[0].errorCode)
    }

    @Test
    fun invalidCardNumberInput() {
        val testSubscriber = TestObserver<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getInvalidCardNumberInputCheckoutResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.HOTEL_CHECKOUT_CARD_DETAILS, testSubscriber.values()[0].errorCode)
    }

    @Test
    fun paymentFailed() {
        val testSubscriber = TestObserver<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getPaymentFailedCheckoutResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.PAYMENT_FAILED, testSubscriber.values()[0].errorCode)
    }

    @Test
    fun sessionTimeout() {
        val testSubscriber = TestObserver<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getSessionTimeoutCheckoutResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.SESSION_TIMEOUT, testSubscriber.values()[0].errorCode)
    }

    @Test
    fun tripAlreadyBooked() {
        val testSubscriber = TestObserver<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getTripAlreadyBookedCheckoutResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.TRIP_ALREADY_BOOKED, testSubscriber.values()[0].errorCode)
    }

    @Test
    fun priceChange() {
        givenWeHadAHappyCreateTripResponse()
        givenPriceChangeCheckoutResponse()
        val testSubscriber = TestObserver<HotelCreateTripResponse>()
        sut.priceChangeResponseObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getPriceChangeCheckoutResponse())

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(happyCreateTripResponse)
    }

    @Test
    fun checkoutErrorCallsNoResponseObservable() {
        val networkErrorThrowable = IOException()
        val testSubscriber = TestObserver<Throwable>()
        sut.noResponseObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onError(networkErrorThrowable)

        testSubscriber.assertValue(networkErrorThrowable)
    }

    @Test
    fun newCheckoutParamsTriggersCheckoutCall() {
        givenGoodCheckoutResponse()
        testSubscriber = TestObserver<HotelCheckoutResponse>()
        sut = TestHotelCheckoutViewModel(testSubscriber, mockHotelTestServiceRule.services!!, paymentModel)

        sut.checkoutParams.onNext(checkoutParams)

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertValueCount(1)
        assertNotNull(testSubscriber.values()[0])
    }

    private fun givenPriceChangeCheckoutResponse() {
        givenSomeCheckoutParams("hotel_price_change_checkout")
    }

    private fun givenGoodCheckoutResponse() {
        givenSomeCheckoutParams("happypath_0")
    }

    private fun givenSomeCheckoutParams(tripId: String) {
        val tripDetails = TripDetails(tripId, "42.00", "USD", true)
        val miscParameters = MiscellaneousParams(true, "tealeafHotel:" + tripId, "expedia.app.android.phone:x.x.x")
        checkoutParams = HotelCheckoutV2Params.Builder()
                .tripDetails(tripDetails)
                .checkoutInfo(HotelCheckoutParamsMock.checkoutInfo())
                .paymentInfo(HotelCheckoutParamsMock.paymentInfo())
                .traveler(HotelCheckoutParamsMock.traveler())
                .misc(miscParameters).build()
    }

    private fun givenWeHadAHappyCreateTripResponse() {
        happyCreateTripResponse = mockHotelTestServiceRule.getHappyCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(happyCreateTripResponse))
    }

    class TestHotelCheckoutViewModel(val testSubscriber: TestObserver<HotelCheckoutResponse>, hotelServices: HotelServices, paymentModel: PaymentModel<HotelCreateTripResponse>) : HotelCheckoutViewModel(hotelServices, paymentModel) {
        override fun getCheckoutResponseObserver(): Observer<HotelCheckoutResponse> {
            return testSubscriber
        }
    }
}
