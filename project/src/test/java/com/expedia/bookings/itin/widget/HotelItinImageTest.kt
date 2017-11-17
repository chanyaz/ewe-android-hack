package com.expedia.bookings.itin.widget

import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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
        val prevDrawable = hotelImageView.hotelImageView.drawable
        hotelImageView.setUpWidget(itinCardDataHotel)
        assertEquals(itinCardDataHotel.propertyName, hotelImageView.hotelNameTextView.text)
        assertNotEquals(prevDrawable, hotelImageView.hotelImageView.drawable)
    }

    @Test
    fun testHotelItinImageNull() {
        var itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val mock = Mockito.mock(HotelMedia::class.java)
        itinCardDataHotel.property.thumbnail = mock
        Mockito.`when`(mock.originalUrl).thenReturn(null)
        val hotelImageView: HotelItinImage = activity.hotelImageView
        val prevDrawable = hotelImageView.hotelImageView.drawable
        hotelImageView.setUpWidget(itinCardDataHotel)
        assertEquals(prevDrawable, hotelImageView.hotelImageView.drawable)
    }

    @Test
    fun testHotelItinImageBlank() {
        var itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val mock = Mockito.mock(HotelMedia::class.java)
        itinCardDataHotel.property.thumbnail = mock
        Mockito.`when`(mock.originalUrl).thenReturn(" ")
        val hotelImageView: HotelItinImage = activity.hotelImageView
        val prevDrawable = hotelImageView.hotelImageView.drawable
        hotelImageView.setUpWidget(itinCardDataHotel)
        assertEquals(prevDrawable, hotelImageView.hotelImageView.drawable)
    }
}