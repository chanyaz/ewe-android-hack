package com.expedia.bookings.itin.lx.moreHelp

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LxItinMoreHelpToolbarViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var toolbarTitleTestObserver: TestObserver<String>
    private lateinit var toolbarSubTitleTestObserver: TestObserver<String>
    private lateinit var shareIconVisibleTestObserver: TestObserver<Boolean>
    private lateinit var navigationBackPressedTestObserver: TestObserver<Unit>
    private lateinit var shareIconClickedTestObserver: TestObserver<Unit>

    private lateinit var vm: LxItinMoreHelpToolbarViewModel<MockLxItinToolbarScope>

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
        vm.itinLxObserver.onChanged(ItinMocker.lxDetailsAlsoHappy.activities?.first())

        toolbarTitleTestObserver.assertValue(R.string.itin_lx_more_info_heading.toString())
        toolbarSubTitleTestObserver.assertValue("California Academy of Sciences General Admission: General Admission ")
    }

    private fun setupViewModel() {
        vm = LxItinMoreHelpToolbarViewModel(MockLxItinToolbarScope())

        vm.toolbarTitleSubject.subscribe(toolbarTitleTestObserver)
        vm.toolbarSubTitleSubject.subscribe(toolbarSubTitleTestObserver)
        vm.shareIconVisibleSubject.subscribe(shareIconVisibleTestObserver)
        vm.navigationBackPressedSubject.subscribe(navigationBackPressedTestObserver)
        vm.shareIconClickedSubject.subscribe(shareIconClickedTestObserver)
    }

    private class MockLxItinToolbarScope : HasStringProvider, HasLxRepo, HasLifecycleOwner {
        override val strings: StringSource = MockStringProvider()
        override val itinLxRepo: ItinLxRepoInterface = MockLxRepo()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    }
}
