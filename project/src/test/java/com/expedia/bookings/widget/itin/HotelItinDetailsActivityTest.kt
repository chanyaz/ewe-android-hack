package com.expedia.bookings.widget.itin

import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import com.expedia.bookings.utils.ClipboardUtils
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinDetailsActivityTest {
    lateinit private var activity: HotelItinDetailsActivity
    lateinit private var itinCardDataHotel: ItinCardDataHotel

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinDetailsActivity::class.java).create().get()
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
    }

    @Test
    fun testRoomDetailsWidget() {
        val roomDetailsView: HotelItinRoomDetails = activity.roomDetailsView
        roomDetailsView.setUpWidget(itinCardDataHotel)
        assertEquals(roomDetailsView.roomDetailsText.text, itinCardDataHotel.property.itinRoomType + ", " + itinCardDataHotel.property.itinBedType)
    }

    @Test
    fun testHotelItinImage() {
        val hotelImageView: HotelItinImage = activity.hotelImageView
        hotelImageView.setUpWidget(itinCardDataHotel)
        assertEquals(hotelImageView.hotelNameTextView.text, itinCardDataHotel.propertyName)
    }

    fun testMapWidget() {
        val locationDetailsView: HotelItinLocationDetails = activity.locationDetailsView
        locationDetailsView.setupWidget(itinCardDataHotel)
        assertEquals(View.VISIBLE, locationDetailsView.locationMapImageView.visibility)
        assertEquals(locationDetailsView.addressLine1.text, itinCardDataHotel.propertyLocation.streetAddressString)
        assertEquals(locationDetailsView.addressLine2.text, itinCardDataHotel.propertyLocation.toTwoLineAddressFormattedString())
        assertEquals(locationDetailsView.actionButtons.getmLeftButton().text, itinCardDataHotel.localPhone)
        locationDetailsView.address.performClick()
        val address: String = Phrase.from(activity, R.string.itin_hotel_details_address_clipboard_TEMPLATE)
                .put("addresslineone", locationDetailsView.addressLine1.text.toString())
                .put("addresslinetwo", locationDetailsView.addressLine2.text.toString()).format().toString()
        assertEquals(ClipboardUtils.getText(activity), address)
    }

    @Test
    fun testItinToolbarWidget() {
        val hotelItinToolbar: HotelItinToolbar = activity.toolbar
        hotelItinToolbar.setUpWidget(itinCardDataHotel)
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val startDate = DateUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel?.startDate.toString().substringBefore("T")))
        val endDate = DateUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel?.endDate.toString().substringBefore("T")))
        assertEquals(hotelItinToolbar.hotelNameTextView.text, itinCardDataHotel.propertyName)
        assertEquals(hotelItinToolbar.hotelTripDatesTextView.text, startDate + " - " + endDate)
    }
}