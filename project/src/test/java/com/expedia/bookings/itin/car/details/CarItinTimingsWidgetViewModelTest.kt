package com.expedia.bookings.itin.car.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.cars.details.CarItinTimingsWidgetViewModel
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
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
    private val endTitleTestObserver = TestObserver<String>()
    private val endDateTestObserver = TestObserver<String>()
    private val endTimeTestObserver = TestObserver<String>()
    private val startTimeObserver = TestObserver<String>()
    private val startDateTestObserver = TestObserver<String>()
    private val startTitleTestObserver = TestObserver<String>()

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

        sut.itinObserver.onChanged(ItinMocker.carDetailsHappy)

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

        sut.itinObserver.onChanged(ItinMocker.carDetailsBadPickupAndTimes)

        endTitleTestObserver.assertValue(R.string.itin_drop_off.toString())
        startTitleTestObserver.assertValue(R.string.itin_pick_up.toString())
        endDateTestObserver.assertNoValues()
        endTimeTestObserver.assertNoValues()
        startTimeObserver.assertNoValues()
        startDateTestObserver.assertNoValues()
    }

    private class MockScope : HasItinRepo, HasLifecycleOwner, HasStringProvider {
        override val strings: StringSource = MockStringProvider()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val itinRepo: ItinRepoInterface = MockItinRepo()
    }
}
