package com.expedia.bookings.itin.widget

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinCheckInCheckOutDetailsTest {

    lateinit var hotelItinCheckinCheckOutWidget: HotelItinCheckInCheckOutDetails
    private lateinit var activity: HotelItinDetailsActivity

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

        val checkInDate = LocaleBasedDateFormatUtils.dateTimeToEEEMMMd(itinCardDataHotel.startDate)
        val checkOutDate = LocaleBasedDateFormatUtils.dateTimeToEEEMMMd(itinCardDataHotel.endDate)
        val checkInContDesc = LocaleBasedDateFormatUtils.dateTimeToEEEEMMMd(itinCardDataHotel.startDate)
        val checkOutContDesc = LocaleBasedDateFormatUtils.dateTimeToEEEEMMMd(itinCardDataHotel.endDate)

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
    
    @Test
    fun testCheckInCheckOutViewText() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)
        assertEquals(hotelItinCheckinCheckOutWidget.checkInTimeView.text.toString(), itinCardDataHotel.checkInTime.toLowerCase())
        assertEquals(hotelItinCheckinCheckOutWidget.checkOutTimeView.text.toString(), itinCardDataHotel.checkOutTime.toLowerCase())

        (itinCardDataHotel.tripComponent as TripHotel).checkInTime = null
        (itinCardDataHotel.tripComponent as TripHotel).checkOutTime = null
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)
        assertEquals(hotelItinCheckinCheckOutWidget.checkInTimeView.text.toString(), JodaUtils.formatDateTime(activity, itinCardDataHotel.startDate, DateUtils.FORMAT_SHOW_TIME).toLowerCase())
        assertEquals(hotelItinCheckinCheckOutWidget.checkOutTimeView.text.toString(), JodaUtils.formatDateTime(activity, itinCardDataHotel.endDate, DateUtils.FORMAT_SHOW_TIME).toLowerCase())
    }
}