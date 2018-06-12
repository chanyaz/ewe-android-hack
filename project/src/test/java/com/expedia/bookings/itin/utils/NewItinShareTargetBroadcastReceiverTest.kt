package com.expedia.bookings.itin.utils

import android.content.Intent
import com.expedia.account.BuildConfig
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricRunner::class)
class NewItinShareTargetBroadcastReceiverTest {

    lateinit var mockAnalyticsProvider: AnalyticsProvider
    val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    @Config(constants = BuildConfig::class, sdk = [26])
    fun testShareItinFromAppClickedTracking() {
        val receiver = NewItinShareTargetBroadcastReceiver()
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_CHOSEN_COMPONENT, "component.name")
        intent.putExtra(Intent.EXTRA_KEY_EVENT, "trip")
        receiver.onReceive(context, intent)

        OmnitureTestUtils.assertLinkTracked(Matchers.allOf(
                OmnitureMatchers.withEventsString("event48"),
                OmnitureMatchers.withProps(mapOf(16 to "App.Itinerary.trip.Share.component.name")),
                OmnitureMatchers.withEvars(mapOf(2 to "trip", 28 to "App.Itinerary.trip.Share.component.name"))), mockAnalyticsProvider)
    }

    @Test
    @Config(constants = BuildConfig::class, sdk = [21])
    fun trackShareItinFromAppClickedNotTrackedForLowOS() {
        val receiver = NewItinShareTargetBroadcastReceiver()
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_CHOSEN_COMPONENT, "component.name")
        intent.putExtra(Intent.EXTRA_KEY_EVENT, "trip")
        receiver.onReceive(context, intent)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }
}
