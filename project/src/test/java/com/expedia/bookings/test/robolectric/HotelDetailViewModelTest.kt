package com.expedia.bookings.test.robolectric

import android.content.Intent
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.test.HotelServicesRule
import com.expedia.vm.HotelDetailViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
public class HotelDetailViewModelTest {

    public var service: HotelServicesRule = HotelServicesRule()
    @Rule get

    var vm: HotelDetailViewModel by Delegates.notNull()
    var hotel1: Hotel by Delegates.notNull()
    var hotel2: Hotel by Delegates.notNull()

    @Before fun before() {
        vm = HotelDetailViewModel(RuntimeEnvironment.application, service.hotelServices())

        hotel1 = Hotel()
        hotel1.localizedName = "hotel1"
        hotel1.latitude = 1.0
        hotel1.longitude = 2.0

        hotel2 = Hotel()
        hotel2.localizedName = "hotel2"
        hotel2.latitude = 100.0
        hotel2.longitude = 150.0
    }

    @Test fun mapClicking() {
        val testSub = TestSubscriber.create<String>()
        val expected = listOf("hotel1", "hotel2", "hotel1", "hotel2", "hotel2", "hotel2")

        vm.mapClickedWithHotelData
                .map { hotel -> hotel.localizedName }
                .take(expected.size())
                .subscribe(testSub)

        vm.hotelSelectedSubject.onNext(hotel1)
        vm.mapClickedSubject.onNext(Unit)

        vm.hotelSelectedSubject.onNext(hotel2)
        vm.mapClickedSubject.onNext(Unit)

        vm.hotelSelectedSubject.onNext(hotel1)
        vm.mapClickedSubject.onNext(Unit)

        vm.hotelSelectedSubject.onNext(hotel1)
        vm.hotelSelectedSubject.onNext(hotel2)
        vm.mapClickedSubject.onNext(Unit)

        vm.hotelSelectedSubject.onNext(hotel1)
        vm.hotelSelectedSubject.onNext(hotel2)
        vm.mapClickedSubject.onNext(Unit)
        vm.mapClickedSubject.onNext(Unit)

        testSub.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSub.assertCompleted()
        testSub.assertReceivedOnNext(expected)
    }

    @Test fun mapIntent() {
        val testSub = TestSubscriber.create<String>()
        val expected = listOf("geo:1.0,2.0", "geo:100.0,150.0", "geo:1.0,2.0")

        vm.startMapWithIntentObservable
                .take(expected.size())
                .map { it.getData().toString() }
                .subscribe(testSub)

        vm.hotelSelectedSubject.onNext(hotel1)
        vm.mapClickedSubject.onNext(Unit)

        vm.hotelSelectedSubject.onNext(hotel2)
        vm.mapClickedSubject.onNext(Unit)

        vm.hotelSelectedSubject.onNext(hotel1)
        vm.mapClickedSubject.onNext(Unit)

        testSub.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSub.assertCompleted()
        testSub.assertReceivedOnNext(expected)
    }

    @Test fun reviewsClicking() {
        val testSub = TestSubscriber.create<String>()
        val expected = listOf("hotel1", "hotel2", "hotel1", "hotel2", "hotel2", "hotel2")

        vm.reviewsClickedWithHotelData
                .map { hotel -> hotel.localizedName }
                .take(expected.size())
                .subscribe(testSub)

        vm.hotelSelectedSubject.onNext(hotel1)
        vm.reviewsClickedSubject.onNext(Unit)

        vm.hotelSelectedSubject.onNext(hotel2)
        vm.reviewsClickedSubject.onNext(Unit)

        vm.hotelSelectedSubject.onNext(hotel1)
        vm.reviewsClickedSubject.onNext(Unit)

        vm.hotelSelectedSubject.onNext(hotel1)
        vm.hotelSelectedSubject.onNext(hotel2)
        vm.reviewsClickedSubject.onNext(Unit)

        vm.hotelSelectedSubject.onNext(hotel1)
        vm.hotelSelectedSubject.onNext(hotel2)
        vm.reviewsClickedSubject.onNext(Unit)
        vm.reviewsClickedSubject.onNext(Unit)

        testSub.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSub.assertCompleted()
        testSub.assertReceivedOnNext(expected)
    }
}