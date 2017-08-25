package com.expedia.bookings.widget.itin

import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinDetailsActivityTest {
    lateinit private var activity: HotelItinDetailsActivity
    lateinit private var itinCardDataHotel: ItinCardDataHotel
    lateinit private var intentBuilder: WebViewActivity.IntentBuilder

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinDetailsActivity::class.java).create().get()
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
        intentBuilder = WebViewActivity.IntentBuilder(RuntimeEnvironment.application)
    }

    @Test
    fun sharedItinView() {
        val sharedItin = ItinCardDataHotelBuilder().isSharedItin(true).build()
        activity.itinCardDataHotel = sharedItin
        activity.setUpWidgets()

        val roomDetailsView: HotelItinRoomDetails = activity.roomDetailsView
        assertEquals(View.GONE, roomDetailsView.visibility)

        val hotelImageView: HotelItinImage = activity.hotelImageView
        assertEquals(View.VISIBLE, hotelImageView.visibility)

        val hotelCheckinCheckout: HotelItinCheckInCheckOutDetails = activity.checkinCheckoutView
        assertEquals(View.VISIBLE, hotelCheckinCheckout.visibility)

        val bookingDetailsView: HotelItinBookingDetails = activity.hotelBookingDetailsView
        assertEquals(View.GONE, bookingDetailsView.visibility)

        val roomDetailsHeader: View = activity.roomDetailsHeader
        assertEquals(View.GONE, roomDetailsHeader.visibility)

        val shareIcon: View = activity.findViewById(R.id.itin_share_button)
        assertEquals(View.GONE, shareIcon.visibility)
    }

    @Test
    fun testRoomDetailsExpansion() {
        val itinCardDataHotelMock = Mockito.spy(itinCardDataHotel)
        activity.itinCardDataHotel = itinCardDataHotelMock
        Mockito.`when`(activity.itinCardDataHotel.lastHotelRoom).thenReturn(null)
        activity.setUpWidgets()
        assertEquals(View.GONE, activity.roomDetailsView.expandedRoomDetails.visibility)
        assertEquals(View.GONE, activity.roomDetailsChevron.visibility)
        assertEquals(false, activity.roomDetailsView.isRowClickable)
        assertEquals(View.GONE, activity.roomDetailsView.changeCancelRulesContainer.visibility)

        activity.itinCardDataHotel = itinCardDataHotel
        activity.setUpWidgets()
        assertEquals(View.GONE, activity.roomDetailsView.expandedRoomDetails.visibility)
        assertEquals(View.VISIBLE, activity.roomDetailsChevron.visibility)
        assertEquals(true, activity.roomDetailsView.isRowClickable)
        assertEquals(View.GONE, activity.roomDetailsView.changeCancelRulesContainer.visibility)
    }
}