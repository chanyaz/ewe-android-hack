package com.expedia.bookings.itin.widget

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
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

    lateinit var bookingDetailsWidget: HotelItinBookingDetails
    lateinit private var intentBuilder: WebViewActivity.IntentBuilder
    lateinit private var itinCardDataHotel: ItinCardDataHotel
    lateinit var intent: Intent
    lateinit var shadowActivity: ShadowActivity

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        bookingDetailsWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_booking_details, null) as HotelItinBookingDetails
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
        bookingDetailsWidget.setUpWidget(itinCardDataHotel)
        intentBuilder = WebViewActivity.IntentBuilder(RuntimeEnvironment.application)
        shadowActivity = Shadows.shadowOf(activity)
    }

    @Test
    fun priceSummaryToolbarAndUrlAreCorrect() {
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
        bookingDetailsWidget.additionalInfoCard.performClick()
        intent = shadowActivity.nextStartedActivity
        assertEquals(WebViewActivity::class.java.name, intent.component.className)
        assertEquals("Additional information", intent.extras.getString("ARG_TITLE"))
        // we cannot check against the Visitor ID stuff that gets added to the URL, because it adds unique data every time it is called
        assertTrue(intent.extras.getString("ARG_URL").startsWith(itinCardDataHotel.detailsUrl))
    }

}