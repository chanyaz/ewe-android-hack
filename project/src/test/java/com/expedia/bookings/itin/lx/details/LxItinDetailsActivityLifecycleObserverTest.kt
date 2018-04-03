package com.expedia.bookings.itin.lx.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockActivityLauncher
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.lx.LxItinToolbarViewModel
import com.expedia.bookings.itin.lx.MockItinLxToolbarScope
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasItinId
import com.expedia.bookings.itin.scopes.HasJsonUtil
import com.expedia.bookings.itin.scopes.HasManageBookingWidgetViewModelSetter
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasToolbarViewModelSetter
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.scopes.LxItinManageBookingWidgetScope
import com.expedia.bookings.itin.scopes.ManageBookingWidgetViewModelSetter
import com.expedia.bookings.itin.scopes.ToolBarViewModelSetter
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LxItinDetailsActivityLifecycleObserverTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    lateinit var sut: LxItinDetailsActivityLifecycleObserver<TestLifeCycleObsScope>
    lateinit var cycle: LifecycleOwner
    lateinit var scope: TestLifeCycleObsScope
    lateinit var repo: MockLxRepo
    lateinit var testObserver: TestObserver<Unit>

    @Before
    fun setup() {
        testObserver = TestObserver<Unit>()
        scope = TestLifeCycleObsScope()
        sut = LxItinDetailsActivityLifecycleObserver(scope)
        cycle = MockLifecycleOwner()
        repo = MockLxRepo()
        sut.repo = repo
    }

    @Test
    fun onCreateTest() {
        sut.finishSubject.subscribe(testObserver)
        testObserver.assertNoValues()

        assertFalse(scope.mockMangeBooking.called)
        assertFalse(scope.toolbarMock.called)

        sut.onCreate(cycle)

        assertTrue(scope.mockMangeBooking.called)
        assertTrue(scope.toolbarMock.called)
        testObserver.assertNoValues()

        sut.repo.invalidDataSubject.onNext(Unit)
        testObserver.assertValue(Unit)
    }

    @Test
    fun onDestroy() {
        assertFalse(repo.disposed)
        sut.onDestroy(cycle)
        assertTrue(repo.disposed)
    }

    @Test
    fun toolbarExitTest() {
        sut.finishSubject.subscribe(testObserver)
        testObserver.assertNoValues()
        sut.toolbarViewModel = LxItinToolbarViewModel(MockItinLxToolbarScope())
        sut.toolbarViewModel.navigationBackPressedSubject.onNext(Unit)
        testObserver.assertValue(Unit)
    }

    class TestLifeCycleObsScope : HasStringProvider, HasWebViewLauncher, HasActivityLauncher, HasJsonUtil, HasItinId, HasToolbarViewModelSetter, HasManageBookingWidgetViewModelSetter {
        override val strings: StringSource = MockStringProvider()
        override val webViewLauncher: IWebViewLauncher = MockWebViewLauncher()
        override val activityLauncher: IActivityLauncher = MockActivityLauncher()
        override val jsonUtil: IJsonToItinUtil = MockReadJsonUtil
        override val id: String = "007"
        val mockMangeBooking = MockManageBookingSetter()
        override val manageBooking: ManageBookingWidgetViewModelSetter = mockMangeBooking
        val toolbarMock = MockToolbarSetter()
        override val toolbar: ToolBarViewModelSetter = toolbarMock
    }

    object MockReadJsonUtil : IJsonToItinUtil {
        var called = false
        override fun getItin(itinId: String?): Itin? {
            called = true
            return ItinMocker.lxDetailsHappy
        }
    }

    class MockManageBookingSetter : ManageBookingWidgetViewModelSetter {
        var called = false
        override fun setUpViewModel(vm: LxItinManageBookingWidgetViewModel<LxItinManageBookingWidgetScope>) {
            called = true
        }
    }

    class MockToolbarSetter : ToolBarViewModelSetter {
        var called = false
        override fun setUpViewModel(vm: NewItinToolbarViewModel) {
            called = true
        }
    }
}
