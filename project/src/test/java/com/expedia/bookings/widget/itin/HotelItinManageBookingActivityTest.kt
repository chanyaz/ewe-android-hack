package com.expedia.bookings.widget.itin

import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.activity.HotelItinManageBookingActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinManageBookingActivityTest {
    lateinit private var activity: HotelItinManageBookingActivity
    lateinit private var itinCardDataHotel: ItinCardDataHotel
    lateinit private var manageBookingActivity: HotelItinManageBookingActivity

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinManageBookingActivity::class.java)
                .create().get()
        manageBookingActivity = Robolectric.buildActivity(HotelItinManageBookingActivity::class.java).create().get()
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
    }

    @Test
    fun testItinToolbarWidget() {
        val hotelItinToolbar: HotelItinToolbar = activity.toolbar
        val titleString = activity.getString(R.string.itin_hotel_manage_booking_header)
        hotelItinToolbar.setUpWidget(itinCardDataHotel, titleString)
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val startDate = DateUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.startDate.toString().substringBefore("T")))
        val endDate = DateUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.endDate.toString().substringBefore("T")))
        assertEquals(hotelItinToolbar.toolbarTitleTextView.text, activity.getString(R.string.itin_hotel_manage_booking_header))
        assertEquals(hotelItinToolbar.toolbarSubtitleTextView.text, startDate + " - " + endDate)
    }

    @Test
    fun testRoomDetailsView() {
        val roomDetailsView: HotelItinRoomDetails = activity.roomDetailsView
        roomDetailsView.setUpWidget(itinCardDataHotel)
        assertEquals(roomDetailsView.roomDetailsText.text, itinCardDataHotel.property.itinRoomType + ", " + itinCardDataHotel.property.itinBedType)
    }

    @Test
    fun testItinHotelManageBookingHelpWidget() {
        val helpWidget: HotelItinManageBookingHelp = manageBookingActivity.hotelManageBookingHelpView
        helpWidget.setUpWidget(itinCardDataHotel)

        assertEquals(Phrase.from(activity, R.string.itin_hotel_manage_booking_hotel_help_text_TEMPLATE)
                .put("hotelname", itinCardDataHotel.propertyName).format().toString(), helpWidget.helpText.text)
        assertEquals(false, itinCardDataHotel.hasConfirmationNumber())
        assertEquals(View.GONE, helpWidget.hotelConfirmationNumber.visibility)
        assertEquals(itinCardDataHotel.localPhone, helpWidget.callHotelButton.text)
    }
}