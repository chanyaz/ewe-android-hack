package com.expedia.bookings.widget.itin

import android.text.format.DateFormat
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.Locale
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinCheckInCheckOutDetailsTest {

    lateinit var hotelItinCheckinCheckOutWidget: HotelItinCheckInCheckOutDetails
    lateinit private var activity: HotelItinDetailsActivity

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinDetailsActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        hotelItinCheckinCheckOutWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_checkin_checkout_details, null) as HotelItinCheckInCheckOutDetails
    }

    @Test
    fun testItinHotelCheckInCheckoutDate() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)

        val formatPattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEE, MMM d")
        val checkInDate = itinCardDataHotel.startDate.toString(formatPattern)
        val checkOutDate = itinCardDataHotel.endDate.toString(formatPattern)

        assertEquals(checkInDate, hotelItinCheckinCheckOutWidget.checkInDateView.text)
        assertEquals(checkOutDate, hotelItinCheckinCheckOutWidget.checkOutDateView.text)
        assertEquals(itinCardDataHotel.checkInTime?.toLowerCase(), hotelItinCheckinCheckOutWidget.checkInTimeView.text)
        assertEquals(itinCardDataHotel.checkOutTime?.toLowerCase(), hotelItinCheckinCheckOutWidget.checkOutTimeView.text)
    }

}