package com.expedia.bookings.test

import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.TravelerPickerViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelTravelerPickerTest {
    var vm: TravelerPickerViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = RuntimeEnvironment.application
        vm = TravelerPickerViewModel(context)
    }

    @Test
    fun defaults() {
        val testSubscriber = TestObserver<TravelerParams>(1)
        vm.travelerParamsObservable.subscribe(testSubscriber)

        testSubscriber.assertValueCount(1)
        assertTravelerParamsEquals(TravelerParams(1, emptyList(), emptyList(), emptyList()), testSubscriber.values()[0])
    }

    @Test
    fun simpleClicks() {
        val testSubscriber = TestObserver<TravelerParams>()

        vm.travelerParamsObservable.subscribe(testSubscriber)
        assertTravelerParamsEquals(TravelerParams(1, emptyList(), emptyList(), emptyList()), testSubscriber.values()[0])

        vm.incrementAdultsObserver.onNext(Unit)
        assertTravelerParamsEquals(TravelerParams(2, emptyList(), emptyList(), emptyList()), testSubscriber.values()[1])

        vm.decrementAdultsObserver.onNext(Unit)
        assertTravelerParamsEquals(TravelerParams(1, emptyList(), emptyList(), emptyList()), testSubscriber.values()[2])

        vm.incrementChildrenObserver.onNext(Unit)
        assertTravelerParamsEquals(TravelerParams(1, listOf(10), emptyList(), emptyList()), testSubscriber.values()[3])

        vm.decrementChildrenObserver.onNext(Unit)
        assertTravelerParamsEquals(TravelerParams(1, emptyList(), emptyList(), emptyList()), testSubscriber.values()[4])
    }

    @Test
    fun lowerBounds() {
        val testSubscriber = TestObserver<TravelerParams>()

        vm.travelerParamsObservable.subscribe(testSubscriber)
        assertTravelerParamsEquals(TravelerParams(1, emptyList(), emptyList(), emptyList()), testSubscriber.values()[0])

        // Don't fire events
        vm.decrementAdultsObserver.onNext(Unit)
        vm.decrementChildrenObserver.onNext(Unit)
    }

    @Test
    fun adultUpperBounds() {
        val testSubscriber = TestObserver<TravelerParams>()

        vm.travelerParamsObservable.subscribe(testSubscriber)
        assertTravelerParamsEquals(TravelerParams(1, emptyList(), emptyList(), emptyList()), testSubscriber.values()[0])

        for (i in 2..6) {
            vm.incrementAdultsObserver.onNext(Unit)
            assertTravelerParamsEquals(TravelerParams(i, emptyList(), emptyList(), emptyList()), testSubscriber.values()[i - 1])
        }

        // Hit the max, these should not fire anything
        vm.incrementAdultsObserver.onNext(Unit)
        vm.incrementAdultsObserver.onNext(Unit)

        // Can't add children if we have max adults
        vm.incrementChildrenObserver.onNext(Unit)
        testSubscriber.assertValueCount(6)

    }

    @Test
    fun childrenUpperBounds() {
        val testSubscriber = TestObserver<TravelerParams>()

        vm.travelerParamsObservable.subscribe(testSubscriber)
        assertTravelerParamsEquals(TravelerParams(1, emptyList(), emptyList(), emptyList()), testSubscriber.values()[0])

        // Max children is 4
        for (i in 1..4) {
            vm.incrementChildrenObserver.onNext(Unit)
            assertTravelerParamsEquals(TravelerParams(1, Array(i, { 10 }).toList(), emptyList(), emptyList()), testSubscriber.values()[i])
        }

        vm.incrementChildrenObserver.onNext(Unit)
        vm.incrementChildrenObserver.onNext(Unit)

        // We can add 1 more adult
        vm.incrementAdultsObserver.onNext(Unit)
        assertTravelerParamsEquals(TravelerParams(2, listOf(10, 10, 10, 10), emptyList(), emptyList()), testSubscriber.values()[5])

        // But no more adults
        vm.incrementAdultsObserver.onNext(Unit)

        testSubscriber.assertValueCount(6)
    }

    @Test
    fun childrenAgeChange() {
        val testSubscriber = TestObserver<TravelerParams>()

        vm.travelerParamsObservable.subscribe(testSubscriber)
        assertTravelerParamsEquals(TravelerParams(1, emptyList(), emptyList(), emptyList()), testSubscriber.values()[0])

        for (i in 1..2) {
            vm.incrementChildrenObserver.onNext(Unit)
            assertTravelerParamsEquals(TravelerParams(1, Array(i, { 10 }).toList(), emptyList(), emptyList()), testSubscriber.values()[i])
        }

        // Change age of first child
        vm.childAgeSelectedObserver.onNext(Pair(0, 1))
        assertTravelerParamsEquals(TravelerParams(1, listOf(1, 10), emptyList(), emptyList()), testSubscriber.values()[3])

        // Change age of second child
        vm.childAgeSelectedObserver.onNext(Pair(1, 5))
        assertTravelerParamsEquals(TravelerParams(1, listOf(1, 5), emptyList(), emptyList()), testSubscriber.values()[4])
    }

    private fun assertTravelerParamsEquals(expected: TravelerParams, actual: TravelerParams) {
        assertEquals(expected.numberOfAdults, actual.numberOfAdults)
        assertEquals(expected.childrenAges.size, actual.childrenAges.size)
        assertEquals(expected.seniorAges.size, actual.seniorAges.size)
        assertEquals(expected.youthAges.size, actual.youthAges.size)
    }
}
