package com.expedia.bookings.itin.lx

import android.arch.lifecycle.LifecycleOwner
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.lx.details.LxItinRedeemVoucherViewModel
import com.expedia.bookings.itin.lx.details.LxItinRedeemVoucherWidget
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class LxItinRedeemVoucherWidgetTest {

    lateinit var activity: AppCompatActivity
    lateinit var redeemVoucherWidget: LxItinRedeemVoucherWidget
    lateinit var mockAnalyticsProvider: AnalyticsProvider
    lateinit var scope: MockLxItinRedeemVoucherScope
    val redeemVoucherClickObserver = TestObserver<Unit>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        redeemVoucherWidget = LayoutInflater.from(activity).inflate(R.layout.test_lx_itin_redeem_voucher, null) as LxItinRedeemVoucherWidget
        scope = LxItinRedeemVoucherWidgetTest.MockLxItinRedeemVoucherScope()
        redeemVoucherWidget.viewModel = LxItinRedeemVoucherViewModel(scope)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testRedeemVoucherButtonVisibility() {
        assertEquals(View.GONE, redeemVoucherWidget.redeemVoucherButton.visibility)
        assertFalse(scope.tripsTracking.trackRedeemVoucherCalled)

        redeemVoucherWidget.viewModel.showRedeemVoucher.onNext(Unit)

        assertEquals(View.VISIBLE, redeemVoucherWidget.redeemVoucherButton.visibility)
        assertEquals("Redeem voucher Button", redeemVoucherWidget.redeemVoucherButton.contentDescription)
    }

    @Test
    fun testRedeemVoucherButtonClick() {
        redeemVoucherWidget.viewModel.redeemVoucherClickSubject.subscribe(redeemVoucherClickObserver)
        redeemVoucherClickObserver.assertNoValues()

        redeemVoucherWidget.viewModel.redeemVoucherClickSubject.onNext(Unit)

        redeemVoucherClickObserver.assertValue(Unit)
    }

    class MockLxItinRedeemVoucherScope() : HasLxRepo, HasStringProvider, HasLifecycleOwner, HasTripsTracking, HasWebViewLauncher {
        val mockStrings = MockStringProvider()
        override val strings: StringSource = mockStrings
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        val redeemVoucherTracking = MockTripsTracking()
        override val tripsTracking = redeemVoucherTracking
        override val itinLxRepo = MockLxRepo()
        val webLauncherMock = MockWebViewLauncher()
        override val webViewLauncher: IWebViewLauncher = webLauncherMock
    }
}
