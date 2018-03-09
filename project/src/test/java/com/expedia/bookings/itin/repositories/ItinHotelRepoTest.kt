package com.expedia.bookings.itin.repositories

import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.mocke3.mockObject
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinHotelRepoTest {

    lateinit var sut: ItinHotelRepo
    val observable = PublishSubject.create<MutableList<ItinCardData>>()
    lateinit var observer: Observer<MutableList<ItinCardData>>

    @Test
    fun onNextHappy() {
        MockReadJsonUtil.first = true
        sut = ItinHotelRepo("testid", MockReadJsonUtil, observable)
        assertEquals(ItinMocker.hotelDetailsHappy, sut.liveDataItin.value)
        assertEquals(ItinMocker.hotelDetailsHappy.firstHotel(), sut.liveDataHotel.value)
        MockReadJsonUtil.first = false
        sut.syncObserver.onNext(mutableListOf<ItinCardData>())
        assertEquals(ItinMocker.hotelDetailsNoPriceDetails, sut.liveDataItin.value)
        assertEquals(ItinMocker.hotelDetailsNoPriceDetails.firstHotel(), sut.liveDataHotel.value)
    }

    @Test
    fun onNextNull() {
        sut = ItinHotelRepo("testid", MockNullSendingReadJsonUtil, observable)
        observer = sut.syncObserver
        assertEquals(null, sut.liveDataItin.value)
        assertEquals(null, sut.liveDataHotel.value)
        observer.onNext(mutableListOf<ItinCardData>())
        assertEquals(null, sut.liveDataItin.value)
        assertEquals(null, sut.liveDataHotel.value)
    }

    object MockReadJsonUtil : IJsonToItinUtil {
        var first = true
        override fun getItin(itinId: String?): Itin? {
            if (first) {
                return mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_for_mocker.json")?.itin!!
            } else {
                return ItinMocker.hotelDetailsNoPriceDetails
            }
        }
    }

    object MockNullSendingReadJsonUtil : IJsonToItinUtil {
        override fun getItin(itinId: String?): Itin? {
            return null
        }
    }
}
