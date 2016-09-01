package com.expedia.bookings.test

import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.TravelerPickerViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class HotelTravelerPickerTest {
    var vm: TravelerPickerViewModel by Delegates.notNull()
    private var LOTS_MORE: Long = 100

    @Before
    fun before() {
        val context = RuntimeEnvironment.application
        vm = TravelerPickerViewModel(context)
    }

    @Test
    fun defaults() {
        val testSubscriber = TestSubscriber<TravelerParams>(1)
        vm.travelerParamsObservable.subscribe(testSubscriber)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValues(TravelerParams(1, emptyList(), emptyList(), emptyList()))
    }

    @Test
    fun simpleClicks() {
        val testSubscriber = TestSubscriber<TravelerParams>()
        val expected = arrayListOf<TravelerParams>()

        vm.travelerParamsObservable.subscribe(testSubscriber)
        expected.add(TravelerParams(1, emptyList(), emptyList(), emptyList()))

        vm.incrementAdultsObserver.onNext(Unit)
        expected.add(TravelerParams(2, emptyList(), emptyList(), emptyList()))

        vm.decrementAdultsObserver.onNext(Unit)
        expected.add(TravelerParams(1, emptyList(), emptyList(), emptyList()))

        vm.incrementChildrenObserver.onNext(Unit)
        expected.add(TravelerParams(1, listOf(10), emptyList(), emptyList()))

        vm.decrementChildrenObserver.onNext(Unit)
        expected.add(TravelerParams(1, emptyList(), emptyList(), emptyList()))

        testSubscriber.requestMore(LOTS_MORE)
        testSubscriber.assertReceivedOnNext(expected)
    }

    @Test
    fun lowerBounds() {
        val testSubscriber = TestSubscriber<TravelerParams>()
        val expected = arrayListOf<TravelerParams>()

        vm.travelerParamsObservable.subscribe(testSubscriber)
        expected.add(TravelerParams(1, emptyList(), emptyList(), emptyList()))

        // Don't fire events
        vm.decrementAdultsObserver.onNext(Unit)
        vm.decrementChildrenObserver.onNext(Unit)

        testSubscriber.requestMore(LOTS_MORE)
        testSubscriber.assertReceivedOnNext(expected)
    }

    @Test
    fun adultUpperBounds() {
        val testSubscriber = TestSubscriber<TravelerParams>()
        val expected = arrayListOf<TravelerParams>()

        vm.travelerParamsObservable.subscribe(testSubscriber)
        expected.add(TravelerParams(1, emptyList(), emptyList(), emptyList()))

        for (i in 2..6) {
            vm.incrementAdultsObserver.onNext(Unit)
            expected.add(TravelerParams(i, emptyList(), emptyList(), emptyList()))
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
        val testSubscriber = TestSubscriber<TravelerParams>()
        val expected = arrayListOf<TravelerParams>()

        vm.travelerParamsObservable.subscribe(testSubscriber)
        expected.add(TravelerParams(1, emptyList(), emptyList(), emptyList()))

        // Max children is 4
        for (i in 1..4) {
            vm.incrementChildrenObserver.onNext(Unit)
            expected.add(TravelerParams(1, Array(i, { 10 }).toList(), emptyList(), emptyList()))
        }

        vm.incrementChildrenObserver.onNext(Unit)
        vm.incrementChildrenObserver.onNext(Unit)

        // We can add 1 more adult
        vm.incrementAdultsObserver.onNext(Unit)
        expected.add(TravelerParams(2, listOf(10, 10, 10, 10), emptyList(), emptyList()))

        // But no more adults
        vm.incrementAdultsObserver.onNext(Unit)

        testSubscriber.requestMore(LOTS_MORE)
        testSubscriber.assertReceivedOnNext(expected)
    }

    @Test
    fun childrenAgeChange() {
        val testSubscriber = TestSubscriber<TravelerParams>()
        val expected = arrayListOf<TravelerParams>()

        vm.travelerParamsObservable.subscribe(testSubscriber)
        expected.add(TravelerParams(1, emptyList(), emptyList(), emptyList()))

        for (i in 1..2) {
            vm.incrementChildrenObserver.onNext(Unit)
            expected.add(TravelerParams(1, Array(i, { 10 }).toList(), emptyList(), emptyList()))
        }

        // Change age of first child
        vm.childAgeSelectedObserver.onNext(Pair(0, 1))
        expected.add(TravelerParams(1, listOf(1, 10), emptyList(), emptyList()))

        // Change age of second child
        vm.childAgeSelectedObserver.onNext(Pair(1, 5))
        expected.add(TravelerParams(1, listOf(1, 5), emptyList(), emptyList()))


        testSubscriber.requestMore(LOTS_MORE)
        testSubscriber.assertReceivedOnNext(expected)
    }
}
