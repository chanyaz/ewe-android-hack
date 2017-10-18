package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.HotelCreateTripViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.expedia.bookings.services.TestObserver
import java.util.concurrent.TimeUnit

@RunWith(RobolectricRunner::class)
class HotelCreateTripViewModelTest {

    val mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    @Test
    fun soldOutRoomLeadsToErrorObservableEmission() {
        val subjectUnderTest = HotelCreateTripViewModel(mockHotelServiceTestRule.services!!, PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!))

        val errorObservableTestSubscriber = TestObserver.create<ApiError>()
        subjectUnderTest.errorObservable.subscribe(errorObservableTestSubscriber)

        subjectUnderTest.tripParams.onNext(HotelCreateTripParams("error_room_unavailable_0", false, 1, emptyList()))
        errorObservableTestSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)

        errorObservableTestSubscriber.assertValue(ApiError(ApiError.Code.HOTEL_ROOM_UNAVAILABLE))
    }
}
