package com.expedia.bookings.tracking.hotel

import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class HotelTrackingTest {

    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testTrackInfositeChangeDate() {
        HotelTracking.trackInfositeChangeDateClick()

        var tpid = PointOfSale.getPointOfSale().tpid.toString()
        if (PointOfSale.getPointOfSale().eapid != PointOfSale.INVALID_EAPID) {
            tpid += "-" + PointOfSale.getPointOfSale().eapid
        }
        val evar = mapOf(61 to tpid)
        val prop = mapOf(7 to tpid)

        OmnitureTestUtils.assertLinkTracked("Infosite Change Dates", "App.Hotels.IS.ChangeDates",
                Matchers.allOf(OmnitureMatchers.withEvars(evar), OmnitureMatchers.withProps(prop)),
                mockAnalyticsProvider)
    }
}
