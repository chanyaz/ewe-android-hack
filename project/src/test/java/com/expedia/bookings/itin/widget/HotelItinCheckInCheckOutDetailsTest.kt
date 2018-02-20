package com.expedia.bookings.itin.widget

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinCheckInCheckOutDetailsTest {

    lateinit var hotelItinCheckinCheckOutWidget: HotelItinCheckInCheckOutDetails
    private lateinit var activity: HotelItinDetailsActivity

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinDetailsActivity::class.java).create().start().get()
        activity.setTheme(R.style.ItinTheme)
        hotelItinCheckinCheckOutWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_checkin_checkout_details, null) as HotelItinCheckInCheckOutDetails
    }

    @Test
    fun testItinHotelCheckInCheckoutDate() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)

        val checkInDate = LocaleBasedDateFormatUtils.dateTimeToEEEMMMd(itinCardDataHotel.startDate)
        val checkOutDate = LocaleBasedDateFormatUtils.dateTimeToEEEMMMd(itinCardDataHotel.endDate)
        val checkInContDesc = LocaleBasedDateFormatUtils.dateTimeToEEEEMMMd(itinCardDataHotel.startDate)
        val checkOutContDesc = LocaleBasedDateFormatUtils.dateTimeToEEEEMMMd(itinCardDataHotel.endDate)

        assertEquals(checkInDate, hotelItinCheckinCheckOutWidget.checkInDateView.text)
        assertEquals(checkInContDesc, hotelItinCheckinCheckOutWidget.checkInDateView.contentDescription)
        assertEquals(checkOutDate, hotelItinCheckinCheckOutWidget.checkOutDateView.text)
        assertEquals(checkOutContDesc, hotelItinCheckinCheckOutWidget.checkOutDateView.contentDescription)
        assertEquals(itinCardDataHotel.checkInTime?.toLowerCase(), hotelItinCheckinCheckOutWidget.checkInTimeView.text)
        assertEquals(itinCardDataHotel.checkOutTime?.toLowerCase(), hotelItinCheckinCheckOutWidget.checkOutTimeView.text)
    }

    @Test
    fun testItinHotelCheckInPolicies() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)

        assertEquals("Check-in policies", hotelItinCheckinCheckOutWidget.checkInOutPoliciesButtonText.text.toString())
        hotelItinCheckinCheckOutWidget.checkInOutPoliciesContainer.performClick()
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val checkInPolicesText = alertDialog.findViewById<View>(R.id.fragment_dialog_scrollable_text_content) as TextView
        assertEquals(true, alertDialog.isShowing)
        assertEquals("Minimum check-in age is 18\nCheck-in time starts at 3 PM", checkInPolicesText.text.toString())
    }

    @Test
    fun testCheckInCheckOutViewText() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)
        assertEquals(hotelItinCheckinCheckOutWidget.checkInTimeView.text.toString(), itinCardDataHotel.checkInTime.toLowerCase())
        assertEquals(hotelItinCheckinCheckOutWidget.checkOutTimeView.text.toString(), itinCardDataHotel.checkOutTime.toLowerCase())

        (itinCardDataHotel.tripComponent as TripHotel).checkInTime = null
        (itinCardDataHotel.tripComponent as TripHotel).checkOutTime = null
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)
        assertEquals(hotelItinCheckinCheckOutWidget.checkInTimeView.text.toString(), JodaUtils.formatDateTime(activity, itinCardDataHotel.startDate, DateUtils.FORMAT_SHOW_TIME).toLowerCase())
        assertEquals(hotelItinCheckinCheckOutWidget.checkOutTimeView.text.toString(), JodaUtils.formatDateTime(activity, itinCardDataHotel.endDate, DateUtils.FORMAT_SHOW_TIME).toLowerCase())
    }

    @Test
    fun checkInOutPoliciesButtonTextCheckTest() {
        var itinCardDataHotel = ItinCardDataHotelBuilder().build()
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)
        assertEquals(activity.getString(R.string.itin_hotel_check_in_policies_dialog_title), hotelItinCheckinCheckOutWidget.checkInOutPoliciesButtonText.text)
        hotelItinCheckinCheckOutWidget.checkInOutPoliciesContainer.performClick()
        var dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertEquals(true, dialog.isShowing)
        assertEquals(View.GONE, dialog.findViewById<View>(R.id.fragment_dialog_second_heading).visibility)
        assertEquals(View.GONE, dialog.findViewById<View>(R.id.fragment_dialog_scrollable_second_text_content).visibility)

        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.TripsHotelsM2)
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)
        assertEquals(activity.getString(R.string.itin_hotel_check_in_policies_dialog_title), hotelItinCheckinCheckOutWidget.checkInOutPoliciesButtonText.text)
        hotelItinCheckinCheckOutWidget.checkInOutPoliciesContainer.performClick()
        dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertEquals(true, dialog.isShowing)
        assertEquals(View.GONE, dialog.findViewById<View>(R.id.fragment_dialog_second_heading).visibility)
        assertEquals(View.GONE, dialog.findViewById<View>(R.id.fragment_dialog_scrollable_second_text_content).visibility)

        itinCardDataHotel = ItinCardDataHotelBuilder().withSpecialInstructions().build()
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)
        assertEquals(activity.getString(R.string.itin_hotel_check_in_policies_and_special_instruction), hotelItinCheckinCheckOutWidget.checkInOutPoliciesButtonText.text)
        hotelItinCheckinCheckOutWidget.checkInOutPoliciesContainer.performClick()
        dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertEquals(View.VISIBLE, dialog.findViewById<View>(R.id.fragment_dialog_second_heading).visibility)
        assertEquals(View.VISIBLE, dialog.findViewById<View>(R.id.fragment_dialog_scrollable_second_text_content).visibility)
        assertEquals("No running in the halls", (dialog.findViewById<View>(R.id.fragment_dialog_scrollable_second_text_content) as TextView).text.toString())
    }

    @Test
    fun testTitleContentWithoutLateArrival() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)
        hotelItinCheckinCheckOutWidget.checkInOutPoliciesContainer.performClick()
        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertEquals("Minimum check-in age is 18\nCheck-in time starts at 3 PM", dialog.findViewById<TextView>(R.id.fragment_dialog_scrollable_text_content).text.toString())
    }

    @Test
    fun testTitleContentWithLateArrival() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val lateArrivalInstructions = "Your room/unit will be guaranteed for late arrival."
        (itinCardDataHotel.getTripComponent() as TripHotel).lateArrivalInstructions = lateArrivalInstructions
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)
        hotelItinCheckinCheckOutWidget.checkInOutPoliciesContainer.performClick()
        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        val expectedString = "Minimum check-in age is 18\nCheck-in time starts at 3 PM\n" + lateArrivalInstructions
        assertEquals(expectedString, dialog.findViewById<TextView>(R.id.fragment_dialog_scrollable_text_content).text.toString())
    }

    @Test
    fun testForCheckInPoliciesButtonText() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val checkInPoliciesArray = ArrayList<String>()
        checkInPoliciesArray.add("Check-in time starts at 4 PM")
        itinCardDataHotel.property.specialInstruction = ArrayList()
        itinCardDataHotel.property.checkInPolicies = checkInPoliciesArray
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.TripsHotelsM2)
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)
        assertEquals(hotelItinCheckinCheckOutWidget.checkInOutPoliciesButtonText.text.toString(), activity.resources.getString(R.string.itin_hotel_check_in_policies_dialog_title))
    }

    @Test
    fun testForSpecialInstructionsButtonText() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val specialInstructionArray = ArrayList<String>()
        specialInstructionArray.add("An adult age 18 or older must assume all liability for the booking. ")
        itinCardDataHotel.property.specialInstruction = specialInstructionArray
        itinCardDataHotel.property.checkInPolicies = ArrayList()
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.TripsHotelsM2)
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)
        assertEquals(hotelItinCheckinCheckOutWidget.checkInOutPoliciesButtonText.text.toString(), activity.resources.getString(R.string.itin_hotel_special_instruction))
    }

    @Test
    fun testForCheckInPoliciesAndSpecialInstructionsButtonText() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val specialInstructionArray = ArrayList<String>()
        specialInstructionArray.add("An adult age 18 or older must assume all liability for the booking. ")
        val checkInPoliciesArray = ArrayList<String>()
        checkInPoliciesArray.add("Check-in time starts at 4 PM")
        itinCardDataHotel.property.specialInstruction = specialInstructionArray
        itinCardDataHotel.property.checkInPolicies = checkInPoliciesArray
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.TripsHotelsM2)
        hotelItinCheckinCheckOutWidget.setUpWidget(itinCardDataHotel)
        assertEquals(hotelItinCheckinCheckOutWidget.checkInOutPoliciesButtonText.text.toString(), activity.resources.getString(R.string.itin_hotel_check_in_policies_and_special_instruction))
    }
}
