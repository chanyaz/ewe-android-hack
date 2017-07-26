package com.expedia.bookings.widget.itin

import android.text.format.DateFormat
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.ClipboardUtils
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
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun testRoomDetailsWidget() {
        val roomDetailsView: HotelItinRoomDetails = activity.roomDetailsView
        roomDetailsView.setUpWidget(itinCardDataHotel)
        assertEquals(itinCardDataHotel.property.itinRoomType + ", " + itinCardDataHotel.property.itinBedType, roomDetailsView.roomDetailsText.text)
        assertEquals(View.VISIBLE, roomDetailsView.visibility)
    }

    @Test
    fun testHotelItinImage() {
        val hotelImageView: HotelItinImage = activity.hotelImageView
        hotelImageView.setUpWidget(itinCardDataHotel)
        assertEquals(itinCardDataHotel.propertyName, hotelImageView.hotelNameTextView.text)
    }

    fun testMapWidget() {
        val locationDetailsView: HotelItinLocationDetails = activity.locationDetailsView
        locationDetailsView.setupWidget(itinCardDataHotel)
        assertEquals(View.VISIBLE, locationDetailsView.locationMapImageView.visibility)
        assertEquals(locationDetailsView.addressLine1.text, itinCardDataHotel.propertyLocation.streetAddressString)
        assertEquals(locationDetailsView.addressLine2.text, itinCardDataHotel.propertyLocation.toTwoLineAddressFormattedString())
        assertEquals(locationDetailsView.actionButtons.getmLeftButton().text, itinCardDataHotel.localPhone)
        locationDetailsView.address.performClick()
        val address: String = Phrase.from(activity, R.string.itin_hotel_details_address_clipboard_TEMPLATE)
                .put("addresslineone", locationDetailsView.addressLine1.text.toString())
                .put("addresslinetwo", locationDetailsView.addressLine2.text.toString()).format().toString()
        assertEquals(address, ClipboardUtils.getText(activity))
    }

    @Test
    fun testItinToolbarWidget() {
        val hotelItinToolbar: HotelItinToolbar = activity.toolbar
        hotelItinToolbar.setUpWidget(itinCardDataHotel, itinCardDataHotel.propertyName)
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val startDate = DateUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.startDate.toString().substringBefore("T")))
        val endDate = DateUtils.localDateToMMMd(formatter.parseLocalDate(itinCardDataHotel.endDate.toString().substringBefore("T")))
        assertEquals(itinCardDataHotel.propertyName, hotelItinToolbar.toolbarTitleTextView.text)
        assertEquals(startDate + " - " + endDate, hotelItinToolbar.toolbarSubtitleTextView.text)
        assertEquals(startDate + " to " + endDate, hotelItinToolbar.toolbarSubtitleTextView.contentDescription)
    }

    @Test
    fun testItinBookingDetailsWidget() {
        val bookingDetailsView: HotelItinBookingDetails = activity.hotelBookingDetailsView
        bookingDetailsView.setUpWidget(itinCardDataHotel)

        //price summary - toolbar title and url check
        bookingDetailsView.priceSummaryCard.performClick()
        var shadowActivity = Shadows.shadowOf(activity)
        var intent = shadowActivity.nextStartedActivity
        assertEquals(WebViewActivity::class.java.name, intent.component.className)
        assertEquals("Price summary", intent.extras.getString("ARG_TITLE"))
        // we cannot check against the Visitor ID stuff that gets added to the URL, because it adds unique data every time it is called
        assertTrue(intent.extras.getString("ARG_URL").startsWith(itinCardDataHotel.detailsUrl))
        assertTrue(intent.extras.getString("ARG_URL").endsWith("#price-header"))

        //additional info - toolbar title and url check
        bookingDetailsView.additionalInfoCard.performClick()
        shadowActivity = Shadows.shadowOf(activity)
        intent = shadowActivity.nextStartedActivity
        assertEquals(WebViewActivity::class.java.name, intent.component.className)
        assertEquals("Additional information", intent.extras.getString("ARG_TITLE"))
        // we cannot check against the Visitor ID stuff that gets added to the URL, because it adds unique data every time it is called
        assertTrue(intent.extras.getString("ARG_URL").startsWith(itinCardDataHotel.detailsUrl))
    }

    @Test
    fun testItinHotelCheckInCheckoutDate() {
        val hotelCheckinCheckout: HotelItinCheckInCheckOutDetails = activity.checkinCheckoutView
        hotelCheckinCheckout.setUpWidget(itinCardDataHotel)

        val formatPattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEE, MMM d")
        val checkInDate = itinCardDataHotel.startDate.toString(formatPattern)
        val checkOutDate = itinCardDataHotel.endDate.toString(formatPattern)

        assertEquals(checkInDate, hotelCheckinCheckout.checkInDateView.text)
        assertEquals(checkOutDate, hotelCheckinCheckout.checkOutDateView.text)
        assertEquals(itinCardDataHotel.checkInTime?.toLowerCase(), hotelCheckinCheckout.checkInTimeView.text)
        assertEquals(itinCardDataHotel.checkOutTime?.toLowerCase(), hotelCheckinCheckout.checkOutTimeView.text)
    }

    @Test
    fun sharedItinView() {
        val sharedItin = ItinCardDataHotelBuilder().isSharedItin(true).build()
        activity.setUpWidgets(sharedItin)

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
}