package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.vm.HotelCreateTripViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit

@RunWith(RobolectricRunner::class)
public class HotelCreateTripViewModelTest {

    val mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    @Test
    fun soldOutRoomLeadsToErrorObservableEmission() {
        val subjectUnderTest = HotelCreateTripViewModel(mockHotelServiceTestRule.service)

        val errorObservableTestSubscriber = TestSubscriber.create<ApiError>()
        subjectUnderTest.errorObservable.subscribe(errorObservableTestSubscriber)

        val createTripSubscriber = TestSubscriber(subjectUnderTest.getCreateTripResponseObserver())
        subjectUnderTest.tripParams.onNext(HotelCreateTripParams("error_room_unavailable_0", false, 1, emptyList()))
        createTripSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)

        errorObservableTestSubscriber.assertValue(ApiError(ApiError.Code.HOTEL_ROOM_UNAVAILABLE))
    }
}
