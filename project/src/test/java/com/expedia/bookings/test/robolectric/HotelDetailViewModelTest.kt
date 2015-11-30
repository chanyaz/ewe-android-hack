package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.test.HotelServicesRule
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelRoomRateViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
public class HotelDetailViewModelTest {

    // TODO: Improve HotelDetailViewModel test coverage
    //  -- TODO: Use MockHotelServiceTestRule (It provides helper functions to grab hotel responses. We shouldn't be creating mock hotel objects (see: makeHotel())

    public var service: HotelServicesRule = HotelServicesRule()
        @Rule get

    private var vm: HotelDetailViewModel by Delegates.notNull()
    private var offer1: HotelOffersResponse by Delegates.notNull()
    private var offer2: HotelOffersResponse by Delegates.notNull()
    private var offer3: HotelOffersResponse by Delegates.notNull()

    private val expectedTotalPriceWithMandatoryFees = 42f

    @Before fun before() {
        vm = HotelDetailViewModel(RuntimeEnvironment.application, service.hotelServices(), endlessObserver { /*ignore*/ })

        offer1 = HotelOffersResponse()
        offer1.hotelId = "hotel1"
        offer1.hotelName = "hotel1"
        offer1.latitude = 1.0
        offer1.longitude = 2.0
        offer1.hotelRoomResponse = makeHotel()

        offer2 = HotelOffersResponse()
        offer1.hotelId = "hotel2"
        offer2.hotelName = "hotel2"
        offer2.latitude = 100.0
        offer2.longitude = 150.0
        offer2.hotelRoomResponse = makeHotel()

        offer3 = HotelOffersResponse()
        offer1.hotelId = "hotel3"
        offer3.hotelName = "hotel3"
        offer3.latitude = 101.0
        offer3.longitude = 152.0
        offer3.hotelRoomResponse = emptyList()
    }

    @Test fun strikeThroughPriceShouldShow() {
        val chargeableRateInfo = offer1.hotelRoomResponse.get(0).rateInfo.chargeableRateInfo
        val df = DecimalFormat("#")
        chargeableRateInfo.priceToShowUsers = 110f
        chargeableRateInfo.strikethroughPriceToShowUsers = chargeableRateInfo.priceToShowUsers + 10f
        vm.hotelOffersSubject.onNext(offer1)
        assertEquals("$" + df.format(chargeableRateInfo.strikethroughPriceToShowUsers), vm.strikeThroughPriceObservable.value)
    }

    @Test fun strikeThroughPriceShouldNotShow() {
        val chargeableRateInfo = offer1.hotelRoomResponse.get(0).rateInfo.chargeableRateInfo
        chargeableRateInfo.priceToShowUsers = 110f
        chargeableRateInfo.strikethroughPriceToShowUsers = chargeableRateInfo.priceToShowUsers - 10f
        vm.hotelOffersSubject.onNext(offer1)
        assertNull(vm.strikeThroughPriceObservable.value)
    }

    @Test fun priceShownToCustomerIncludesCustomerFees() {
        vm.hotelOffersSubject.onNext(offer2)
        val df = DecimalFormat("#")
        val expectedPrice = "$" + df.format(expectedTotalPriceWithMandatoryFees)
        assertEquals(expectedPrice, vm.totalPriceObservable.value)
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

//  TODO: Fix the test.
//      @Test fun expandNextAvailableRoomOnSoldOut() {
//        vm.hotelOffersSubject.onNext(offer1)
//
//        val hotelRoomRateViewModels = ArrayList<HotelRoomRateViewModel>()
//        (1..3).forEach {
//            hotelRoomRateViewModels.add(HotelRoomRateViewModel(RuntimeEnvironment.application, offer1.hotelId, offer1.hotelRoomResponse.first(), "", it, PublishSubject.create(), endlessObserver { }))
//        }
//        vm.hotelRoomRateViewModelsObservable.onNext(hotelRoomRateViewModels)
//
//        val expandRoom0TestSubscriber = TestSubscriber.create<Boolean>()
//        hotelRoomRateViewModels.get(0).expandRoomObservable.subscribe(expandRoom0TestSubscriber)
//        val expandRoom1TestSubscriber = TestSubscriber.create<Boolean>()
//        hotelRoomRateViewModels.get(1).expandRoomObservable.subscribe(expandRoom1TestSubscriber)
//        val expandRoom2TestSubscriber = TestSubscriber.create<Boolean>()
//        hotelRoomRateViewModels.get(2).expandRoomObservable.subscribe(expandRoom2TestSubscriber)
//
//        vm.rowExpandingObservable.onNext(0)
//        vm.selectedRoomSoldOut.onNext(Unit)
//
//        vm.rowExpandingObservable.onNext(1)
//        vm.selectedRoomSoldOut.onNext(Unit)
//
//        expandRoom0TestSubscriber.assertNoValues()
//        expandRoom1TestSubscriber.assertValues(false)
//        expandRoom2TestSubscriber.assertValues(false)
//    }

    @Test fun allRoomsSoldOutSignal() {
        vm.hotelOffersSubject.onNext(offer1)

        val hotelSoldOutTestSubscriber = TestSubscriber.create<Boolean>()
        vm.hotelSoldOut.subscribe(hotelSoldOutTestSubscriber)

        val hotelRoomRateViewModels = ArrayList<HotelRoomRateViewModel>()
        (1..20).forEach {
            hotelRoomRateViewModels.add(HotelRoomRateViewModel(RuntimeEnvironment.application, offer1.hotelId, offer1.hotelRoomResponse.first(), "", it, PublishSubject.create(), endlessObserver { }))
        }
        vm.hotelRoomRateViewModelsObservable.onNext(hotelRoomRateViewModels)

        hotelRoomRateViewModels.forEach {
            it.roomSoldOut.onNext(true)
        }

        hotelSoldOutTestSubscriber.assertValues(false, false, true)
    }

    @Test fun hotelSoldOutSignal() {
        val hotelSoldOutTestSubscriber = TestSubscriber.create<Boolean>()
        vm.hotelSoldOut.subscribe(hotelSoldOutTestSubscriber)

        vm.hotelOffersSubject.onNext(offer3)

        hotelSoldOutTestSubscriber.assertValues(false, false, true)
    }

    private fun makeHotel(): ArrayList<HotelOffersResponse.HotelRoomResponse> {
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
        rateInfo.chargeableRateInfo.totalPriceWithMandatoryFees = expectedTotalPriceWithMandatoryFees
        hotel.rateInfo = rateInfo

        rooms.add(hotel)
        return rooms
    }
}
