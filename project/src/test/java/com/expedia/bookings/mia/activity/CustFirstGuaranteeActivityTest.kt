package com.expedia.bookings.mia.activity

import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = [MultiBrand.TRAVELOCITY])
class CustFirstGuaranteeActivityTest {

    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private lateinit var activity: CustomerFirstActivity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(CustomerFirstActivity::class.java).create().start().get()
        activity.setTheme(R.style.ItinTheme)
        activity.setContentView(R.layout.customer_first_support_activity)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun startingCustomerFirstActivityIsTrackedInOmniture() {
        val activity = Robolectric.buildActivity(CustomerFirstActivity::class.java).create().start().postCreate(null).resume().get()
        OmnitureTestUtils.assertStateTracked("App.Support.CFG",
                OmnitureMatchers.withEvars(mapOf(18 to "D=App.Support.CFG")), mockAnalyticsProvider)
        activity.finish()
    }
}
