package com.expedia.bookings.itin.widget

import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinImageTest {

    lateinit var hotelItinImageWidget: HotelItinImage
    lateinit private var activity: HotelItinDetailsActivity

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinDetailsActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        hotelItinImageWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_image, null) as HotelItinImage
    }

    @Test
    fun testHotelItinImage() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val hotelImageView: HotelItinImage = activity.hotelImageView
        hotelImageView.setUpWidget(itinCardDataHotel)
        assertEquals(itinCardDataHotel.propertyName, hotelImageView.hotelNameTextView.text)
    }

}