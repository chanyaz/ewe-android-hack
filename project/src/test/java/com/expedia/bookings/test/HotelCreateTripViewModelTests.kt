package com.expedia.bookings.test

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.HotelCreateTripViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import io.reactivex.Observer
import com.expedia.bookings.services.TestObserver
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class HotelCreateTripViewModelTests {

    val mockHotelServicesTestRule = MockHotelServiceTestRule()
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    private val happyMockProductKey = "happypath_0"
    private val redeemableTripMockProductKey = "happypath_pwp_points_only"

    lateinit var paymentModel: PaymentModel<HotelCreateTripResponse>
    lateinit var sut: HotelCreateTripViewModel
    lateinit var hotelCreateTripParams: HotelCreateTripParams
    lateinit var testSubscriber: TestObserver<HotelCreateTripResponse>

    @Before
    fun setup() {
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        testSubscriber = TestObserver<HotelCreateTripResponse>()
        sut = HotelCreateTripViewModel(mockHotelServicesTestRule.services!!, paymentModel)
    }

    @Test
    fun hotelServicesCreateTripIsCalled() {
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_pwp_enabled.json")
        givenGoodCreateTripParams()
        sut = TestHotelCreateTripViewModel(testSubscriber, mockHotelServicesTestRule.services!!, paymentModel)

        sut.tripParams.onNext(hotelCreateTripParams)
        testSubscriber.awaitTerminalEvent()

        testSubscriber.assertComplete()
        val createTripResponse = testSubscriber.onNextEvents[0]
        assertEquals(happyMockProductKey, createTripResponse.tripId)
    }

    @Test
    fun tripRewardsRedeemableIsFalseForPOSWithPwPDisabled() {
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_pwp_disabled.json")
        givenRedeemableCreateTripParams()
        sut = TestHotelCreateTripViewModel(testSubscriber, mockHotelServicesTestRule.services!!, paymentModel)

        sut.tripParams.onNext(hotelCreateTripParams)
        testSubscriber.awaitTerminalEvent()

        testSubscriber.assertComplete()
        val createTripResponse = testSubscriber.onNextEvents[0]
        assertFalse(createTripResponse.isRewardsRedeemable())
    }

    @Test
    fun unknownError() {
        val testSubscriber = TestObserver<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCreateTripResponseObserver().onNext(mockHotelServicesTestRule.getUnknownErrorResponse())

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.UNKNOWN_ERROR, testSubscriber.onNextEvents[0].errorCode)
    }

    @Test
    fun productKeyExpired() {
        val testSubscriber = TestObserver<ApiError>()
        val happyCreateTripResponse = mockHotelServicesTestRule.getProductKeyExpiredResponse()
        sut.errorObservable.subscribe(testSubscriber)

        sut.getCreateTripResponseObserver().onNext(happyCreateTripResponse)

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.HOTEL_PRODUCT_KEY_EXPIRY, testSubscriber.onNextEvents[0].errorCode)
    }

    @Test
    fun networkTimeout() {
        val testSubscriber = TestObserver<Unit>()

        sut.noResponseObservable.subscribe(testSubscriber)
        sut.getCreateTripResponseObserver().onError(IOException())
        testSubscriber.assertValueCount(1)
    }

    private fun givenGoodCreateTripParams() {
        hotelCreateTripParams = HotelCreateTripParams(happyMockProductKey, false, 1, listOf(1))
    }

    private fun givenRedeemableCreateTripParams() {
        hotelCreateTripParams = HotelCreateTripParams(redeemableTripMockProductKey, false, 1, listOf(1))
    }

    class TestHotelCreateTripViewModel(val testSubscriber: TestObserver<HotelCreateTripResponse>, hotelServices: HotelServices, paymentModel: PaymentModel<HotelCreateTripResponse>) : HotelCreateTripViewModel(hotelServices, paymentModel) {

        override fun getCreateTripResponseObserver(): Observer<HotelCreateTripResponse> {
            return testSubscriber
        }
    }
}
