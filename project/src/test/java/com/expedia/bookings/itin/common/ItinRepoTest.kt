package com.expedia.bookings.itin.common

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ItinRepoTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()
    lateinit var sut: ItinRepo
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
        sut = ItinRepo("ItinID", mockJsonUtil, observable)
        assertEquals(ItinMocker.lxDetailsHappy, sut.liveDataItin.value)
        mockJson.first = true
        sut.syncObserver.onNext(mutableListOf())
        assertEquals(ItinMocker.lxDetailsAlsoHappy, sut.liveDataItin.value)
    }

    @Test
    fun itinIsNull() {
        sut = ItinRepo("ItinID", nullSendingJsonUtil, observable)
        observer = sut.syncObserver
        assertEquals(null, sut.liveDataItin.value)

        observer.onNext(mutableListOf())

        assertEquals(null, sut.liveDataItin.value)
    }

    @Test
    fun disposeTest() {
        sut = ItinRepo("ItinID", MockJsonUtil(), observable)
        assertFalse(sut.syncObserver.isDisposed)
        sut.dispose()
        assertTrue(sut.syncObserver.isDisposed)
    }

    class MockJsonUtil : IJsonToItinUtil {
        var first = false
        override fun getItin(itinId: String?): Itin? {
            if (first) {
                return ItinMocker.lxDetailsAlsoHappy
            } else {
                return ItinMocker.lxDetailsHappy
            }
        }
    }

    class NullJsonUtil : IJsonToItinUtil {
        override fun getItin(itinId: String?): Itin? {
            return null
        }
    }
}
