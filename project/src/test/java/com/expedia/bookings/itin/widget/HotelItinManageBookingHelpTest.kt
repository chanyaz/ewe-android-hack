package com.expedia.bookings.itin.widget

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import com.expedia.account.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.itin.hotel.manageBooking.HotelItinManageBookingActivity
import com.expedia.bookings.itin.hotel.manageBooking.HotelItinManageBookingHelp
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.squareup.phrase.Phrase
import kotlinx.android.synthetic.main.widget_itin_more_help.view.itin_hotel_manage_booking_message_hotel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelItinManageBookingHelpTest {

    lateinit var manageBookingHelpWidget: HotelItinManageBookingHelp
    private lateinit var activity: HotelItinManageBookingActivity

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinManageBookingActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        manageBookingHelpWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_manage_booking_help, null) as HotelItinManageBookingHelp
    }

    @Test
    fun testCallHotel() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        manageBookingHelpWidget.setUpWidget(itinCardDataHotel)

        assertEquals(Phrase.from(activity, R.string.itin_more_help_text_TEMPLATE)
                .put("supplier", itinCardDataHotel.propertyName).format().toString(), manageBookingHelpWidget.helpText.text)
        assertEquals(itinCardDataHotel.localPhone, manageBookingHelpWidget.callHotelButton.text.toString())
        assertEquals("Call hotel at " + itinCardDataHotel.localPhone + ". Button", manageBookingHelpWidget.callHotelButton.contentDescription)

        manageBookingHelpWidget.callHotelButton.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.Call.Hotel", mockAnalyticsProvider)
    }

    @Test
    fun testCallHotelTextViewGetsFocusedOnTouch() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        manageBookingHelpWidget.setUpWidget(itinCardDataHotel)

        val touchEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        manageBookingHelpWidget.callHotelButton.dispatchTouchEvent(touchEvent)
        assertTrue(manageBookingHelpWidget.callHotelButton.hasFocus())
    }

    @Test
    fun testMessageHotelUrlAvailable() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        itinCardDataHotel.property.epcConversationUrl = "google.com"
        manageBookingHelpWidget.setUpWidget(itinCardDataHotel)
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val messageHotelWidget = manageBookingHelpWidget.itin_hotel_manage_booking_message_hotel
        assertEquals(messageHotelWidget.visibility, View.VISIBLE)
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        messageHotelWidget.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.Message.Hotel", mockAnalyticsProvider)
    }

    @Test
    fun testMessageHotelNoURL() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        itinCardDataHotel.property.epcConversationUrl = ""
        manageBookingHelpWidget.setUpWidget(itinCardDataHotel)
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val messageHotelWidget = manageBookingHelpWidget.itin_hotel_manage_booking_message_hotel
        assertEquals(messageHotelWidget.visibility, View.GONE)
        messageHotelWidget.performClick()
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    @Test
    fun testSelectionToolbarCallButtonClicked() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        manageBookingHelpWidget.setUpWidget(itinCardDataHotel)
        activity.menuInflater.inflate(R.menu.test_menu, activity.toolbar.menu)
        activity.toolbar.menu.add(0, android.R.id.textAssist, 0, "")
        val menuItem = activity.toolbar.menu.findItem(android.R.id.textAssist)

        manageBookingHelpWidget.callHotelButton.customSelectionActionModeCallback.onActionItemClicked(null, menuItem)
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.Call.Hotel", mockAnalyticsProvider)
    }

    @Test
    @Config(constants = BuildConfig::class, sdk = intArrayOf(26))
    fun testSelectingConfirmationNumberRemovesPhoneButtonInActionToolbar() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        manageBookingHelpWidget.setUpWidget(itinCardDataHotel)

        activity.menuInflater.inflate(R.menu.test_menu, activity.toolbar.menu)
        activity.toolbar.menu.add(0, android.R.id.textAssist, 0, "")
        assertNotNull(activity.toolbar.menu.findItem(android.R.id.textAssist))

        manageBookingHelpWidget.confirmationNumber.customSelectionActionModeCallback.onPrepareActionMode(null, activity.toolbar.menu)
        assertNull(activity.toolbar.menu.findItem(android.R.id.textAssist))
    }
}
