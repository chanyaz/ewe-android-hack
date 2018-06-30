package com.expedia.bookings.itin.cruise.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CruiseItinTimingsWidgetViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    lateinit var startTitleTestObserver: TestObserver<String>
    lateinit var startDateTestObserver: TestObserver<String>
    lateinit var startTimeTestObserver: TestObserver<String>
    lateinit var endTitleTestObserver: TestObserver<String>
    lateinit var endDateTestObserver: TestObserver<String>
    lateinit var endTimeTestObserver: TestObserver<String>
    lateinit var vm: CruiseItinTimingsWidgetViewModel<MockScope>

    @Before
    fun setup() {
        startTitleTestObserver = TestObserver()
        startDateTestObserver = TestObserver()
        startTimeTestObserver = TestObserver()
        endTitleTestObserver = TestObserver()
        endDateTestObserver = TestObserver()
        endTimeTestObserver = TestObserver()
        setupViewModel()
    }

    @After
    fun dispose() {
        startTitleTestObserver.dispose()
        startDateTestObserver.dispose()
        startTimeTestObserver.dispose()
        endTitleTestObserver.dispose()
        endDateTestObserver.dispose()
        endTimeTestObserver.dispose()
    }

    @Test
    fun testTimingsHappyPath() {
        startTitleTestObserver.assertNoValues()
        startDateTestObserver.assertNoValues()
        startTimeTestObserver.assertNoValues()
        endTitleTestObserver.assertNoValues()
        endDateTestObserver.assertNoValues()
        endTimeTestObserver.assertNoValues()

        vm.itinObserver.onChanged(ItinMocker.cruiseDetailsHappy)

        startTitleTestObserver.assertValue(R.string.itin_cruise_embarkation_title.toString())
        startDateTestObserver.assertValue("Sun, 23 Sep")
        startTimeTestObserver.assertValue("4:00 PM")
        endTitleTestObserver.assertValue(R.string.itin_cruise_disembarkation_title.toString())
        endDateTestObserver.assertValue("Sun, 30 Sep")
        endTimeTestObserver.assertValue("8:00 AM")
    }

    @Test
    fun testEmptyTripTimings() {
        startTitleTestObserver.assertNoValues()
        startDateTestObserver.assertNoValues()
        startTimeTestObserver.assertNoValues()
        endTitleTestObserver.assertNoValues()
        endDateTestObserver.assertNoValues()
        endTimeTestObserver.assertNoValues()

        vm.itinObserver.onChanged(ItinMocker.emptyTrip)

        startTitleTestObserver.assertValue(R.string.itin_cruise_embarkation_title.toString())
        startDateTestObserver.assertNoValues()
        startTimeTestObserver.assertNoValues()
        endTitleTestObserver.assertValue(R.string.itin_cruise_disembarkation_title.toString())
        endDateTestObserver.assertNoValues()
        endTimeTestObserver.assertNoValues()
    }

    private fun setupViewModel() {
        vm = CruiseItinTimingsWidgetViewModel(MockScope())
        vm.startTitleSubject.subscribe(startTitleTestObserver)
        vm.startDateSubject.subscribe(startDateTestObserver)
        vm.startTimeSubject.subscribe(startTimeTestObserver)
        vm.endTitleSubject.subscribe(endTitleTestObserver)
        vm.endDateSubject.subscribe(endDateTestObserver)
        vm.endTimeSubject.subscribe(endTimeTestObserver)
    }

    class MockScope : HasItinRepo, HasLifecycleOwner, HasStringProvider {
        override val strings: StringSource = MockStringProvider()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val itinRepo: ItinRepoInterface = MockItinRepo()
    }
}
