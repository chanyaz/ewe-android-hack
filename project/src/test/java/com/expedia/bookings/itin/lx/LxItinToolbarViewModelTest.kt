package com.expedia.bookings.itin.lx

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LxItinToolbarViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()
    private val toolbarTitleTestObserver = TestObserver<String>()
    private val toolbarSubTitleTestObserver = TestObserver<String>()

    val startTime = "Oct 24"

    lateinit var sut: LxItinToolbarViewModel<MockItinLxToolbarScope>

    @Before
    fun setup() {
        sut = LxItinToolbarViewModel(MockItinLxToolbarScope())
    }

    @Test
    fun itinObserverTest() {
        sut.toolbarSubTitleSubject.subscribe(toolbarSubTitleTestObserver)
        toolbarSubTitleTestObserver.assertNoValues()

        sut.itinObserver.onChanged(ItinMocker.lxDetailsHappy)

        toolbarSubTitleTestObserver.assertValue(startTime)
    }

    @Test
    fun itinLxObserverTest() {
        val lx = ItinMocker.lxDetailsHappy.firstLx()
        sut.toolbarTitleSubject.subscribe(toolbarTitleTestObserver)
        toolbarTitleTestObserver.assertNoValues()
        sut.itinLxObserver.onChanged(lx)
        toolbarTitleTestObserver.assertValue("somePhraseString")
    }
}

class MockItinLxToolbarScope : HasLxRepo, HasStringProvider, HasLifecycleOwner {
    override val strings: StringSource = MockStringProvider()
    override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    override val itinLxRepo: ItinLxRepoInterface = MockLxRepo()
}
