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
    val lxHappy = ItinMocker.lxDetailsAlsoHappy
    val lxNotHappy = ItinMocker.lxDetailsNoDetailsUrl
    val voucherUrlReturned = ItinMocker.lxDetailsNoTripID
    val lxvoucherUrlReturned = ItinMocker.lxDetailsNoLat

    @Before
    fun setup() {
        scope = MockRedeemVoucherViewModelScope()
        sut = LxItinRedeemVoucherViewModel(scope)
    }

    @Test
    fun redeemUrlWhenBothUrlInResponse() {
        assertEquals(null, sut.redeemUrl)

        sut.itinObserver.onChanged(lxHappy)
        assertEquals("https://www.expedia.com/things-to-do/voucher/?tripid=72d43105-3cd2-4f83-ae10-55f390397ac0", sut.redeemUrl)
    }

    @Test
    fun redeemUrlWhenNoUrlInResponse() {
        assertEquals(null, sut.redeemUrl)

        sut.itinObserver.onChanged(lxNotHappy)
        assertEquals(null, sut.redeemUrl)
    }

    @Test
    fun redeemUrlWhenVoucherUrlIsReturned() {
        assertEquals(null, sut.redeemUrl)

        sut.itinObserver.onChanged(voucherUrlReturned)
        assertEquals("https://www.expedia.com/itinerary-print?tripid=72d43105-3cd2-4f83-ae10-55f390397ac0&itineraryNumber=7337689803181", sut.redeemUrl)
    }

    @Test
    fun redeemUrlWhenLxVoucherUrlIsReturned() {
        assertEquals(null, sut.redeemUrl)

        sut.itinObserver.onChanged(lxvoucherUrlReturned)
        assertEquals("https://www.expedia.com/things-to-do/voucher/?tripid=72d43105-3cd2-4f83-ae10-55f390397ac0", sut.redeemUrl)
    }

    @Test
    fun tripIdNotNullRedeemUrlNull() {
        sut.itinObserver.onChanged(lxNotHappy)
        sut.showRedeemVoucher.subscribe(showRedeemObserver)
        showRedeemObserver.assertNoValues()
    }

    @Test
    fun tripIdNull() {
        sut.itinObserver.onChanged(voucherUrlReturned)
        sut.showRedeemVoucher.subscribe(showRedeemObserver)
        showRedeemObserver.assertNoValues()
    }

    @Test
    fun happyPathTest() {
        val url = "https://www.expedia.com/things-to-do/voucher/?tripid=72d43105-3cd2-4f83-ae10-55f390397ac0"
        val tripId = "72d43105-3cd2-4f83-ae10-55f390397ac0"
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
