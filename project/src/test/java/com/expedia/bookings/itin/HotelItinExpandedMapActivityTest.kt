package com.expedia.bookings.itin

import android.net.Uri
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.itin.activity.HotelItinExpandedMapActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class HotelItinExpandedMapActivityTest {

    lateinit var activity: HotelItinExpandedMapActivity
    private lateinit var itinCardDataHotel: ItinCardDataHotel

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinExpandedMapActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
    }

    @Test
    fun testDirectionsOmnitureClick() {
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

    @Test
    fun testBuildUriForHotel() {
        var lat: Double? = 37.14
        var long: Double? = 22.23
        var propertyName: String? = "Test Property"
        var result = activity.buildUriForHotel(lat, long, propertyName)
        assertEquals(Uri.parse("geo:37.14,22.23?q=Test%20Property"), result)

        propertyName = "Test & Property"
        result = activity.buildUriForHotel(lat, long, propertyName)
        assertEquals(Uri.parse("geo:37.14,22.23?q=Test%20%26%20Property"), result)

        propertyName = null
        result = activity.buildUriForHotel(lat, long, propertyName)
        assertEquals(Uri.parse("geo:37.14,22.23?q="), result)

        propertyName = ""
        result = activity.buildUriForHotel(lat, long, propertyName)
        assertEquals(Uri.parse("geo:37.14,22.23?q="), result)

        propertyName = ""
        result = activity.buildUriForHotel(lat, long, propertyName)
        assertEquals(Uri.parse("geo:37.14,22.23?q="), result)

        lat = null
        result = activity.buildUriForHotel(lat, long, propertyName)
        assertNull(result)

        long = null
        result = activity.buildUriForHotel(lat, long, propertyName)
        assertNull(result)
    }
}
