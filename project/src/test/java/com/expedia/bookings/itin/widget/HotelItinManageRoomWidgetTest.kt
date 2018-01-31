package com.expedia.bookings.itin.widget

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.itin.vm.HotelItinManageRoomViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinManageRoomWidgetTest {

    lateinit var manageRoomWidget: HotelItinManageRoomWidget
    lateinit var manageRoomViewModel: HotelItinManageRoomViewModel
    lateinit var activity: Activity

    @Before
    fun setup() {
        activity = buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        manageRoomWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_manage_room_widget, null) as HotelItinManageRoomWidget
        manageRoomViewModel = HotelItinManageRoomViewModel(activity)
        manageRoomWidget.viewModel = manageRoomViewModel
    }

    @Test
    fun testRoomDetailsSubject() {
        val itinCardData = ItinCardDataHotelBuilder().build()
        val room = itinCardData.rooms[0]
        room.hotelConfirmationNumber = "12345"
        manageRoomViewModel.roomDetailsSubject.onNext(room)

        assertEquals("Deluxe Room, 1 King Bed, 1 king bed", manageRoomWidget.roomDetailsView.roomDetailsText.text)
        assertEquals(View.VISIBLE, manageRoomWidget.roomDetailsView.expandedRoomDetails.visibility)
        assertEquals(View.VISIBLE, manageRoomWidget.hotelManageBookingHelpView.hotelConfirmationNumber.visibility)
        assertEquals("Confirmation # 12345", manageRoomWidget.hotelManageBookingHelpView.hotelConfirmationNumber.text)
    }

    @Test
    fun testRoomChangeAndCancelRulesSubject() {
        val rules = listOf("abc", "123")
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        manageRoomViewModel.roomChangeAndCancelRulesSubject.onNext(rules)

        assertEquals(View.VISIBLE, manageRoomWidget.roomDetailsView.changeCancelRulesContainer.visibility)
    }

    @Test
    fun testItinCardDataHotelSubject() {
        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        manageRoomViewModel.itinCardDataHotelSubject.onNext(itinCardDataHotel)

        assertEquals(Phrase.from(activity, R.string.itin_hotel_manage_booking_hotel_help_text_TEMPLATE)
                .put("hotelname", itinCardDataHotel.propertyName).format().toString(), manageRoomWidget.hotelManageBookingHelpView.helpText.text)
        assertEquals(itinCardDataHotel.localPhone, manageRoomWidget.hotelManageBookingHelpView.callHotelButton.text)
        assertEquals("Call hotel at " + itinCardDataHotel.localPhone + ". Button", manageRoomWidget.hotelManageBookingHelpView.callHotelButton.contentDescription)

        val customerSupportHeaderText = Phrase.from(activity, R.string.itin_hotel_customer_support_header_text_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        assertEquals(customerSupportHeaderText, manageRoomWidget.hotelCustomerSupportDetailsView.customerSupportTextView.text)
        val itinNumber = Phrase.from(activity, R.string.itin_hotel_itinerary_number_TEMPLATE).put("itinnumber", itinCardDataHotel.tripNumber).format().toString()
        assertEquals(itinNumber, manageRoomWidget.hotelCustomerSupportDetailsView.itineraryNumberTextView.text)
        val userStateManager = Ui.getApplication(RuntimeEnvironment.application).appComponent().userStateManager()
        val user = userStateManager.userSource.user
        val phoneNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(user)
        assertEquals(phoneNumber, manageRoomWidget.hotelCustomerSupportDetailsView.callSupportActionButton.text)
        val supportSite = Phrase.from(activity, R.string.itin_hotel_customer_support_site_header_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        assertEquals(supportSite, manageRoomWidget.hotelCustomerSupportDetailsView.customerSupportSiteButton.text)
    }
}