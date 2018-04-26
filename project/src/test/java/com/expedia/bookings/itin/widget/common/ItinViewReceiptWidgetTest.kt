package com.expedia.bookings.itin.widget.common

import android.arch.lifecycle.LifecycleOwner
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.itin.common.ItinViewReceiptWidget
import com.expedia.bookings.itin.helpers.MockHotelRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.hotel.pricingRewards.HotelItinViewReceiptViewModel
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class ItinViewReceiptWidgetTest {

    lateinit var activity: AppCompatActivity
    lateinit var viewReceiptWidget: ItinViewReceiptWidget
    lateinit var mockAnalyticsProvider: AnalyticsProvider
    lateinit var scope: MockHotelItinViewReceiptScope

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        viewReceiptWidget = LayoutInflater.from(activity).inflate(R.layout.test_view_receipt_widget, null) as ItinViewReceiptWidget
        scope = MockHotelItinViewReceiptScope()
        viewReceiptWidget.viewModel = HotelItinViewReceiptViewModel(scope)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testViewReceiptVisibility() {
        assertEquals(View.GONE, viewReceiptWidget.viewReceiptButton.visibility)
        assertFalse(scope.tripsTracking.trackItinHotelViewReceiptCalled)

        viewReceiptWidget.viewModel.showReceipt.onNext(Unit)

        assertEquals(View.VISIBLE, viewReceiptWidget.viewReceiptButton.visibility)
        assertEquals("View receipt Button", viewReceiptWidget.viewReceiptButton.contentDescription)
    }

    class MockHotelItinViewReceiptScope() : HasHotelRepo, HasStringProvider, HasLifecycleOwner, HasTripsTracking, HasWebViewLauncher {
        val mockStrings = MockStringProvider()
        override val strings: StringSource = mockStrings
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        val viewReceiptTracking = MockTripsTracking()
        override val tripsTracking = viewReceiptTracking
        override val itinHotelRepo = MockHotelRepo()
        val webLauncherMock = MockWebViewLauncher()
        override val webViewLauncher: IWebViewLauncher = webLauncherMock
    }
}
