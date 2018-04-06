package com.expedia.bookings.itin.widget

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.itin.hotel.manageBooking.HotelItinManageBookingActivity
import com.expedia.bookings.itin.hotel.details.HotelItinRoomAmenity
import com.expedia.bookings.itin.hotel.details.HotelItinRoomDetails
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowDrawable
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
        roomDetailsWidget.setUpRoomAndOccupantInfo(itinCardDataHotel.getHotelRoom(0)!!)

        val expected = itinCardDataHotel.property.itinRoomType + ", " + itinCardDataHotel.property.itinBedType
        assertEquals(expected, roomDetailsWidget.roomDetailsText.text)
    }

    @Test
    fun reservedForWhenGuestNameOccupantInfoNotEmpty() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        roomDetailsWidget.setUpRoomAndOccupantInfo(itinCardDataHotel.getHotelRoom(0)!!)

        val expectedString = "Kevin Carpenter, 1 adult"
        assertEquals(expectedString, roomDetailsWidget.guestName.text)
    }

    @Test
    fun reservedForAdultsChildrenAndInfant() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().withAdultChildInfantCount(3, 2, 1).build()

        roomDetailsWidget.setUpRoomAndOccupantInfo(itinCardDataHotel.getHotelRoom(0)!!)

        val expectedString = "Kevin Carpenter, 3 adults, 2 children, 1 infant"
        assertEquals(expectedString, roomDetailsWidget.guestName.text)
    }

    @Test
    fun reservedForAdults1ChildAndInfants() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().withAdultChildInfantCount(4, 1, 2).build()

        roomDetailsWidget.setUpRoomAndOccupantInfo(itinCardDataHotel.getHotelRoom(0)!!)

        val expectedString = "Kevin Carpenter, 4 adults, 1 child, 2 infants"
        assertEquals(expectedString, roomDetailsWidget.guestName.text)
    }

    @Test
    fun reservedForAdultsChildNullInfantNotNull() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().withAdultChildInfantCount(2, 0, 1).build()

        roomDetailsWidget.setUpRoomAndOccupantInfo(itinCardDataHotel.getHotelRoom(0)!!)

        val expectedString = "Kevin Carpenter, 2 adults, 1 infant"
        assertEquals(expectedString, roomDetailsWidget.guestName.text)
    }

    @Test
    fun reservedForAdultsChildNotNullInfantNull() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().withAdultChildInfantCount(2, 3, 0).build()

        roomDetailsWidget.setUpRoomAndOccupantInfo(itinCardDataHotel.getHotelRoom(0)!!)

        val expectedString = "Kevin Carpenter, 2 adults, 3 children"
        assertEquals(expectedString, roomDetailsWidget.guestName.text)
    }

    @Test
    fun reservedForWhenGuestNameNullOccupantInfoNotNull() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().withPrimaryOccupantFullName("").build()
        roomDetailsWidget.setUpRoomAndOccupantInfo(itinCardDataHotel.getHotelRoom(0)!!)

        val expectedString = "1 adult"
        assertEquals(expectedString, roomDetailsWidget.guestName.text)
    }

    @Test
    fun reservedForWhenGuestNameNotNullOccupantInfoNull() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().withAdultCount(0).build()
        roomDetailsWidget.setUpRoomAndOccupantInfo(itinCardDataHotel.getHotelRoom(0)!!)

        val expectedString = "Kevin Carpenter"
        assertEquals(expectedString, roomDetailsWidget.guestName.text)
    }

    @Test
    fun reservedForWhenGuestNameAndOccupantInfoAreNull() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().withEmptyGuestNameAndOccupants("", 0).build()
        roomDetailsWidget.setUpRoomAndOccupantInfo(itinCardDataHotel.getHotelRoom(0)!!)

        assertEquals(View.GONE, roomDetailsWidget.reservedFor.visibility)
        assertEquals(View.GONE, roomDetailsWidget.guestName.visibility)
    }

    @Test
    fun roomDetailsCollapseExpand() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        roomDetailsWidget.setUpRoomAndOccupantInfo(itinCardDataHotel.getHotelRoom(0)!!)
        roomDetailsWidget.collapseRoomDetailsView()
        assertEquals(View.GONE, roomDetailsWidget.expandedRoomDetails.visibility)

        roomDetailsWidget.expandRoomDetailsView()
        assertEquals(View.VISIBLE, roomDetailsWidget.expandedRoomDetails.visibility)
    }

    @Test
    fun roomRequestsAreCorrect() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        roomDetailsWidget.setUpRoomAndOccupantInfo(itinCardDataHotel.getHotelRoom(0)!!)
        val expectedString = "Non-smoking\n" +
                "1 king bed\n" +
                "Accessible bathroom, Roll-in shower, In-room accessibility\n" +
                "\"Please bring New York Times to the room\"\n" +
                "Extra adult bed"
        assertEquals(expectedString, roomDetailsWidget.roomRequestsText.text)
    }

    @Test
    fun roomAmenitiesAreDisplayedAndCorrect() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        roomDetailsWidget.setUpAndShowAmenities(itinCardDataHotel.getHotelRoom(0)!!)

        assertEquals(View.VISIBLE, roomDetailsWidget.amenitiesContainer.visibility)
        if (roomDetailsWidget.amenitiesContainer.childCount > 0) {
            val amenity: HotelItinRoomAmenity? = roomDetailsWidget.amenitiesContainer.getChildAt(0) as HotelItinRoomAmenity?
            assertEquals("Free\nWifi", amenity?.getLabel()?.text?.toString())
            val shadowDrawable: ShadowDrawable = Shadows.shadowOf(amenity?.getIcon()?.drawable)
            assertEquals(R.drawable.itin_hotel_free_wifi, shadowDrawable.createdFromResId)
        }
    }

    @Test
    fun testItinHotelChangeCancelRules() {
        val hotelManageBookingActivity = Robolectric.buildActivity(HotelItinManageBookingActivity::class.java).create().start().get()
        hotelManageBookingActivity.setTheme(R.style.ItinTheme)
        val roomWidget = LayoutInflater.from(hotelManageBookingActivity).inflate(R.layout.test_hotel_itin_room_details, null) as HotelItinRoomDetails
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        roomWidget.setupAndShowChangeAndCancelRules(itinCardDataHotel.changeAndCancelRules)
        assertEquals(View.VISIBLE, roomWidget.changeCancelRulesContainer.visibility)

        roomWidget.changeCancelRulesContainer.performClick()
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val rulesText = alertDialog.findViewById<View>(R.id.fragment_dialog_scrollable_text_content) as TextView
        assertEquals(true, alertDialog.isShowing)
        assertEquals("We understand that sometimes plans fall through. We do not charge a cancel or change fee. When the property charges such fees in accordance with its own policies, the cost will be passed on to you. Adante Hotel, a C-Two Hotel charges the following cancellation and change fees.\n" +
                "Cancellations or changes made after 3:00PM (Pacific Daylight Time (US & Canada); Tijuana) on Oct 17, 2017 or no-shows are subject to a property fee equal to 100% of the total amount paid for the reservation.\n" +
                "Prices and hotel availability are not guaranteed until full payment is received.If you would like to book multiple rooms, you must use a different name for each room. Otherwise, the duplicate reservation will be canceled by the hotel.", rulesText.text.toString())
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.Info.Change-Cancel", mockAnalyticsProvider)
    }

    @Test
    fun testDoOnClick() {
        assertEquals(View.VISIBLE, roomDetailsWidget.collapsedRoomDetails.visibility)
        assertEquals(View.GONE, roomDetailsWidget.expandedRoomDetails.visibility)
        roomDetailsWidget.doOnClick()
        assertEquals(View.VISIBLE, roomDetailsWidget.collapsedRoomDetails.visibility)
        assertEquals(View.VISIBLE, roomDetailsWidget.expandedRoomDetails.visibility)
        roomDetailsWidget.doOnClick()
        assertEquals(View.VISIBLE, roomDetailsWidget.collapsedRoomDetails.visibility)
        assertEquals(View.GONE, roomDetailsWidget.expandedRoomDetails.visibility)
    }

    @Test
    fun testShowChevron() {
        assertEquals(View.GONE, roomDetailsWidget.roomDetailsChevron.visibility)
        roomDetailsWidget.showChevron()
        assertEquals(View.VISIBLE, roomDetailsWidget.roomDetailsChevron.visibility)
    }
}
