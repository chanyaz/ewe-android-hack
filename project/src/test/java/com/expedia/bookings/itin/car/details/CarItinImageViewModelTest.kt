package com.expedia.bookings.itin.car.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.cars.details.CarItinImageViewModel
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
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

        sut.itinObserver.onChanged(ItinMocker.carDetailsHappy)

        nameTestObserver.assertValue("Thrifty")
        urlTestObserver.assertValue("https://images.trvl-media.com/cars%2F45%2FECMRToyotaYaris_ZT_AUS_20160405_s.jpg")
    }

    @Test
    fun unHappyTest() {
        nameTestObserver.assertNoValues()
        urlTestObserver.assertNoValues()

        sut.itinObserver.onChanged(ItinMocker.carDetailsBadNameAndImage)

        nameTestObserver.assertNoValues()
        urlTestObserver.assertNoValues()
    }

    private class MockScope : HasItinRepo, HasLifecycleOwner {
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val itinRepo: ItinRepoInterface = MockItinRepo()
    }
}
