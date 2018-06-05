package com.expedia.bookings.itin.lx.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LxItinImageViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var sut: LxItinImageViewModel<MockScope>
    val nameTestObserver = TestObserver<String>()
    val urlTestObserver = TestObserver<String>()

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

        sut.itinLOBObserver.onChanged(ItinMocker.lxDetailsHappy.firstLx())

        nameTestObserver.assertValue("Day Trip to New York by Train with Hop-on Hop-Off Pass: Full-Day Excursion")
        urlTestObserver.assertValue("https://s3.amazonaws.com/mediavault.le/media/d845b674a00c4ec7dc685942d31f955b2e354f73.jpeg")
    }

    @Test
    fun unHappyTest() {
        nameTestObserver.assertNoValues()
        urlTestObserver.assertNoValues()

        sut.itinLOBObserver.onChanged(ItinMocker.lxDetailsNoLat.firstLx())

        nameTestObserver.assertNoValues()
        urlTestObserver.assertNoValues()
    }

    private class MockScope : HasLxRepo, HasLifecycleOwner {
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val itinLxRepo: ItinLxRepoInterface = MockLxRepo()
    }
}
