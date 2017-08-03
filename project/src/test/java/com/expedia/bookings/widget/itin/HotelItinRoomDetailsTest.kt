package com.expedia.bookings.widget.itin

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinRoomDetailsTest {

    lateinit var roomDetailsWidget: HotelItinRoomDetails

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        roomDetailsWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_room_details, null) as HotelItinRoomDetails
    }

    @Test
    fun roomDetailsAreCorrect() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        roomDetailsWidget.setUpWidget(itinCardDataHotel)

        val expected = itinCardDataHotel.property.itinRoomType + ", " + itinCardDataHotel.property.itinBedType
        assertEquals(expected, roomDetailsWidget.roomDetailsText.text)
    }

    @Test
    fun roomDetailsReservedFor() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        roomDetailsWidget.setUpWidget(itinCardDataHotel)

        val expectedString = "Kevin Carpenter, 1 adult "
        assertEquals(expectedString, roomDetailsWidget.guestName.text)
    }

    @Test
    fun roomDetailsCollapseExpand() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        roomDetailsWidget.setUpWidget(itinCardDataHotel)
        roomDetailsWidget.collapseRoomDetailsView()
        assertEquals(View.GONE, roomDetailsWidget.expandedRoomDetails.visibility)

        roomDetailsWidget.expandRoomDetailsView()
        assertEquals(View.VISIBLE, roomDetailsWidget.expandedRoomDetails.visibility)
    }

    @Test
    fun roomRequestsAreCorrect() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        roomDetailsWidget.setUpWidget(itinCardDataHotel)
        val expectedString = "NONSMOKING_ROOM\n" +
                "1 king bed\n" +
                "Accessible bathroom, Roll-in shower, In-room accessibility\n" +
                "\"Please bring New York Times to the room\"\n" +
                "Extra adult bed"
        assertEquals(expectedString, roomDetailsWidget.roomRequestsText.text)
    }
}