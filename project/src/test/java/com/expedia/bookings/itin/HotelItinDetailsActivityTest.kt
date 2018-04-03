package com.expedia.bookings.itin

import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.itin.hotel.details.HotelItinBookingDetails
import com.expedia.bookings.itin.hotel.details.HotelItinCheckInCheckOutDetails
import com.expedia.bookings.itin.hotel.details.HotelItinDetailsActivity
import com.expedia.bookings.itin.hotel.details.HotelItinImageWidget
import com.expedia.bookings.itin.hotel.details.HotelItinRoomDetails
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinDetailsActivityTest {
    private lateinit var activity: HotelItinDetailsActivity
    private lateinit var itinCardDataHotel: ItinCardDataHotel
    private lateinit var intentBuilder: WebViewActivity.IntentBuilder
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinDetailsActivity::class.java).create().get()
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
        intentBuilder = WebViewActivity.IntentBuilder(RuntimeEnvironment.application)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun sharedItinView() {
        val sharedItin = ItinCardDataHotelBuilder().isSharedItin(true).build()
        activity.itinCardDataHotel = sharedItin
        activity.setUpWidgets()

        val hotelImageView: HotelItinImageWidget = activity.hotelImageView
        assertEquals(View.VISIBLE, hotelImageView.visibility)

        val hotelCheckinCheckout: HotelItinCheckInCheckOutDetails = activity.checkinCheckoutView
        assertEquals(View.VISIBLE, hotelCheckinCheckout.visibility)

        val bookingDetailsView: HotelItinBookingDetails = activity.hotelBookingDetailsView
        assertEquals(View.GONE, bookingDetailsView.visibility)

        val roomDetailsHeader: View = activity.roomDetailsHeader
        assertEquals(View.GONE, roomDetailsHeader.visibility)

        val multiRoomContainer = activity.multiRoomContainer
        assertEquals(View.GONE, multiRoomContainer.visibility)

        val shareIcon: View = activity.findViewById(R.id.itin_share_button)
        assertEquals(View.GONE, shareIcon.visibility)
    }

    @Test
    fun testRoomDetailsExpansionNoRoomCase() {
        val itinCardDataHotelMock = Mockito.spy(itinCardDataHotel)
        activity.itinCardDataHotel = itinCardDataHotelMock
        Mockito.`when`(activity.itinCardDataHotel.rooms).thenReturn(emptyList())
        activity.setUpWidgets()
        assertEquals(View.GONE, activity.roomDetailsHeader.visibility)
        assertEquals(View.GONE, activity.multiRoomContainer.visibility)
    }

    @Test
    fun testRoomDetailsExpansionSharedItin() {
        val itinCardDataHotelMock = Mockito.spy(itinCardDataHotel)
        activity.itinCardDataHotel = itinCardDataHotelMock
        Mockito.`when`(activity.itinCardDataHotel.isSharedItin).thenReturn(true)
        activity.setUpWidgets()
        assertEquals(View.GONE, activity.roomDetailsHeader.visibility)
        assertEquals(View.GONE, activity.multiRoomContainer.visibility)
        assertEquals(0, activity.multiRoomContainer.childCount)
    }

    @Test
    fun testRoomDetailsExpansion() {
        activity.itinCardDataHotel = itinCardDataHotel
        activity.setUpWidgets()

        assertEquals(View.VISIBLE, activity.multiRoomContainer.visibility)
        assertEquals(1, activity.multiRoomContainer.childCount)
        val roomDetailsView = activity.multiRoomContainer.getChildAt(0) as HotelItinRoomDetails
        assertEquals(View.GONE, roomDetailsView.expandedRoomDetails.visibility)
        assertEquals(View.VISIBLE, roomDetailsView.roomDetailsChevron.visibility)
        assertEquals(View.GONE, roomDetailsView.changeCancelRulesContainer.visibility)
    }

    @Test
    fun testRoomAmenitiesVisible() {
        activity.itinCardDataHotel = itinCardDataHotel
        activity.setUpWidgets()

        val roomDetailsView = activity.multiRoomContainer.getChildAt(0) as HotelItinRoomDetails
        roomDetailsView.expandRoomDetailsView()
        assertEquals(View.VISIBLE, roomDetailsView.expandedRoomDetails.visibility)
        assertEquals(View.VISIBLE, roomDetailsView.amenitiesContainer.visibility)
    }

    @Test
    fun testMultiRoomContainer() {
        activity.itinCardDataHotel = itinCardDataHotel
        val rooms = activity.itinCardDataHotel.rooms
        rooms[0].roomType = "testroom1"

        assertEquals(0, activity.multiRoomContainer.childCount)
        activity.setUpWidgets()
        assertEquals(1, activity.multiRoomContainer.childCount)
        assertEquals("Your room", activity.roomDetailsHeader.text)
        val child1 = activity.multiRoomContainer.getChildAt(0) as HotelItinRoomDetails
        assertEquals("testroom1, 1 king bed", child1.roomDetailsText.text)

        val secondRoom = rooms[0].copy(roomType = "testroom2")
        rooms.add(secondRoom)
        activity.setUpWidgets()
        assertEquals(2, activity.multiRoomContainer.childCount)
        assertEquals("Your rooms", activity.roomDetailsHeader.text)
        assertEquals("testroom1, 1 king bed", child1.roomDetailsText.text)
        val child2 = activity.multiRoomContainer.getChildAt(1) as HotelItinRoomDetails
        assertEquals("testroom2, 1 king bed", child2.roomDetailsText.text)
    }

    @Test
    fun testRoomExpandCollapse() {
        activity.itinCardDataHotel = itinCardDataHotel
        val rooms = activity.itinCardDataHotel.rooms
        rooms[0].roomType = "testroom1"
        val secondRoom = rooms[0].copy(roomType = "testroom2")
        rooms.add(secondRoom)
        activity.setUpWidgets()

        val room1 = activity.multiRoomContainer.getChildAt(0) as HotelItinRoomDetails
        val room2 = activity.multiRoomContainer.getChildAt(1) as HotelItinRoomDetails
        assertEquals(View.VISIBLE, room1.collapsedRoomDetails.visibility)
        assertEquals(View.VISIBLE, room2.collapsedRoomDetails.visibility)
        room1.collapsedRoomDetails.performClick()
        assertEquals(View.VISIBLE, room1.expandedRoomDetails.visibility)
        assertEquals(View.GONE, room2.expandedRoomDetails.visibility)

        room2.collapsedRoomDetails.performClick()
        assertEquals(View.GONE, room1.expandedRoomDetails.visibility)
        assertEquals(View.VISIBLE, room2.expandedRoomDetails.visibility)
    }
}
