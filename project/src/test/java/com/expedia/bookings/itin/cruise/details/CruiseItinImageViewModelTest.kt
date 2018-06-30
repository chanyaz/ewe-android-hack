package com.expedia.bookings.itin.cruise.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.services.TestObserver
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CruiseItinImageViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var imageUrlTestObserver: TestObserver<String>
    private lateinit var shipNameTestObserver: TestObserver<String>
    private lateinit var vm: CruiseItinImageViewModel<MockScope>

    @Before
    fun setup() {
        imageUrlTestObserver = TestObserver()
        shipNameTestObserver = TestObserver()
        vm = CruiseItinImageViewModel(MockScope())
        vm.imageUrlSubject.subscribe(imageUrlTestObserver)
        vm.nameSubject.subscribe(shipNameTestObserver)
    }

    @After
    fun dispose() {
        imageUrlTestObserver.dispose()
        shipNameTestObserver.dispose()
    }

    @Test
    fun testImageHappyPath() {
        imageUrlTestObserver.assertNoValues()
        shipNameTestObserver.assertNoValues()

        vm.itinObserver.onChanged(ItinMocker.cruiseDetailsHappy)

        imageUrlTestObserver.assertValue("https://s3.amazonaws.com/mediavault.le/media/ccf3c179d4a84f8adc7c33c303278afe53cad613.jpeg")
        shipNameTestObserver.assertValue("Norwegian Pearl")
    }

    @Test
    fun testEmptyTripImage() {
        imageUrlTestObserver.assertNoValues()
        shipNameTestObserver.assertNoValues()

        vm.itinObserver.onChanged(ItinMocker.emptyTrip)

        imageUrlTestObserver.assertNoValues()
        shipNameTestObserver.assertNoValues()
    }

    private class MockScope : HasItinRepo, HasLifecycleOwner {
        override val itinRepo: ItinRepoInterface = MockItinRepo()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    }
}
