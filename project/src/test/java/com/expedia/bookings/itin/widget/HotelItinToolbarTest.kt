package com.expedia.bookings.itin.widget

import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity
import com.expedia.bookings.itin.activity.HotelItinExpandedMapActivity
import com.expedia.bookings.itin.activity.HotelItinManageBookingActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinToolbarTest {

    lateinit var hotelItinToolbar: HotelItinToolbar
    lateinit private var manageBookingActivity: HotelItinManageBookingActivity
    lateinit private var detailsActivity: HotelItinDetailsActivity
    lateinit private var expandedMapActivity: HotelItinExpandedMapActivity

    @Test
    fun testItinToolbarOnDetailsView() {
        detailsActivity = Robolectric.buildActivity(HotelItinDetailsActivity::class.java).create().get()
        detailsActivity.setTheme(R.style.ItinTheme)
        hotelItinToolbar = LayoutInflater.from(detailsActivity).inflate(R.layout.test_hotel_itin_toolbar, null) as HotelItinToolbar

        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        hotelItinToolbar.setUpWidget(itinCardDataHotel, itinCardDataHotel.propertyName, null)
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val startDate = LocaleBasedDateFormatUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.startDate.toString().substringBefore("T")))
        val endDate = LocaleBasedDateFormatUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.endDate.toString().substringBefore("T")))

        assertEquals(itinCardDataHotel.propertyName, hotelItinToolbar.toolbarTitleTextView.text)
        assertEquals(startDate + " - " + endDate, hotelItinToolbar.toolbarSubtitleTextView.text)
        assertEquals(startDate + " to " + endDate, hotelItinToolbar.toolbarSubtitleTextView.contentDescription)
    }

    @Test
    fun testItinToolbarOnManageBookingView() {
        manageBookingActivity = Robolectric.buildActivity(HotelItinManageBookingActivity::class.java).create().get()
        manageBookingActivity.setTheme(R.style.ItinTheme)
        hotelItinToolbar = LayoutInflater.from(manageBookingActivity).inflate(R.layout.test_hotel_itin_toolbar, null) as HotelItinToolbar
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()

        val titleString = manageBookingActivity.getString(R.string.itin_hotel_manage_booking_header)
        hotelItinToolbar.setUpWidget(itinCardDataHotel, titleString, null)
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val startDate = LocaleBasedDateFormatUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.startDate.toString().substringBefore("T")))
        val endDate = LocaleBasedDateFormatUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.endDate.toString().substringBefore("T")))

        assertEquals(manageBookingActivity.getString(R.string.itin_hotel_manage_booking_header), hotelItinToolbar.toolbarTitleTextView.text)
        assertEquals(startDate + " - " + endDate, hotelItinToolbar.toolbarSubtitleTextView.text)
        assertEquals(startDate + " to " + endDate, hotelItinToolbar.toolbarSubtitleTextView.contentDescription)
    }

    @Test
    fun testItinToolbarOnExpandedMapView() {
        expandedMapActivity = Robolectric.buildActivity(HotelItinExpandedMapActivity::class.java).create().get()
        expandedMapActivity.setTheme(R.style.ItinTheme)
        hotelItinToolbar = LayoutInflater.from(expandedMapActivity).inflate(R.layout.test_hotel_itin_toolbar, null) as HotelItinToolbar

        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val hotelCityStateCountry = itinCardDataHotel.propertyLocation.toCityStateCountryAddressFormattedString()
        hotelItinToolbar.setUpWidget(itinCardDataHotel, itinCardDataHotel.propertyName, hotelCityStateCountry)

        assertEquals(itinCardDataHotel.propertyName, hotelItinToolbar.toolbarTitleTextView.text)
        assertEquals(hotelCityStateCountry, hotelItinToolbar.toolbarSubtitleTextView.text)
    }

}