package com.expedia.bookings.itin.widget

import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.itin.activity.HotelItinManageBookingActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelItinManageBookingHelpTest {

    lateinit var manageBookingHelpWidget: HotelItinManageBookingHelp
    private lateinit var activity: HotelItinManageBookingActivity

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinManageBookingActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        manageBookingHelpWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_manage_booking_help, null) as HotelItinManageBookingHelp
    }

    @Test
    fun testItinHotelManageBookingHelpWidget() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        manageBookingHelpWidget.setUpWidget(itinCardDataHotel)

        assertEquals(Phrase.from(activity, R.string.itin_hotel_manage_booking_hotel_help_text_TEMPLATE)
                .put("hotelname", itinCardDataHotel.propertyName).format().toString(), manageBookingHelpWidget.helpText.text)
        assertEquals(itinCardDataHotel.localPhone, manageBookingHelpWidget.callHotelButton.text)
        assertEquals("Call hotel at " + itinCardDataHotel.localPhone + ". Button", manageBookingHelpWidget.callHotelButton.contentDescription)

        manageBookingHelpWidget.callHotelButton.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.Call.Hotel", mockAnalyticsProvider)
    }

    @Test
    fun testShowConfirmationNumberIfAvailable() {
        var confirmationNumber = ""
        manageBookingHelpWidget.showConfirmationNumberIfAvailable(confirmationNumber)
        assertEquals(View.GONE, manageBookingHelpWidget.hotelConfirmationNumber.visibility)

        confirmationNumber = "12345"
        manageBookingHelpWidget.showConfirmationNumberIfAvailable(confirmationNumber)
        assertEquals("Confirmation # 12345", manageBookingHelpWidget.hotelConfirmationNumber.text)
        assertEquals("Confirmation number 1 2 3 4 5 . Click to copy", manageBookingHelpWidget.hotelConfirmationNumber.contentDescription)

        manageBookingHelpWidget.hotelConfirmationNumber.performClick()
        assertTrue(ClipboardUtils.hasText(activity))
        assertEquals("12345", ClipboardUtils.getText(activity))
    }
}
