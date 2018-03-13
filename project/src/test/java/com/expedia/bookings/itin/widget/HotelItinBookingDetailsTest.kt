package com.expedia.bookings.itin.widget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity

import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.itin.hotel.details.HotelItinBookingDetails
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.mobiata.mocke3.mockObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowActivity
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelItinBookingDetailsTest {

    private lateinit var bookingDetailsWidget: HotelItinBookingDetails
    private lateinit var intentBuilder: WebViewActivity.IntentBuilder
    private lateinit var itinCardDataHotel: ItinCardDataHotel
    lateinit var intent: Intent
    lateinit var shadowActivity: ShadowActivity

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        bookingDetailsWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_booking_details, null) as HotelItinBookingDetails
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
        intentBuilder = WebViewActivity.IntentBuilder(RuntimeEnvironment.application)
        shadowActivity = Shadows.shadowOf(activity)
    }

    @Test
    fun priceSummaryToolbarAndUrlAreCorrect() {
        bookingDetailsWidget.setUpWidget(itinCardDataHotel)
        bookingDetailsWidget.priceSummaryCard.performClick()
        intent = shadowActivity.nextStartedActivity
        assertEquals(WebViewActivity::class.java.name, intent.component.className)
        assertEquals("Price summary", intent.extras.getString("ARG_TITLE"))
        // we cannot check against the Visitor ID stuff that gets added to the URL, because it adds unique data every time it is called
        assertTrue(intent.extras.getString("ARG_URL").startsWith(itinCardDataHotel.detailsUrl))
        assertTrue(intent.extras.getString("ARG_URL").endsWith("#price-header"))
    }

    @Test
    fun additionalInfoToolbarAndUrlAreCorrect() {
        bookingDetailsWidget.setUpWidget(itinCardDataHotel)
        bookingDetailsWidget.additionalInfoCard.performClick()
        intent = shadowActivity.nextStartedActivity
        assertEquals(WebViewActivity::class.java.name, intent.component.className)
        assertEquals("Additional information", intent.extras.getString("ARG_TITLE"))
        // we cannot check against the Visitor ID stuff that gets added to the URL, because it adds unique data every time it is called
        assertTrue(intent.extras.getString("ARG_URL").startsWith(itinCardDataHotel.detailsUrl))
    }

    @Test
    fun testPriceSummaryButtonHappy() {
        bookingDetailsWidget.readJsonUtil = MockReadJsonUtil

        bookingDetailsWidget.checkIfWriteJsonEnabled = false
        bookingDetailsWidget.checkIfReadJsonEnabled = false
        bookingDetailsWidget.setUpWidget(itinCardDataHotel)
        assertEquals(View.VISIBLE, bookingDetailsWidget.priceSummaryCard.visibility)
        assertEquals("Pricing and Rewards", bookingDetailsWidget.priceSummaryCard.heading.text)
        assertEquals(View.GONE, bookingDetailsWidget.newPriceSummaryCard.visibility)

        bookingDetailsWidget.checkIfWriteJsonEnabled = true
        bookingDetailsWidget.checkIfReadJsonEnabled = false
        bookingDetailsWidget.setUpWidget(itinCardDataHotel)
        assertEquals(View.VISIBLE, bookingDetailsWidget.priceSummaryCard.visibility)
        assertEquals("Pricing and Rewards", bookingDetailsWidget.priceSummaryCard.heading.text)
        assertEquals(View.GONE, bookingDetailsWidget.newPriceSummaryCard.visibility)

        bookingDetailsWidget.checkIfWriteJsonEnabled = true
        bookingDetailsWidget.checkIfReadJsonEnabled = true
        bookingDetailsWidget.setUpWidget(itinCardDataHotel)
        assertEquals(View.VISIBLE, bookingDetailsWidget.newPriceSummaryCard.visibility)
        assertEquals("Pricing and Rewards", bookingDetailsWidget.newPriceSummaryCard.heading.text)
        assertEquals("₹3,500.00 total due at hotel", bookingDetailsWidget.newPriceSummaryCard.subheading.text)
        assertEquals(View.GONE, bookingDetailsWidget.priceSummaryCard.visibility)
    }

    @Test
    fun testPriceSummaryButtonNoItin() {
        bookingDetailsWidget.readJsonUtil = FaultyReadJsonUtil

        bookingDetailsWidget.checkIfWriteJsonEnabled = true
        bookingDetailsWidget.checkIfReadJsonEnabled = true
        bookingDetailsWidget.setUpWidget(itinCardDataHotel)
        assertEquals(View.GONE, bookingDetailsWidget.priceSummaryCard.visibility)
        assertEquals(View.GONE, bookingDetailsWidget.newPriceSummaryCard.visibility)
    }

    object MockReadJsonUtil : IJsonToItinUtil {
        override fun getItin(context: Context, itinId: String?): Itin? {
            return mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_for_mocker.json")?.itin!!
        }
    }

    object FaultyReadJsonUtil : IJsonToItinUtil {
        override fun getItin(context: Context, itinId: String?): Itin? {
            return null
        }
    }
}
