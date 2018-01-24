package com.expedia.bookings.itin

import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.activity.HotelItinManageBookingActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.itin.widget.HotelItinRoomDetails
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinManageBookingActivityTest {
    private lateinit var activityController: ActivityController<HotelItinManageBookingActivity>
    private lateinit var activity: HotelItinManageBookingActivity
    private lateinit var itinCardDataHotel: ItinCardDataHotel
    lateinit var roomDetailsWidget: HotelItinRoomDetails

    @Before
    fun before() {
        activityController = Robolectric.buildActivity(HotelItinManageBookingActivity::class.java).create()
        activity = activityController.get()
        activity.setTheme(R.style.ItinTheme)
        roomDetailsWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_room_details, null) as HotelItinRoomDetails
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
    }

    @Test
    fun testRoomDetailsExpansionNullCase() {
        val itinCardDataHotelMock = Mockito.spy(itinCardDataHotel)
        activity.itinCardDataHotel = itinCardDataHotelMock
        Mockito.`when`(activity.itinCardDataHotel.getHotelRoom(0)).thenReturn(null)
        Mockito.`when`(activity.itinCardDataHotel.changeAndCancelRules).thenReturn(null)

        activity.manageRoomViewModel.refreshItinCardDataSubject.onNext(activity.itinCardDataHotel)

        assertEquals(1, activity.manageRoomContainer.childCount)
        assertEquals(activity.manageRoomViewModel.manageRoomWidget, activity.manageRoomContainer.getChildAt(0))
        val roomDetailsView = activity.manageRoomViewModel.manageRoomWidget.roomDetailsView
        assertEquals(View.GONE, roomDetailsView.expandedRoomDetails.visibility)
        assertEquals(View.GONE, roomDetailsView.roomDetailsChevron.visibility)
        assertEquals(false, roomDetailsView.isRowClickable)
        assertEquals(View.GONE, roomDetailsView.changeCancelRulesContainer.visibility)
    }

    @Test
    fun testRoomDetailsExpansionNonNullCase() {
        activity.itinCardDataHotel = itinCardDataHotel

        activity.manageRoomViewModel.refreshItinCardDataSubject.onNext(activity.itinCardDataHotel)

        val roomDetailsView = activity.manageRoomViewModel.manageRoomWidget.roomDetailsView
        assertEquals(View.VISIBLE, roomDetailsView.expandedRoomDetails.visibility)
        assertEquals(View.GONE, roomDetailsView.roomDetailsChevron.visibility)
        assertEquals(false, roomDetailsView.isRowClickable)
        assertEquals(View.VISIBLE, roomDetailsView.changeCancelRulesContainer.visibility)
    }

    @Test
    fun testManageRoomContainerSubject() {
        assertEquals(1, activity.manageRoomContainer.childCount)
        assertEquals(activity.manageRoomViewModel.manageRoomWidget, activity.manageRoomContainer.getChildAt(0))

        activityController.pause()
        assertEquals(1, activity.manageRoomContainer.childCount)
        assertEquals(activity.manageRoomViewModel.manageRoomWidget, activity.manageRoomContainer.getChildAt(0))

        activityController.resume()
        assertEquals(1, activity.manageRoomContainer.childCount)
        assertEquals(activity.manageRoomViewModel.manageRoomWidget, activity.manageRoomContainer.getChildAt(0))
    }
}
