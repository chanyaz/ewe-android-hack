package com.expedia.bookings.tracking

import android.content.Context
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class OmnitureTrackingLXTest {

    private lateinit var context: Context
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testTrackBookingConfirmationDialog() {
        OmnitureTracking.trackLXBookingConfirmationDialog("activityID", LocalDate(), 2)
        val expectedEvars = mapOf(18 to "App.LX.Checkout.Confirmation.Slim",
                50 to "app.phone.android")
        val expectedProducts = "LX;Merchant LX:activityID;2;null"
        val appState = "App.Checkout.Confirmation.Slim"
        OmnitureTestUtils.assertStateTracked(appState, OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked(appState, OmnitureMatchers.withProductsString(expectedProducts, shouldExactlyMatch = false), mockAnalyticsProvider)
    }
}
