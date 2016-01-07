package com.expedia.bookings.test

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelCheckoutParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.PaymentModel
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.vm.HotelCheckoutViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit.RetrofitError
import rx.Observer
import rx.observers.TestSubscriber
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

public class HotelCheckoutViewModelTests {

    var mockHotelTestServiceRule = MockHotelServiceTestRule()
        @Rule get

    lateinit var paymentModel: PaymentModel
    lateinit var sut: HotelCheckoutViewModel
    lateinit var checkoutParams: HotelCheckoutParams
    lateinit var testSubscriber: TestSubscriber<HotelCheckoutResponse>
    lateinit var happyCreateTripResponse: HotelCreateTripResponse

    @Before
    fun setup() {
        paymentModel = PaymentModel(mockHotelTestServiceRule.service)
        sut = HotelCheckoutViewModel(mockHotelTestServiceRule.service, paymentModel)
    }

    @Test
    fun unknownError() {
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getUnknownErrorCheckoutResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.HOTEL_CHECKOUT_UNKNOWN, testSubscriber.onNextEvents.get(0).errorCode)
    }

    @Test
    fun invalidTravelerInput() {
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getInvalidTravelerInputCheckoutResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.HOTEL_CHECKOUT_TRAVELLER_DETAILS, testSubscriber.onNextEvents.get(0).errorCode)
    }

    @Test
    fun invalidCardNumberInput() {
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getInvalidCardNumberInputCheckoutResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.HOTEL_CHECKOUT_CARD_DETAILS, testSubscriber.onNextEvents.get(0).errorCode)
    }

    @Test
    fun paymentFailed() {
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getPaymentFailedCheckoutResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.PAYMENT_FAILED, testSubscriber.onNextEvents.get(0).errorCode)
    }

    @Test
    fun sessionTimeout() {
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getSessionTimeoutCheckoutResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.SESSION_TIMEOUT, testSubscriber.onNextEvents.get(0).errorCode)
    }

    @Test
    fun tripAlreadyBooked() {
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getTripAlreadyBookedCheckoutResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.TRIP_ALREADY_BOOKED, testSubscriber.onNextEvents.get(0).errorCode)
    }

    @Test
    fun priceChange() {
        givenWeHadAHappyCreateTripResponse()
        givenPriceChangeCheckoutResponse()
        val testSubscriber = TestSubscriber<HotelCreateTripResponse>()
        sut.priceChangeResponseObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onNext(mockHotelTestServiceRule.getPriceChangeCheckoutResponse())

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(happyCreateTripResponse)
    }

    @Test
    fun checkoutErrorCallsNoResponseObservable() {
        val networkErrorThrowable = RetrofitError.networkError("", IOException())
        val testSubscriber = TestSubscriber<Throwable>()
        sut.noResponseObservable.subscribe(testSubscriber)

        sut.getCheckoutResponseObserver().onError(networkErrorThrowable)

        testSubscriber.assertValue(networkErrorThrowable)
    }

    @Test
    fun newCheckoutParamsTriggersCheckoutCall() {
        givenGoodCheckoutResponse()
        testSubscriber = TestSubscriber<HotelCheckoutResponse>()
        sut = TestHotelCheckoutViewModel(testSubscriber, mockHotelTestServiceRule.service, paymentModel)

        sut.checkoutParams.onNext(checkoutParams)

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertValueCount(1)
        assertNotNull(testSubscriber.onNextEvents.get(0))
    }

    private fun givenPriceChangeCheckoutResponse() {
        givenSomeCheckoutParams("hotel_price_change_checkout")
    }

    private fun givenGoodCheckoutResponse() {
        givenSomeCheckoutParams("happypath_0")
    }

    private fun givenSomeCheckoutParams(tripId: String) {
        checkoutParams = HotelCheckoutParams()
        checkoutParams.tripId = tripId
        checkoutParams.expectedTotalFare = "42.00"
        checkoutParams.tealeafTransactionId = "tealeafHotel:" + checkoutParams.tripId
    }

    private fun givenWeHadAHappyCreateTripResponse() {
        happyCreateTripResponse = mockHotelTestServiceRule.getHappyCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemHotelV2(happyCreateTripResponse))
    }

    class TestHotelCheckoutViewModel(val testSubscriber: TestSubscriber<HotelCheckoutResponse>, hotelServices: HotelServices, paymentModel: PaymentModel): HotelCheckoutViewModel(hotelServices,paymentModel) {
        override fun getCheckoutResponseObserver(): Observer<HotelCheckoutResponse> {
            return testSubscriber
        }
    }
}
