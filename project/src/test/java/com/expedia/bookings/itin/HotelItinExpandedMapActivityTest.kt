package com.expedia.bookings.itin

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.itin.activity.HotelItinExpandedMapActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.mobiata.android.util.SettingUtils
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
        SettingUtils.save(activity, R.string.preference_trips_hotel_maps, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppItinHotelRedesign)
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        activity.setUpWidgets(itinCardDataHotel)
        activity.directionsButton.performClick()

        OmnitureTestUtils.assertLinkTracked("Map Action", "App.Map.Directions.Drive", mockAnalyticsProvider)
    }

}