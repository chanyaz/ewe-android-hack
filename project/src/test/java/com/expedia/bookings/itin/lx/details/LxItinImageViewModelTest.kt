package com.expedia.bookings.itin.lx.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
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

class LxItinImageViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var sut: LxItinImageViewModel<MockScope>
    private val nameTestObserver = TestObserver<String>()
    private val urlTestObserver = TestObserver<String>()

    @Before
    fun setup() {
        sut = LxItinImageViewModel(MockScope())
        sut.nameSubject.subscribe(nameTestObserver)
        sut.imageUrlSubject.subscribe(urlTestObserver)
    }

    @Test
    fun happyTest() {
        nameTestObserver.assertNoValues()
        urlTestObserver.assertNoValues()

        sut.itinObserver.onChanged(ItinMocker.lxDetailsHappy)

        nameTestObserver.assertValue("Day Trip to New York by Train with Hop-on Hop-Off Pass: Full-Day Excursion")
        urlTestObserver.assertValue("https://s3.amazonaws.com/mediavault.le/media/d845b674a00c4ec7dc685942d31f955b2e354f73.jpeg")
    }

    @Test
    fun unHappyTest() {
        nameTestObserver.assertNoValues()
        urlTestObserver.assertNoValues()

        sut.itinObserver.onChanged(ItinMocker.lxDetailsNoLat)

        nameTestObserver.assertNoValues()
        urlTestObserver.assertNoValues()
    }

    private class MockScope : HasItinRepo, HasLifecycleOwner {
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val itinRepo: ItinRepoInterface = MockItinRepo()
    }
}
