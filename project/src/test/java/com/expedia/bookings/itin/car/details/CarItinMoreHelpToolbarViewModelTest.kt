package com.expedia.bookings.itin.car.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.cars.ItinCarRepoInterface
import com.expedia.bookings.itin.cars.details.CarItinMoreHelpToolbarViewModel
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockCarRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.extensions.firstCar
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse

class CarItinMoreHelpToolbarViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var toolbarTitleTestObserver: TestObserver<String>
    private lateinit var toolbarSubTitleTestObserver: TestObserver<String>
    private lateinit var shareIconVisibleTestObserver: TestObserver<Boolean>
    private lateinit var navigationBackPressedTestObserver: TestObserver<Unit>
    private lateinit var shareIconClickedTestObserver: TestObserver<Unit>

    private lateinit var vm: CarItinMoreHelpToolbarViewModel<MockCarItinToolbarScope>

    @Before
    fun setup() {
        toolbarTitleTestObserver = TestObserver()
        toolbarSubTitleTestObserver = TestObserver()
        shareIconVisibleTestObserver = TestObserver()
        navigationBackPressedTestObserver = TestObserver()
        shareIconClickedTestObserver = TestObserver()
        setupViewModel()
    }

    @Test
    fun testToolbarTitles() {
        toolbarTitleTestObserver.assertNoValues()
        toolbarSubTitleTestObserver.assertNoValues()
        assertFalse(vm.scope.mockStrings.fetchWithPhraseCalled)

        vm.itinCarObserver.onChanged(ItinMocker.carDetailsHappy.cars?.first())

        toolbarTitleTestObserver.assertValue(R.string.itin_car_more_info_heading.toString())

        val expectedString = R.string.itin_car_toolbar_title_TEMPLATE.toString().plus(mapOf("location" to ItinMocker.carDetailsHappy.firstCar()?.pickupLocation?.cityName))

        toolbarSubTitleTestObserver.assertValue(expectedString)
    }

    private fun setupViewModel() {
        vm = CarItinMoreHelpToolbarViewModel(MockCarItinToolbarScope())

        vm.toolbarTitleSubject.subscribe(toolbarTitleTestObserver)
        vm.toolbarSubTitleSubject.subscribe(toolbarSubTitleTestObserver)
        vm.shareIconVisibleSubject.subscribe(shareIconVisibleTestObserver)
        vm.navigationBackPressedSubject.subscribe(navigationBackPressedTestObserver)
        vm.shareIconClickedSubject.subscribe(shareIconClickedTestObserver)
    }

    private class MockCarItinToolbarScope : HasStringProvider, HasLxRepo, HasLifecycleOwner, HasCarRepo {
        override val itinCarRepo: ItinCarRepoInterface = MockCarRepo()
        val mockStrings = MockStringProvider()
        override val strings: StringSource = mockStrings
        override val itinLxRepo: ItinLxRepoInterface = MockLxRepo()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    }
}
