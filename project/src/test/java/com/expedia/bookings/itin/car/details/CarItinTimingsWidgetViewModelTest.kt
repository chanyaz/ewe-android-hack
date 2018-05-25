package com.expedia.bookings.itin.car.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.cars.ItinCarRepoInterface
import com.expedia.bookings.itin.cars.details.CarItinTimingsWidgetViewModel
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockCarRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.extensions.firstCar
import com.expedia.bookings.itin.utils.StringSource
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CarItinTimingsWidgetViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()
    private lateinit var sut: CarItinTimingsWidgetViewModel<MockScope>
    val endTitleTestObserver = TestObserver<String>()
    val endDateTestObserver = TestObserver<String>()
    val endTimeTestObserver = TestObserver<String>()
    val startTimeObserver = TestObserver<String>()
    val startDateTestObserver = TestObserver<String>()
    val startTitleTestObserver = TestObserver<String>()

    @Before
    fun setup() {
        sut = CarItinTimingsWidgetViewModel(MockScope())
        sut.endDateSubject.subscribe(endDateTestObserver)
        sut.endTitleSubject.subscribe(endTitleTestObserver)
        sut.endTimeSubject.subscribe(endTimeTestObserver)
        sut.startDateSubject.subscribe(startDateTestObserver)
        sut.startTitleSubject.subscribe(startTitleTestObserver)
        sut.startTimeSubject.subscribe(startTimeObserver)
    }

    @Test
    fun happyPathTest() {
        endTitleTestObserver.assertNoValues()
        endDateTestObserver.assertNoValues()
        endTimeTestObserver.assertNoValues()
        startTimeObserver.assertNoValues()
        startDateTestObserver.assertNoValues()
        startTitleTestObserver.assertNoValues()

        sut.itinObserver.onChanged(ItinMocker.carDetailsHappy.firstCar())

        endTimeTestObserver.assertValue("5:00pm")
        endDateTestObserver.assertValue("Sat, Apr 15")
        endTitleTestObserver.assertValue(R.string.itin_drop_off.toString())
        startTimeObserver.assertValue("7:30am")
        startDateTestObserver.assertValue("Sat, Apr 15")
        startTitleTestObserver.assertValue(R.string.itin_pick_up.toString())
    }

    @Test
    fun unhappyPathTest() {
        endTitleTestObserver.assertNoValues()
        endDateTestObserver.assertNoValues()
        endTimeTestObserver.assertNoValues()
        startTimeObserver.assertNoValues()
        startDateTestObserver.assertNoValues()
        startTitleTestObserver.assertNoValues()

        sut.itinObserver.onChanged(ItinMocker.carDetailsBadPickupAndTimes.firstCar())

        endTitleTestObserver.assertValue(R.string.itin_drop_off.toString())
        startTitleTestObserver.assertValue(R.string.itin_pick_up.toString())
        endDateTestObserver.assertNoValues()
        endTimeTestObserver.assertNoValues()
        startTimeObserver.assertNoValues()
        startDateTestObserver.assertNoValues()
    }

    private class MockScope: HasCarRepo, HasLifecycleOwner, HasStringProvider {
        override val strings: StringSource = MockStringProvider()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val itinCarRepo: ItinCarRepoInterface = MockCarRepo()
    }
}
