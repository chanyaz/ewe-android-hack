package com.expedia.bookings.itin.lx.moreHelp

import android.content.Intent
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.TripsTracking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class LxItinMoreHelpActivityTest {
    private lateinit var activity: LxItinMoreHelpActivity
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val intent = Intent()
        intent.putExtra("LX_ITIN_ID", "LX_ITIN_ID")
        activity = Robolectric.buildActivity(LxItinMoreHelpActivity::class.java, intent).create().start().get()
    }

    @Test
    fun testActivityPageLoadTracked() {
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(mapOf(18 to "App.Itinerary.Error")), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withProps(mapOf(36 to "itin:unable to retrieve trip summary")), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEventsString("event63,event253"), mockAnalyticsProvider)
    }

    @Test
    fun testFinishActivity() {
        val shadow = Shadows.shadowOf(activity)
        assertFalse(shadow.isFinishing)
        activity.finish()
        assertTrue(shadow.isFinishing)
    }
}
