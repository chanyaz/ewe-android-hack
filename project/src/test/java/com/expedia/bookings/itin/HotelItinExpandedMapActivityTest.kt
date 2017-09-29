package com.expedia.bookings.itin

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.itin.activity.HotelItinExpandedMapActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric


@RunWith(RobolectricRunner::class)
class HotelItinExpandedMapActivityTest {

    lateinit var activity: HotelItinExpandedMapActivity
    lateinit private var itinCardDataHotel: ItinCardDataHotel

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinExpandedMapActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
    }

    @Test
    fun testDirectionsOmnitureClick() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppItinHotelRedesign)
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        activity.setUpWidgets(itinCardDataHotel)
        activity.directionsButton.performClick()

        OmnitureTestUtils.assertLinkTracked("Map Action", "App.Map.Directions.Drive", mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureForZoomIn() {

        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackItinExpandedMapZoomIn()
        OmnitureTestUtils.assertLinkTracked("Map Action", "App.Map.Directions.ZoomIn", mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureForZoomout() {

        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackItinExpandedMapZoomOut()
        OmnitureTestUtils.assertLinkTracked("Map Action", "App.Map.Directions.ZoomOut", mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureForPan() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackItinExpandedMapZoomPan()
        OmnitureTestUtils.assertLinkTracked("Map Action", "App.Map.Directions.Pan", mockAnalyticsProvider)
    }

}