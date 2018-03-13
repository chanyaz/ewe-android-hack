package com.expedia.bookings.itin

import android.support.design.widget.TabLayout
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripHotelRoom
import com.expedia.bookings.itin.hotel.manageBooking.HotelItinManageBookingActivity
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.itin.hotel.details.HotelItinRoomDetails
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

        assertEquals(2, activity.manageRoomContainer.childCount)
        assertEquals(activity.numberOfRoomsText, activity.manageRoomContainer.getChildAt(0))
        assertEquals(View.GONE, activity.numberOfRoomsText.visibility)
        assertEquals(activity.manageRoomViewModel.manageRoomWidget, activity.manageRoomContainer.getChildAt(1))
        val roomDetailsView = activity.manageRoomViewModel.manageRoomWidget.roomDetailsView
        assertEquals(View.GONE, roomDetailsView.expandedRoomDetails.visibility)
        assertEquals(View.GONE, roomDetailsView.roomDetailsChevron.visibility)
        assertEquals(View.GONE, roomDetailsView.changeCancelRulesContainer.visibility)
        assertEquals(View.GONE, roomDetailsView.amenitiesContainer.visibility)
    }

    @Test
    fun testRoomDetailsExpansionNonNullCase() {
        activity.itinCardDataHotel = itinCardDataHotel

        activity.manageRoomViewModel.refreshItinCardDataSubject.onNext(activity.itinCardDataHotel)

        val roomDetailsView = activity.manageRoomViewModel.manageRoomWidget.roomDetailsView
        assertEquals(View.VISIBLE, roomDetailsView.expandedRoomDetails.visibility)
        assertEquals(View.GONE, roomDetailsView.roomDetailsChevron.visibility)
        assertEquals(View.VISIBLE, roomDetailsView.changeCancelRulesContainer.visibility)
        assertEquals(View.GONE, roomDetailsView.amenitiesContainer.visibility)
    }

    @Test
    fun testManageRoomContainerSubject() {
        assertEquals(2, activity.manageRoomContainer.childCount)
        assertEquals(activity.numberOfRoomsText, activity.manageRoomContainer.getChildAt(0))
        assertEquals(View.GONE, activity.numberOfRoomsText.visibility)
        assertEquals(activity.manageRoomViewModel.manageRoomWidget, activity.manageRoomContainer.getChildAt(1))

        activityController.pause()
        assertEquals(2, activity.manageRoomContainer.childCount)
        assertEquals(activity.numberOfRoomsText, activity.manageRoomContainer.getChildAt(0))
        assertEquals(View.GONE, activity.numberOfRoomsText.visibility)
        assertEquals(activity.manageRoomViewModel.manageRoomWidget, activity.manageRoomContainer.getChildAt(1))

        activityController.resume()
        assertEquals(2, activity.manageRoomContainer.childCount)
        assertEquals(activity.numberOfRoomsText, activity.manageRoomContainer.getChildAt(0))
        assertEquals(View.GONE, activity.numberOfRoomsText.visibility)
        assertEquals(activity.manageRoomViewModel.manageRoomWidget, activity.manageRoomContainer.getChildAt(1))
    }

    @Test
    fun testTabNotVisibleForOneRoom() {
        activity.manageRoomViewModel.refreshItinCardDataSubject.onNext(itinCardDataHotel)

        assertEquals(View.GONE, activity.numberOfRoomsText.visibility)
        assertEquals(View.GONE, activity.roomTabs.visibility)
    }

    @Test
    fun testTabVisibleForMoreThanOneRoom() {
        val room = TripHotelRoom("xyz", "2 beds", "BOOKED", null, null, null, emptyList(), emptyList())
        itinCardDataHotel.rooms.add(room)
        activity.manageRoomViewModel.refreshItinCardDataSubject.onNext(itinCardDataHotel)

        assertEquals(View.VISIBLE, activity.numberOfRoomsText.visibility)
        assertEquals("2 total rooms", activity.numberOfRoomsText.text)
        assertEquals(View.VISIBLE, activity.roomTabs.visibility)
        assertEquals(2, activity.roomTabs.tabCount)
    }

    @Test
    fun testTabMode() {
        activity.manageRoomViewModel.refreshItinCardDataSubject.onNext(itinCardDataHotel)
        assertEquals(View.GONE, activity.roomTabs.visibility)

        val room = TripHotelRoom("xyz", "2 beds", "BOOKED", null, null, null, emptyList(), emptyList())
        itinCardDataHotel.rooms.add(room)
        activity.manageRoomViewModel.refreshItinCardDataSubject.onNext(itinCardDataHotel)
        assertEquals(View.VISIBLE, activity.roomTabs.visibility)
        assertEquals(2, activity.roomTabs.tabCount)
        assertEquals(TabLayout.MODE_FIXED, activity.roomTabs.tabMode)

        val room2 = TripHotelRoom("mls", "2 beds", "BOOKED", null, null, null, emptyList(), emptyList())
        val room3 = TripHotelRoom("asd", "2 beds", "BOOKED", null, null, null, emptyList(), emptyList())
        itinCardDataHotel.rooms.add(room2)
        itinCardDataHotel.rooms.add(room3)
        activity.manageRoomViewModel.refreshItinCardDataSubject.onNext(itinCardDataHotel)

        assertEquals(View.VISIBLE, activity.roomTabs.visibility)
        assertEquals(4, activity.roomTabs.tabCount)
        assertEquals(TabLayout.MODE_SCROLLABLE, activity.roomTabs.tabMode)
    }

    @Test
    fun testTabClicked() {
        itinCardDataHotel.rooms[0].hotelConfirmationNumber = "abc"
        val room = TripHotelRoom("xyz", "2 beds", "BOOKED", null, null, null, emptyList(), emptyList())
        itinCardDataHotel.rooms.add(room)
        activity.manageRoomViewModel.refreshItinCardDataSubject.onNext(itinCardDataHotel)

        assertEquals(View.VISIBLE, activity.roomTabs.visibility)
        assertEquals(2, activity.manageRoomContainer.childCount)
        assertEquals(View.VISIBLE, activity.manageRoomViewModel.manageRoomWidget.hotelManageBookingHelpView.hotelConfirmationNumber.visibility)
        assertEquals("Confirmation # abc", activity.manageRoomViewModel.manageRoomWidget.hotelManageBookingHelpView.hotelConfirmationNumber.text)

        activity.roomTabs.getTabAt(1)?.select()

        assertEquals(2, activity.manageRoomContainer.childCount)
        assertEquals(View.VISIBLE, activity.manageRoomViewModel.manageRoomWidget.hotelManageBookingHelpView.hotelConfirmationNumber.visibility)
        assertEquals("Confirmation # xyz", activity.manageRoomViewModel.manageRoomWidget.hotelManageBookingHelpView.hotelConfirmationNumber.text)
    }

    @Test
    fun testExitActivityWhenNoRooms() {
        itinCardDataHotel.rooms.clear()
        activity.manageRoomViewModel.refreshItinCardDataSubject.onNext(itinCardDataHotel)

        val shadowActivity = Shadows.shadowOf(activity)
        assertTrue(shadowActivity.isFinishing)
    }

    @Test
    fun tesTabDisappears() {
        itinCardDataHotel.rooms[0].hotelConfirmationNumber = "abc"
        val room = TripHotelRoom("xyz", "2 beds", "BOOKED", null, null, null, emptyList(), emptyList())
        itinCardDataHotel.rooms.add(room)
        activity.manageRoomViewModel.refreshItinCardDataSubject.onNext(itinCardDataHotel)

        assertEquals(View.VISIBLE, activity.roomTabs.visibility)
        assertEquals(2, activity.manageRoomContainer.childCount)
        assertEquals(activity.manageRoomViewModel.manageRoomWidget, activity.manageRoomContainer.getChildAt(1))
        assertEquals(View.VISIBLE, activity.numberOfRoomsText.visibility)
        assertEquals("2 total rooms", activity.numberOfRoomsText.text)

        itinCardDataHotel.rooms.removeAt(1)
        activity.manageRoomViewModel.refreshItinCardDataSubject.onNext(itinCardDataHotel)

        assertEquals(View.GONE, activity.roomTabs.visibility)
        assertEquals(View.GONE, activity.numberOfRoomsText.visibility)
    }
}
