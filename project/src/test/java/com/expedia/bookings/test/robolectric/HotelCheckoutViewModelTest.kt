package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelCheckoutParamsMock
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.ServicesRule
import com.expedia.bookings.data.hotels.HotelCheckoutV2Params
import com.expedia.bookings.data.payment.MiscellaneousParams
import com.expedia.bookings.data.payment.TripDetails
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.vm.HotelCheckoutViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit

@RunWith(RobolectricRunner::class)
public class HotelCheckoutViewModelTest {

    val mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    public var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    @Test
    fun soldOutRoomLeadsToErrorObservableEmission() {
        val tripId = "error_room_unavailable_0"
        val tripDetails = TripDetails(tripId, "333.33", "USD", "guid", true)
        val miscParameters = MiscellaneousParams(true, "tealeafHotel:" + tripId)
        val checkoutParams = HotelCheckoutV2Params.Builder()
                .tripDetails(tripDetails)
                .checkoutInfo(HotelCheckoutParamsMock.checkoutInfo())
                .paymentInfo(HotelCheckoutParamsMock.paymentInfo())
                .traveler(HotelCheckoutParamsMock.traveler())
                .misc(miscParameters).build();

        val subjectUnderTest = HotelCheckoutViewModel(mockHotelServiceTestRule.service, PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!))

        val errorObservableTestSubscriber = TestSubscriber.create<ApiError>()
        subjectUnderTest.errorObservable.subscribe(errorObservableTestSubscriber)

        val checkoutSubscriber = TestSubscriber(subjectUnderTest.getCheckoutResponseObserver())
        subjectUnderTest.checkoutParams.onNext(checkoutParams)
        checkoutSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)

        errorObservableTestSubscriber.assertValue(ApiError(ApiError.Code.HOTEL_ROOM_UNAVAILABLE))
    }
}
