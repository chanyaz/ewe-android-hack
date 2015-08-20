package com.expedia.bookings.test

import android.content.Context
import android.content.res.Resources
import com.expedia.vm.HotelTravelerParams
import com.expedia.vm.HotelTravelerPickerViewModel
import org.junit.Before
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito
import rx.observers.TestSubscriber
import kotlin.properties.Delegates

public class HotelTravelerPickerTest {
    public var vm: HotelTravelerPickerViewModel by Delegates.notNull()
    private var LOTS_MORE: Long = 100

    @Before
    fun before() {
        val context = Mockito.mock(javaClass<Context>())
        val resources = Mockito.mock(javaClass<Resources>())
        Mockito.`when`(context.getResources()).thenReturn(resources)
        Mockito.`when`(resources.getQuantityString(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt())).thenReturn("")

        vm = HotelTravelerPickerViewModel(context)
    }

    @Test
    fun defaults() {
        val testSubscriber = TestSubscriber<HotelTravelerParams>(1)
        vm.updateObservable.subscribe(testSubscriber)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValues(HotelTravelerParams(1, emptyList()))
    }

    @Test
    fun simpleClicks() {
        val testSubscriber = TestSubscriber<HotelTravelerParams>()
        val expected = arrayListOf<HotelTravelerParams>()

        vm.updateObservable.subscribe(testSubscriber)
        expected.add(HotelTravelerParams(1, emptyList()))

        vm.incrementAdultsObserver.onNext(Unit)
        expected.add(HotelTravelerParams(2, emptyList()))

        vm.decrementAdultsObserver.onNext(Unit)
        expected.add(HotelTravelerParams(1, emptyList()))

        vm.incrementChildrenObserver.onNext(Unit)
        expected.add(HotelTravelerParams(1, listOf(10)))

        vm.decrementChildrenObserver.onNext(Unit)
        expected.add(HotelTravelerParams(1, emptyList()))

        testSubscriber.requestMore(LOTS_MORE)
        testSubscriber.assertReceivedOnNext(expected)
    }

    @Test
    fun lowerBounds() {
        val testSubscriber = TestSubscriber<HotelTravelerParams>()
        val expected = arrayListOf<HotelTravelerParams>()

        vm.updateObservable.subscribe(testSubscriber)
        expected.add(HotelTravelerParams(1, emptyList()))

        // Don't fire events
        vm.decrementAdultsObserver.onNext(Unit)
        vm.decrementChildrenObserver.onNext(Unit)

        testSubscriber.requestMore(LOTS_MORE)
        testSubscriber.assertReceivedOnNext(expected)
    }

    @Test
    fun adultUpperBounds() {
        val testSubscriber = TestSubscriber<HotelTravelerParams>()
        val expected = arrayListOf<HotelTravelerParams>()

        vm.updateObservable.subscribe(testSubscriber)
        expected.add(HotelTravelerParams(1, emptyList()))

        for (i in 2..6) {
            vm.incrementAdultsObserver.onNext(Unit)
            expected.add(HotelTravelerParams(i, emptyList()))
        }
        testSubscriber.requestMore(LOTS_MORE)
        testSubscriber.assertReceivedOnNext(expected)

        // Hit the max, these should not fire anything
        vm.incrementAdultsObserver.onNext(Unit)
        vm.incrementAdultsObserver.onNext(Unit)

        // Can't add children if we have max adults
        vm.incrementChildrenObserver.onNext(Unit)

        testSubscriber.requestMore(LOTS_MORE)
        testSubscriber.assertReceivedOnNext(expected)
    }

    @Test
    fun childrenUpperBounds() {
        val testSubscriber = TestSubscriber<HotelTravelerParams>()
        val expected = arrayListOf<HotelTravelerParams>()

        vm.updateObservable.subscribe(testSubscriber)
        expected.add(HotelTravelerParams(1, emptyList()))

        // Max children is 4
        for (i in 1..4) {
            vm.incrementChildrenObserver.onNext(Unit)
            expected.add(HotelTravelerParams(1, Array(i, { 10 }).toList()))
        }

        vm.incrementChildrenObserver.onNext(Unit)
        vm.incrementChildrenObserver.onNext(Unit)

        // We can add 1 more adult
        vm.incrementAdultsObserver.onNext(Unit)
        expected.add(HotelTravelerParams(2, listOf(10, 10, 10, 10)))

        // But no more adults
        vm.incrementAdultsObserver.onNext(Unit)

        testSubscriber.requestMore(LOTS_MORE)
        testSubscriber.assertReceivedOnNext(expected)
    }
}
