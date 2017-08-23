package com.expedia.bookings.widget.itin

import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.activity.HotelItinManageBookingActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinManageBookingActivityTest {
    lateinit private var activity: HotelItinManageBookingActivity
    lateinit private var itinCardDataHotel: ItinCardDataHotel

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinManageBookingActivity::class.java).create().get()
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
    }

    @Test
    fun testRoomDetailsExpansion() {
        SettingUtils.save(activity, R.string.preference_enable_expandable_hotel_itin_room_details, true)

        val itinCardDataHotelMock = Mockito.spy(itinCardDataHotel)
        activity.itinCardDataHotel = itinCardDataHotelMock
        Mockito.`when`(activity.itinCardDataHotel.lastHotelRoom).thenReturn(null)
        activity.setUpWidgets()
        assertEquals(View.GONE, activity.roomDetailsView.expandedRoomDetails.visibility)
        assertEquals(View.GONE, activity.roomDetailsView.roomDetailsChevron.visibility)
        assertEquals(false, activity.roomDetailsView.isRowClickable)

        activity.itinCardDataHotel = itinCardDataHotel
        activity.setUpWidgets()
        assertEquals(View.VISIBLE, activity.roomDetailsView.expandedRoomDetails.visibility)
        assertEquals(View.GONE, activity.roomDetailsView.roomDetailsChevron.visibility)
        assertEquals(false, activity.roomDetailsView.isRowClickable)
    }
}