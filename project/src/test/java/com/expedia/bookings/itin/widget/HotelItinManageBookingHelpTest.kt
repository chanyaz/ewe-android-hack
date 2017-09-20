package com.expedia.bookings.itin.widget

import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.activity.HotelItinManageBookingActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinManageBookingHelpTest {

    lateinit var manageBookingHelpWidget: HotelItinManageBookingHelp
    lateinit private var activity: HotelItinManageBookingActivity

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinManageBookingActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        manageBookingHelpWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_manage_booking_help, null) as HotelItinManageBookingHelp
    }

    @Test
    fun testItinHotelManageBookingHelpWidget() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        manageBookingHelpWidget.setUpWidget(itinCardDataHotel)

        assertEquals(Phrase.from(activity, R.string.itin_hotel_manage_booking_hotel_help_text_TEMPLATE)
                .put("hotelname", itinCardDataHotel.propertyName).format().toString(), manageBookingHelpWidget.helpText.text)
        assertEquals(false, itinCardDataHotel.hasConfirmationNumber())
        assertEquals(View.GONE, manageBookingHelpWidget.hotelConfirmationNumber.visibility)
        assertEquals(itinCardDataHotel.localPhone, manageBookingHelpWidget.callHotelButton.text)
    }

}