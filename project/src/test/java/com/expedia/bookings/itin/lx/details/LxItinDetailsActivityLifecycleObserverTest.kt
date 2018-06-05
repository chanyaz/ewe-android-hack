package com.expedia.bookings.itin.lx.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.common.ItinImageViewModel
import com.expedia.bookings.itin.common.ItinManageBookingWidgetViewModel
import com.expedia.bookings.itin.common.ItinMapWidgetViewModel
import com.expedia.bookings.itin.common.ItinRedeemVoucherViewModel
import com.expedia.bookings.itin.common.ItinTimingsWidgetViewModel
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockActivityLauncher
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockPhoneHandler
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockToaster
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.lx.LxItinToolbarViewModel
import com.expedia.bookings.itin.lx.MockItinLxToolbarScope
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasItinId
import com.expedia.bookings.itin.scopes.HasItinImageViewModelSetter
import com.expedia.bookings.itin.scopes.HasItinTimingsViewModelSetter
import com.expedia.bookings.itin.scopes.HasJsonUtil
import com.expedia.bookings.itin.scopes.HasManageBookingWidgetViewModelSetter
import com.expedia.bookings.itin.scopes.HasMapWidgetViewModelSetter
import com.expedia.bookings.itin.scopes.HasPhoneHandler
import com.expedia.bookings.itin.scopes.HasRedeemVoucherViewModelSetter
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasToaster
import com.expedia.bookings.itin.scopes.HasToolbarViewModelSetter
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.scopes.ItinImageViewModelSetter
import com.expedia.bookings.itin.scopes.ItinTimingsViewModelSetter
import com.expedia.bookings.itin.scopes.ManageBookingWidgetViewModelSetter
import com.expedia.bookings.itin.scopes.MapWidgetViewModelSetter
import com.expedia.bookings.itin.scopes.RedeemVoucherViewModelSetter
import com.expedia.bookings.itin.scopes.ToolBarViewModelSetter
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLOB
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.IPhoneHandler
import com.expedia.bookings.itin.utils.IToaster
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking
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

    lateinit var sut: LxItinDetailsActivityLifecycleObserver<TestLifeCycleObsScope<ItinLx>>
    lateinit var cycle: LifecycleOwner
    lateinit var scope: TestLifeCycleObsScope<ItinLx>
    lateinit var repo: MockLxRepo
    lateinit var testObserver: TestObserver<Unit>

    @Before
    fun setup() {
        testObserver = TestObserver()
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
        assertFalse(scope.tripsTracker.trackItinLxCalled)
        assertFalse(scope.redeemVoucher.called)
        assertFalse(scope.mockImage.called)
        assertFalse(scope.mockTimings.called)

        sut.onCreate(cycle)

        assertTrue(scope.mockMangeBooking.called)
        assertTrue(scope.toolbarMock.called)
        assertTrue(scope.tripsTracker.trackItinLxCalled)
        assertTrue(scope.redeemVoucher.called)
        assertTrue(scope.mockImage.called)
        assertTrue(scope.mockTimings.called)
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

    class TestLifeCycleObsScope<T : ItinLOB> : HasStringProvider, HasWebViewLauncher, HasActivityLauncher, HasJsonUtil, HasItinId, HasToolbarViewModelSetter, HasManageBookingWidgetViewModelSetter, HasTripsTracking, HasMapWidgetViewModelSetter<T>, HasRedeemVoucherViewModelSetter, HasToaster, HasPhoneHandler, HasItinImageViewModelSetter<T>, HasItinTimingsViewModelSetter<T> {
        val mockPhoneHandler = MockPhoneHandler()
        override val phoneHandler: IPhoneHandler = mockPhoneHandler
        val mockImage = MockImageSetter<T>()
        override val itinImage: ItinImageViewModelSetter<T> = mockImage
        val mockToaster = MockToaster()
        override val toaster: IToaster = mockToaster
        override val map: MapWidgetViewModelSetter<T> = MockMapSetter()
        val tripsTracker = MockTripsTracking()
        override val tripsTracking: ITripsTracking = tripsTracker
        override val strings: StringSource = MockStringProvider()
        override val webViewLauncher: IWebViewLauncher = MockWebViewLauncher()
        override val activityLauncher: IActivityLauncher = MockActivityLauncher()
        override val jsonUtil: IJsonToItinUtil = MockReadJsonUtil()
        override val id: String = "007"
        val mockMangeBooking = MockManageBookingSetter()
        override val manageBooking: ManageBookingWidgetViewModelSetter = mockMangeBooking
        val toolbarMock = MockToolbarSetter()
        override val toolbar: ToolBarViewModelSetter = toolbarMock
        override val redeemVoucher: MockRedeemVoucherSetter = MockRedeemVoucherSetter()
        val mockTimings = MockTimingsSetter<T>()
        override val itinTimings: ItinTimingsViewModelSetter<T> = mockTimings
    }

    class MockReadJsonUtil : IJsonToItinUtil {
        var called = false
        override fun getItin(itinId: String?): Itin? {
            called = true
            return ItinMocker.lxDetailsHappy
        }
    }

    class MockMapSetter<T : ItinLOB> : MapWidgetViewModelSetter<T> {
        var called = false
        override fun setUpViewModel(vm: ItinMapWidgetViewModel<T>) {
            called = true
        }
    }

    class MockTimingsSetter<T : ItinLOB> : ItinTimingsViewModelSetter<T> {
        var called = false
        override fun setupViewModel(vm: ItinTimingsWidgetViewModel<T>) {
            called = true
        }
    }

    class MockRedeemVoucherSetter : RedeemVoucherViewModelSetter {
        var called = false
        override fun setUpViewModel(vm: ItinRedeemVoucherViewModel) {
            called = true
        }
    }

    class MockManageBookingSetter : ManageBookingWidgetViewModelSetter {
        override fun setUpViewModel(vm: ItinManageBookingWidgetViewModel) {
            called = true
        }

        var called = false
    }

    class MockToolbarSetter : ToolBarViewModelSetter {
        var called = false
        override fun setUpViewModel(vm: NewItinToolbarViewModel) {
            called = true
        }
    }

    class MockImageSetter<T : ItinLOB> : ItinImageViewModelSetter<T> {
        override fun setupViewModel(vm: ItinImageViewModel<T>) {
            called = true
        }

        var called = false
    }
}
