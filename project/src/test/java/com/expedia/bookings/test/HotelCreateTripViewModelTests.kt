package com.expedia.bookings.test

import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.vm.HotelCreateTripViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit.RetrofitError
import rx.Observer
import rx.observers.TestSubscriber
import java.io.IOException
import kotlin.test.assertEquals

public class HotelCreateTripViewModelTests {

    val mockHotelServicesTestRule = MockHotelServiceTestRule()
        @Rule get

    private val happyMockProductKey = "happypath_0"

    lateinit var sut: HotelCreateTripViewModel
    lateinit var hotelCreateTripParams: HotelCreateTripParams
    lateinit var testSubscriber: TestSubscriber<HotelCreateTripResponse>

    @Before
    fun setup() {
        testSubscriber = TestSubscriber<HotelCreateTripResponse>()
        sut = HotelCreateTripViewModel(mockHotelServicesTestRule.service)
    }

    @Test
    fun hotelServicesCreateTripIsCalled() {
        givenGoodCreateTripParams()
        sut = TestHotelCreateTripViewModel(testSubscriber, mockHotelServicesTestRule.service)

        sut.tripParams.onNext(hotelCreateTripParams)
        testSubscriber.awaitTerminalEvent()

        testSubscriber.assertCompleted()
        val createTripResponse = testSubscriber.onNextEvents.get(0)
        assertEquals(happyMockProductKey, createTripResponse.tripId)
    }

    @Test
    fun unknownError() {
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCreateTripResponseObserver().onNext(mockHotelServicesTestRule.getUnknownErrorResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.UNKNOWN_ERROR, testSubscriber.onNextEvents.get(0).errorCode)
    }

    @Test
    fun productKeyExpired() {
        val testSubscriber = TestSubscriber<ApiError>()
        val happyCreateTripResponse = mockHotelServicesTestRule.getProductKeyExpiredResponse()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCreateTripResponseObserver().onNext(happyCreateTripResponse)

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.HOTEL_PRODUCT_KEY_EXPIRY, testSubscriber.onNextEvents.get(0).errorCode)
    }

    @Test
    fun networkTimeout() {
        val testSubscriber = TestSubscriber<Unit>()

        sut.noResponseObservable.subscribe(testSubscriber)
        sut.getCreateTripResponseObserver().onError(RetrofitError.networkError("", IOException()))

        testSubscriber.assertValueCount(1)
    }

    private fun givenGoodCreateTripParams() {
        hotelCreateTripParams = HotelCreateTripParams(happyMockProductKey, false, 1, listOf(1))
    }

    public class TestHotelCreateTripViewModel(val testSubscriber: TestSubscriber<HotelCreateTripResponse>, hotelServices: HotelServices): HotelCreateTripViewModel(hotelServices) {

        override fun getCreateTripResponseObserver(): Observer<HotelCreateTripResponse> {
            return testSubscriber
        }
    }
}
