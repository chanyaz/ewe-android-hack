package com.expedia.bookings.itin.car.details

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.cars.ItinCarRepoInterface
import com.expedia.bookings.itin.cars.details.CarItinToolbarViewModel
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockCarRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.extensions.firstCar
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.Before
import org.junit.Test

class CarItinToolbarViewModelTest {
    private lateinit var sut: CarItinToolbarViewModel<MockScope>
    private lateinit var mockScope: MockScope
    private val toolbarTitleTestObserver = TestObserver<String>()
    private val toolbarSubTitleTestObserver = TestObserver<String>()

    @Before
    fun setup() {
        mockScope = MockScope()
        sut = CarItinToolbarViewModel(mockScope)
        sut.toolbarSubTitleSubject.subscribe(toolbarSubTitleTestObserver)
        sut.toolbarTitleSubject.subscribe(toolbarTitleTestObserver)
    }

    @Test
    fun happyTest() {
        toolbarTitleTestObserver.assertNoValues()
        toolbarSubTitleTestObserver.assertNoValues()

        sut.itinCarObserver.onChanged(ItinMocker.carDetailsHappy.firstCar())

        toolbarTitleTestObserver.assertValue((R.string.itin_car_toolbar_title_TEMPLATE).toString().plus(mapOf("location" to "Sydney")))
        toolbarSubTitleTestObserver.assertValue((R.string.itin_car_toolbar_subtitle_date_to_date_TEMPLATE).toString().plus(mapOf("startdate" to "Apr 15", "enddate" to "Apr 15")))
    }

    @Test
    fun unhappyTest() {
        toolbarTitleTestObserver.assertNoValues()
        toolbarSubTitleTestObserver.assertNoValues()

        sut.itinCarObserver.onChanged(ItinMocker.carDetailsBadPickupAndTimes.firstCar())

        toolbarTitleTestObserver.assertNoValues()
        toolbarSubTitleTestObserver.assertNoValues()
    }

    private class MockScope : HasCarRepo, HasStringProvider, HasLifecycleOwner {
        override val strings: StringSource = MockStringProvider()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val itinCarRepo: ItinCarRepoInterface = MockCarRepo()
    }
}
