package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.cars.ApiError
import com.expedia.vm.HotelErrorViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
public class HotelErrorViewModelTest {

    @Test fun observableEmissionsOnSoldOutApiError() {
        val subjectUnderTest = HotelErrorViewModel(RuntimeEnvironment.application)

        val soldOutObservableTestSubscriber = TestSubscriber.create<Unit>()
        subjectUnderTest.soldOutObservable.subscribe(soldOutObservableTestSubscriber)

        val errorImageObservableTestSubscriber = TestSubscriber.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.buttonTextObservable.subscribe(errorButtonObservableTestSubscriber)

        subjectUnderTest.apiErrorObserver.onNext(ApiError(ApiError.Code.HOTEL_ROOM_UNAVAILABLE))
        subjectUnderTest.actionObservable.onNext(Unit)

        soldOutObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_default)
        errorMessageObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(R.string.error_no_hotel_rooms_available))
        errorButtonObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(R.string.search_again))
    }
}