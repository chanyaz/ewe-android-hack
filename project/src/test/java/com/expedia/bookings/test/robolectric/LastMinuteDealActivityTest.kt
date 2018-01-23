package com.expedia.bookings.test.robolectric

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.mia.activity.LastMinuteDealActivity
import com.expedia.bookings.test.OmnitureMatchers
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(RobolectricRunner::class)
class LastMinuteDealActivityTest {

    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setUp() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun startingLastMinuteDealActivityIsTrackedInOmniture() {
        val activity = Robolectric.buildActivity(LastMinuteDealActivity::class.java).create().start().postCreate(null).resume().get()
        OmnitureTestUtils.assertStateTracked("App.LastMinuteDeals", Matchers.allOf(
                OmnitureMatchers.withProps(mapOf(2 to "Merch")),
                OmnitureMatchers.withEvars(mapOf(12 to "App.LastMinuteDeals"))), mockAnalyticsProvider)
        activity.finish()
    }
}
