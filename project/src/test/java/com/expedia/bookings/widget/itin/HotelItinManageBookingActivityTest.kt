package com.expedia.bookings.widget.itin

import android.view.View
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.itin.activity.HotelItinManageBookingActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinManageBookingActivityTest {
    lateinit private var activity: HotelItinManageBookingActivity
    lateinit private var itinCardDataHotel: ItinCardDataHotel
    lateinit private var manageBookingActivity: HotelItinManageBookingActivity
    lateinit private var intentBuilder: WebViewActivity.IntentBuilder

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinManageBookingActivity::class.java)
                .create().get()
        manageBookingActivity = Robolectric.buildActivity(HotelItinManageBookingActivity::class.java).create().get()
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
        intentBuilder = WebViewActivity.IntentBuilder(RuntimeEnvironment.application)
    }

    @Test
    fun testItinToolbarWidget() {
        val hotelItinToolbar: HotelItinToolbar = activity.toolbar
        val titleString = activity.getString(R.string.itin_hotel_manage_booking_header)
        hotelItinToolbar.setUpWidget(itinCardDataHotel, titleString)
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val startDate = DateUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.startDate.toString().substringBefore("T")))
        val endDate = DateUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.endDate.toString().substringBefore("T")))
        assertEquals(activity.getString(R.string.itin_hotel_manage_booking_header), hotelItinToolbar.toolbarTitleTextView.text)
        assertEquals(startDate + " - " + endDate, hotelItinToolbar.toolbarSubtitleTextView.text)
        assertEquals(startDate + " to " + endDate, hotelItinToolbar.toolbarSubtitleTextView.contentDescription)
    }

    @Test
    fun testRoomDetailsView() {
        val roomDetailsView: HotelItinRoomDetails = activity.roomDetailsView
        roomDetailsView.setUpWidget(itinCardDataHotel)
        assertEquals(itinCardDataHotel.property.itinRoomType + ", " + itinCardDataHotel.property.itinBedType, roomDetailsView.roomDetailsText.text)
    }

    @Test
    fun testItinHotelManageBookingHelpWidget() {
        val helpWidget: HotelItinManageBookingHelp = manageBookingActivity.hotelManageBookingHelpView
        helpWidget.setUpWidget(itinCardDataHotel)

        assertEquals(Phrase.from(activity, R.string.itin_hotel_manage_booking_hotel_help_text_TEMPLATE)
                .put("hotelname", itinCardDataHotel.propertyName).format().toString(), helpWidget.helpText.text)
        assertEquals(false, itinCardDataHotel.hasConfirmationNumber())
        assertEquals(View.GONE, helpWidget.hotelConfirmationNumber.visibility)
        assertEquals(itinCardDataHotel.localPhone, helpWidget.callHotelButton.text)
    }

    @Test
    fun testItinHotelCustomerSupportWidget() {
        val customerSupportWidget: HotelItinCustomerSupportDetails = manageBookingActivity.hotelCustomerSupportDetailsView
        customerSupportWidget.setUpWidget(itinCardDataHotel)

        val customerSupportHeaderText = Phrase.from(activity, R.string.itin_hotel_customer_support_header_text_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        assertEquals(customerSupportHeaderText, customerSupportWidget.customerSupportTextView.text)

        val itinNumber = Phrase.from(activity, R.string.itin_hotel_itinerary_number_TEMPLATE).put("itinnumber", itinCardDataHotel.tripNumber).format().toString()
        assertEquals(itinNumber, customerSupportWidget.itineraryNumberTextView.text)

        val phoneNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser())
        assertEquals(phoneNumber, customerSupportWidget.callSupportActionButton.text)

        val supportSite = Phrase.from(activity, R.string.itin_hotel_customer_support_site_header_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        assertEquals(supportSite, customerSupportWidget.customerSupportSiteButton.text)

        customerSupportWidget.customerSupportSiteButton.performClick()
        var shadowActivity = Shadows.shadowOf(activity)
        var intent = shadowActivity.nextStartedActivity
        assertEquals(WebViewActivity::class.java.name, intent.component.className)
        assertEquals("Customer service", intent.extras.getString("ARG_TITLE"))
       // assertEquals(PointOfSale.getPointOfSale().appSupportUrl, intent.extras.getString("ARG_URL"))
    }
}