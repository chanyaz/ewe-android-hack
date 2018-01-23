package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelCheckoutParamsMock
import com.expedia.bookings.data.hotels.HotelCheckoutV2Params
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.MiscellaneousParams
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.TripDetails
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.HotelCheckoutViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.expedia.bookings.services.TestObserver
import java.util.concurrent.TimeUnit

@RunWith(RobolectricRunner::class)
class HotelCheckoutViewModelTest {

    val mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    @Test
    fun soldOutRoomLeadsToErrorObservableEmission() {
        val tripId = "error_room_unavailable_0"
        val tripDetails = TripDetails(tripId, "333.33", "USD", true)
        val miscParameters = MiscellaneousParams(true, "tealeafHotel:" + tripId, "expedia.app.android.phone:x.x.x")
        val checkoutParams = HotelCheckoutV2Params.Builder()
                .tripDetails(tripDetails)
                .checkoutInfo(HotelCheckoutParamsMock.checkoutInfo())
                .paymentInfo(HotelCheckoutParamsMock.paymentInfo())
                .traveler(HotelCheckoutParamsMock.traveler())
                .misc(miscParameters).build()

        val subjectUnderTest = HotelCheckoutViewModel(mockHotelServiceTestRule.services!!, PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!))

        val errorObservableTestSubscriber = TestObserver.create<ApiError>()
        subjectUnderTest.errorObservable.subscribe(errorObservableTestSubscriber)

        subjectUnderTest.checkoutParams.onNext(checkoutParams)
        errorObservableTestSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)

        errorObservableTestSubscriber.assertValue(ApiError(ApiError.Code.HOTEL_ROOM_UNAVAILABLE))
    }
}
