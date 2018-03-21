package com.expedia.bookings.itin.lx

import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.mocke3.mockObject
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ItinLxRepoTest {
    lateinit var sut: ItinLxRepo
    val observable = PublishSubject.create<MutableList<ItinCardData>>()
    lateinit var observer: Observer<MutableList<ItinCardData>>

    @Test
    fun itinIsHappy() {
        MockReadJsonUtil.first = true
        sut = ItinLxRepo("ItinID", MockReadJsonUtil, observable)
        assertEquals(ItinMocker.lxDetailsAlsoHappy, sut.liveDataItin.value)
        assertEquals(ItinMocker.lxDetailsAlsoHappy.firstLx(), sut.liveDataLx.value)
        MockReadJsonUtil.first = false
        sut.syncObserver.onNext(mutableListOf<ItinCardData>())
        assertEquals(ItinMocker.lxDetailsHappy, sut.liveDataItin.value)
        assertEquals(ItinMocker.lxDetailsHappy.firstLx(), sut.liveDataLx.value)
    }

    @Test
    fun itinIsNull() {
        sut = ItinLxRepo("ItinID", MockNullSendingReadJsonUtil, observable)
        observer = sut.syncObserver
        assertEquals(null, sut.liveDataItin.value)
        assertEquals(null, sut.liveDataLx.value)
        observer.onNext(mutableListOf<ItinCardData>())
        assertEquals(null, sut.liveDataItin.value)
        assertEquals(null, sut.liveDataLx.value)
    }

    @Test
    fun disposeTest() {
        sut = ItinLxRepo("ItinID", MockReadJsonUtil, observable)
        assertFalse(sut.syncObserver.isDisposed)
        sut.dispose()
        assertTrue(sut.syncObserver.isDisposed)
    }

    object MockReadJsonUtil : IJsonToItinUtil {
        var first = true
        override fun getItin(itinId: String?): Itin? {
            if (first) {
                return mockObject(ItinDetailsResponse::class.java, "api/trips/lx_trip_details_for_mocker.json")?.itin!!
            } else {
                return ItinMocker.lxDetailsHappy
            }
        }
    }

    object MockNullSendingReadJsonUtil : IJsonToItinUtil {
        override fun getItin(itinId: String?): Itin? {
            return null
        }
    }
}
