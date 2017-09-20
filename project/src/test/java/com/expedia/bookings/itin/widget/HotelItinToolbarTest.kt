package com.expedia.bookings.itin.widget

import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.itin.activity.HotelItinManageBookingActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinToolbarTest {

    lateinit var hotelItinToolbar: ItinToolbar
    lateinit private var activity: HotelItinManageBookingActivity

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinManageBookingActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        hotelItinToolbar = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_toolbar, null) as ItinToolbar
    }

    @Test
    fun testItinToolbarOnDetailsView() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        hotelItinToolbar.setUpWidget(itinCardDataHotel, itinCardDataHotel.propertyName)
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val startDate = LocaleBasedDateFormatUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.startDate.toString().substringBefore("T")))
        val endDate = LocaleBasedDateFormatUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.endDate.toString().substringBefore("T")))
        assertEquals(itinCardDataHotel.propertyName, hotelItinToolbar.toolbarTitleTextView.text)
        assertEquals(startDate + " - " + endDate, hotelItinToolbar.toolbarSubtitleTextView.text)
        assertEquals(startDate + " to " + endDate, hotelItinToolbar.toolbarSubtitleTextView.contentDescription)
    }

    @Test
    fun testItinToolbarOnManageBookingView() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val titleString = activity.getString(R.string.itin_hotel_manage_booking_header)
        hotelItinToolbar.setUpWidget(itinCardDataHotel, titleString)
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val startDate = LocaleBasedDateFormatUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.startDate.toString().substringBefore("T")))
        val endDate = LocaleBasedDateFormatUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.endDate.toString().substringBefore("T")))
        assertEquals(activity.getString(R.string.itin_hotel_manage_booking_header), hotelItinToolbar.toolbarTitleTextView.text)
        assertEquals(startDate + " - " + endDate, hotelItinToolbar.toolbarSubtitleTextView.text)
        assertEquals(startDate + " to " + endDate, hotelItinToolbar.toolbarSubtitleTextView.contentDescription)
    }
}