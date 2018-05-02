package com.expedia.bookings.itin.repositories

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepo
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ItinHotelRepoTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    lateinit var sut: ItinHotelRepo
    val observable = PublishSubject.create<MutableList<ItinCardData>>()
    lateinit var observer: Observer<MutableList<ItinCardData>>

    @Test
    fun onNextHappy() {
        sut = ItinHotelRepo("testid", MockReadJsonUtil(), observable)
        assertEquals(ItinMocker.hotelDetailsHappy, sut.liveDataItin.value)
        assertEquals(ItinMocker.hotelDetailsHappy.firstHotel(), sut.liveDataHotel.value)
        sut.syncObserver.onNext(mutableListOf())
        assertEquals(ItinMocker.hotelDetailsNoPriceDetails, sut.liveDataItin.value)
        assertEquals(ItinMocker.hotelDetailsNoPriceDetails.firstHotel(), sut.liveDataHotel.value)
    }

    @Test
    fun onNextNull() {
        sut = ItinHotelRepo("testid", MockNullSendingReadJsonUtil, observable)
        observer = sut.syncObserver
        var count = 0
        sut.liveDataInvalidItin.observeForever {
            count++
        }
        assertEquals(1, count)
        assertEquals(null, sut.liveDataItin.value)
        assertEquals(null, sut.liveDataHotel.value)
        observer.onNext(mutableListOf<ItinCardData>())
        assertEquals(null, sut.liveDataItin.value)
        assertEquals(null, sut.liveDataHotel.value)
        assertEquals(2, count)
    }

    @Test
    fun nullInit() {
        sut = ItinHotelRepo("testid", MockNullSendingReadJsonUtil, observable)
        assertEquals(null, sut.liveDataItin.value)
        assertEquals(null, sut.liveDataHotel.value)
        assertEquals(Unit, sut.liveDataInvalidItin.value)
    }

    class MockReadJsonUtil : IJsonToItinUtil {
        var first = true
        override fun getItin(itinId: String?): Itin? {
            if (first) {
                first = false
                return ItinMocker.hotelDetailsHappy
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
