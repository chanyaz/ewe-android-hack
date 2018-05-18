package com.expedia.bookings.itin.lx.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LxItinRedeemVoucherViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()
    private lateinit var sut: LxItinRedeemVoucherViewModel<MockRedeemVoucherViewModelScope>
    private lateinit var scope: MockRedeemVoucherViewModelScope
    val showRedeemObserver = TestObserver<Unit>()
    val lxHappy = ItinMocker.lxDetailsHappy
    val lxNotHappy = ItinMocker.lxDetailsNoDetailsUrl
    val noTripId = ItinMocker.lxDetailsNoTripID
    val lxvoucherUrlReturned = ItinMocker.lxDetailsNoLat
    val voucherUrlReturned = ItinMocker.lxDetailsAlsoHappy

    @Before
    fun setup() {
        scope = MockRedeemVoucherViewModelScope()
        sut = LxItinRedeemVoucherViewModel(scope)
    }

    @Test
    fun redeemUrlWhenBothUrlInResponse() {
        assertEquals(null, scope.webViewLauncerMock.lastSeenURL)

        sut.itinObserver.onChanged(lxHappy)
        sut.redeemVoucherClickSubject.onNext(Unit)
        assertEquals("https://wwwexpediacom.trunk.sb.karmalab.net/things-to-do/voucher/?tripid=b9739936-62a8-49a1-af12-fdbe85d78e5f", scope.webViewLauncerMock.lastSeenURL)
    }

    @Test
    fun redeemUrlWhenNoUrlInResponse() {
        assertEquals(null, scope.webViewLauncerMock.lastSeenURL)

        sut.itinObserver.onChanged(lxNotHappy)
        assertEquals(null, scope.webViewLauncerMock.lastSeenURL)
    }

    @Test
    fun redeemUrlWhenVoucherUrlIsReturned() {
        assertEquals(null, scope.webViewLauncerMock.lastSeenURL)

        sut.itinObserver.onChanged(voucherUrlReturned)
        sut.redeemVoucherClickSubject.onNext(Unit)
        assertEquals("https://www.expedia.com/itinerary-print?tripid=72d43105-3cd2-4f83-ae10-55f390397ac0&itineraryNumber=7337689803181", scope.webViewLauncerMock.lastSeenURL)
    }

    @Test
    fun redeemUrlWhenLxVoucherUrlIsReturned() {
        assertEquals(null, scope.webViewLauncerMock.lastSeenURL)

        sut.itinObserver.onChanged(lxvoucherUrlReturned)
        sut.redeemVoucherClickSubject.onNext(Unit)
        assertEquals("https://www.expedia.com/things-to-do/voucher/?tripid=72d43105-3cd2-4f83-ae10-55f390397ac0", scope.webViewLauncerMock.lastSeenURL)
    }

    @Test
    fun tripIdNotNullRedeemUrlNull() {
        sut.itinObserver.onChanged(lxNotHappy)
        sut.showRedeemVoucher.subscribe(showRedeemObserver)
        showRedeemObserver.assertNoValues()
    }

    @Test
    fun tripIdNull() {
        sut.itinObserver.onChanged(noTripId)
        sut.showRedeemVoucher.subscribe(showRedeemObserver)
        showRedeemObserver.assertNoValues()
    }

    @Test
    fun happyPathTest() {
        val url = "https://wwwexpediacom.trunk.sb.karmalab.net/things-to-do/voucher/?tripid=b9739936-62a8-49a1-af12-fdbe85d78e5f"
        val tripId = "b9739936-62a8-49a1-af12-fdbe85d78e5f"
        sut.showRedeemVoucher.subscribe(showRedeemObserver)
        showRedeemObserver.assertNoValues()

        sut.itinObserver.onChanged(lxHappy)

        assertFalse(scope.tripsTracking.trackRedeemVoucherCalled)
        showRedeemObserver.assertValue(Unit)

        sut.redeemVoucherClickSubject.onNext(Unit)

        assertTrue(scope.tripsTracking.trackRedeemVoucherCalled)
        assertTrue(scope.webViewLauncerMock.lastSeenTitle == R.string.itin_lx_redeem_voucher)
        assertEquals(url, scope.webViewLauncerMock.lastSeenURL)
        assertEquals(tripId, scope.webViewLauncerMock.lastSeenTripId)
    }

    private class MockRedeemVoucherViewModelScope : HasLifecycleOwner, HasLxRepo, HasTripsTracking, HasStringProvider, HasWebViewLauncher {
        val mockTracking = MockTripsTracking()
        override val tripsTracking = mockTracking
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        val mockRepo = MockLxRepo(false)
        override val itinLxRepo: ItinLxRepoInterface = mockRepo
        override val strings: StringSource = MockStringProvider()
        val webViewLauncerMock = MockWebViewLauncher()
        override val webViewLauncher: IWebViewLauncher = webViewLauncerMock
    }
}
