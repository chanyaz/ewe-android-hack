package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.test.HotelServicesRule
import com.expedia.vm.HotelDetailViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
public class HotelDetailViewModelTest {

    public var service: HotelServicesRule = HotelServicesRule()
    @Rule get

    var vm: HotelDetailViewModel by Delegates.notNull()
    var offer1: HotelOffersResponse by Delegates.notNull()
    var offer2: HotelOffersResponse by Delegates.notNull()

    @Before fun before() {
        vm = HotelDetailViewModel(RuntimeEnvironment.application, service.hotelServices())

        offer1 = HotelOffersResponse()
        offer1.hotelName = "hotel1"
        offer1.latitude = 1.0
        offer1.longitude = 2.0
        offer1.hotelRoomResponse = makeHotel()

        offer2 = HotelOffersResponse()
        offer2.hotelName = "hotel2"
        offer2.latitude = 100.0
        offer2.longitude = 150.0
        offer2.hotelRoomResponse = makeHotel()
    }

    private fun makeHotel() : ArrayList<HotelOffersResponse.HotelRoomResponse> {
        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();

        var hotel = HotelOffersResponse.HotelRoomResponse()
        var valueAdds = ArrayList<HotelOffersResponse.ValueAdds>()
        var valueAdd = HotelOffersResponse.ValueAdds()
        valueAdd.description = "Value Add"
        valueAdds.add(valueAdd)
        hotel.valueAdds = valueAdds

        var bedTypes = ArrayList<HotelOffersResponse.BedTypes>()
        var bedType = HotelOffersResponse.BedTypes()
        bedType.id = "1"
        bedType.description = "King Bed"
        bedTypes.add(bedType)
        hotel.bedTypes = bedTypes

        hotel.currentAllotment = "1"

        var lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        rooms.add(hotel)
        return rooms
    }

    @Test fun mapClicking() {
        val testSub = TestSubscriber.create<String>()
        val expected = listOf("hotel1", "hotel2", "hotel1", "hotel2", "hotel2", "hotel2")

        vm.mapClickedWithHotelData
                .map { hotel -> hotel.hotelName }
                .take(expected.size())
                .subscribe(testSub)

        vm.hotelOffersSubject.onNext(offer1)
        vm.mapClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer2)
        vm.mapClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer1)
        vm.mapClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer1)
        vm.hotelOffersSubject.onNext(offer2)
        vm.mapClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer1)
        vm.hotelOffersSubject.onNext(offer2)
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

        vm.hotelOffersSubject.onNext(offer1)
        vm.mapClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer2)
        vm.mapClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer1)
        vm.mapClickedSubject.onNext(Unit)

        testSub.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSub.assertCompleted()
        testSub.assertReceivedOnNext(expected)
    }

    @Test fun reviewsClicking() {
        val testSub = TestSubscriber.create<String>()
        val expected = listOf("hotel1", "hotel2", "hotel1", "hotel2", "hotel2", "hotel2")

        vm.reviewsClickedWithHotelData
                .map { hotel -> hotel.hotelName }
                .take(expected.size())
                .subscribe(testSub)

        vm.hotelOffersSubject.onNext(offer1)
        vm.reviewsClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer2)
        vm.reviewsClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer1)
        vm.reviewsClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer1)
        vm.hotelOffersSubject.onNext(offer2)
        vm.reviewsClickedSubject.onNext(Unit)

        vm.hotelOffersSubject.onNext(offer1)
        vm.hotelOffersSubject.onNext(offer2)
        vm.reviewsClickedSubject.onNext(Unit)
        vm.reviewsClickedSubject.onNext(Unit)

        testSub.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSub.assertCompleted()
        testSub.assertReceivedOnNext(expected)
    }
}