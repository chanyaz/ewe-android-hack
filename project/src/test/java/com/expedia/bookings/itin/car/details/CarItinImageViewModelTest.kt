package com.expedia.bookings.itin.car.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.cars.ItinCarRepoInterface
import com.expedia.bookings.itin.cars.details.CarItinImageViewModel
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockCarRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.tripstore.extensions.firstCar
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CarItinImageViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var sut: CarItinImageViewModel<MockScope>
    val nameTestObserver = TestObserver<String>()
    val urlTestObserver = TestObserver<String>()

    @Before
    fun setup() {
        sut = CarItinImageViewModel(MockScope())
        sut.nameSubject.subscribe(nameTestObserver)
        sut.imageUrlSubject.subscribe(urlTestObserver)
    }

    @Test
    fun happyTest() {
        nameTestObserver.assertNoValues()
        urlTestObserver.assertNoValues()

        sut.itinLOBObserver.onChanged(ItinMocker.carDetailsHappy.firstCar())

        nameTestObserver.assertValue("Thrifty")
        urlTestObserver.assertValue("https://images.trvl-media.com/cars%2F45%2FECMRToyotaYaris_ZT_AUS_20160405_s.jpg")
    }

    @Test
    fun unHappyTest() {
        nameTestObserver.assertNoValues()
        urlTestObserver.assertNoValues()

        sut.itinLOBObserver.onChanged(ItinMocker.carDetailsBadNameAndImage.firstCar())

        nameTestObserver.assertNoValues()
        urlTestObserver.assertNoValues()
    }

    private class MockScope : HasCarRepo, HasLifecycleOwner {
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val itinCarRepo: ItinCarRepoInterface = MockCarRepo()
    }
}
