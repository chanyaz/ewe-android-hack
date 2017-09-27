package com.expedia.bookings.itin.widget

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowAlertDialog
import java.util.Locale
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinCheckInCheckOutDetailsTest {

    lateinit var hotelItinCheckinCheckOutWidget: HotelItinCheckInCheckOutDetails
    lateinit private var activity: HotelItinDetailsActivity

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinDetailsActivity::class.java).create().start().get()
        activity.setTheme(R.style.ItinTheme)
        hotelItinCheckinCheckOutWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_checkin_checkout_details, null) as HotelItinCheckInCheckOutDetails
    }

    @Test
    fun testItinHotelCheckInCheckoutDate() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)

        val formatPattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEE, MMM d")
        val contDescFormatPattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEE, MMM d")
        val checkInDate = itinCardDataHotel.startDate.toString(formatPattern)
        val checkOutDate = itinCardDataHotel.endDate.toString(formatPattern)
        val checkInContDesc = itinCardDataHotel.startDate.toString(contDescFormatPattern)
        val checkOutContDesc = itinCardDataHotel.endDate.toString(contDescFormatPattern)

        assertEquals(checkInDate, hotelItinCheckinCheckOutWidget.checkInDateView.text)
        assertEquals(checkInContDesc, hotelItinCheckinCheckOutWidget.checkInDateView.contentDescription)
        assertEquals(checkOutDate, hotelItinCheckinCheckOutWidget.checkOutDateView.text)
        assertEquals(checkOutContDesc, hotelItinCheckinCheckOutWidget.checkOutDateView.contentDescription)
        assertEquals(itinCardDataHotel.checkInTime?.toLowerCase(), hotelItinCheckinCheckOutWidget.checkInTimeView.text)
        assertEquals(itinCardDataHotel.checkOutTime?.toLowerCase(), hotelItinCheckinCheckOutWidget.checkOutTimeView.text)
    }

    @Test
    fun testItinHotelCheckInPolicies() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)

        assertEquals("Check-in policies", hotelItinCheckinCheckOutWidget.checkInOutPoliciesButtonText.text.toString())
        hotelItinCheckinCheckOutWidget.checkInOutPoliciesContainer.performClick()
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val checkInPolicesText = alertDialog.findViewById<View>(R.id.fragment_dialog_scrollable_text_content) as TextView
        assertEquals(true, alertDialog.isShowing)
        assertEquals("Minimum check-in age is 18\nCheck-in time starts at 3 PM", checkInPolicesText.text.toString())
    }
}