package com.expedia.bookings.itin.car

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.cars.ItinCarRepo
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstCar
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ItinCarRepoTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    lateinit var sut: ItinCarRepo
    val observable = PublishSubject.create<MutableList<ItinCardData>>()
    lateinit var observer: Observer<MutableList<ItinCardData>>
    lateinit var nullSendingJsonUtil: IJsonToItinUtil
    lateinit var mockJsonUtil: IJsonToItinUtil
    lateinit var mockJson: MockJsonUtil

    @Before
    fun setup() {
        nullSendingJsonUtil = NullJsonUtil()
        mockJson = MockJsonUtil()
        mockJsonUtil = mockJson
    }

    @Test
    fun itinIsHappy() {
        sut = ItinCarRepo("ItinID", mockJsonUtil, observable)
        assertEquals(ItinMocker.carDetailsHappy, sut.liveDataItin.value)
        assertEquals(ItinMocker.carDetailsHappy.firstCar(), sut.liveDataCar.value)
        mockJson.called = true
        sut.syncObserver.onNext(mutableListOf())
        assertEquals(ItinMocker.carDetailsHappyPickupDropOffSame, sut.liveDataItin.value)
        assertEquals(ItinMocker.carDetailsHappyPickupDropOffSame.firstCar(), sut.liveDataCar.value)
    }

    @Test
    fun itinIsNull() {
        sut = ItinCarRepo("ItinID", nullSendingJsonUtil, observable)
        observer = sut.syncObserver
        assertEquals(null, sut.liveDataItin.value)
        assertEquals(null, sut.liveDataCar.value)
        observer.onNext(mutableListOf())
        assertEquals(null, sut.liveDataItin.value)
        assertEquals(null, sut.liveDataCar.value)
    }

    @Test
    fun disposeTest() {
        sut = ItinCarRepo("ItinID", MockJsonUtil(), observable)
        assertFalse(sut.syncObserver.isDisposed)
        sut.dispose()
        assertTrue(sut.syncObserver.isDisposed)
    }

    class MockJsonUtil : IJsonToItinUtil {
        var called = false
        override fun getItin(itinId: String?): Itin? {
            if (!called) {
                return ItinMocker.carDetailsHappy
            } else {
                return ItinMocker.carDetailsHappyPickupDropOffSame
            }
        }
        override fun getItinList(): List<Itin> {
            return emptyList()
        }
    }

    class NullJsonUtil : IJsonToItinUtil {
        override fun getItinList(): List<Itin> {
            return emptyList()
        }

        override fun getItin(itinId: String?): Itin? {
            return null
        }
    }
}
