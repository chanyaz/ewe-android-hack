package com.expedia.bookings.itin.widget

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.itin.hotel.details.HotelItinBookingDetails
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.mobiata.mocke3.mockObject
import org.junit.After
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
    lateinit var context: Context

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        context = RuntimeEnvironment.application
        bookingDetailsWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_booking_details, null) as HotelItinBookingDetails
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
        intentBuilder = WebViewActivity.IntentBuilder(RuntimeEnvironment.application)
        shadowActivity = Shadows.shadowOf(activity)
    }

    @Test
    fun priceSummaryToolbarAndUrlAreCorrect() {
        bookingDetailsWidget.readJsonUtil = MockReadJsonUtil
        bookingDetailsWidget.setUpWidget(itinCardDataHotel)
        bookingDetailsWidget.newPriceSummaryCard.performClick()

        intent = shadowActivity.nextStartedActivity
        assertEquals(WebViewActivity::class.java.name, intent.component.className)
        assertEquals("Price summary", intent.extras.getString("ARG_TITLE"))
        // we cannot check against the Visitor ID stuff that gets added to the URL, because it adds unique data every time it is called
        assertTrue(intent.extras.getString("ARG_URL").startsWith("https://www.expedia.com/trips/7280999576135"))
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
        bookingDetailsWidget.setUpWidget(itinCardDataHotel)

        assertEquals(View.VISIBLE, bookingDetailsWidget.newPriceSummaryCard.visibility)
        assertEquals("Pricing and rewards", bookingDetailsWidget.newPriceSummaryCard.heading.text)
        assertEquals("₹3,500.00 total due at hotel", bookingDetailsWidget.newPriceSummaryCard.subheading.text)
    }

    @Test
    fun testPriceSummaryButtonNoItin() {
        bookingDetailsWidget.readJsonUtil = FaultyReadJsonUtil
        bookingDetailsWidget.setUpWidget(itinCardDataHotel)

        assertEquals(View.GONE, bookingDetailsWidget.newPriceSummaryCard.visibility)
    }

    object MockReadJsonUtil : IJsonToItinUtil {
        override fun getItin(itinId: String?): Itin? {
            return mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_for_mocker.json")?.itin!!
        }
        override fun getItinList(): List<Itin> {
            return emptyList()
        }
    }

    object FaultyReadJsonUtil : IJsonToItinUtil {
        override fun getItin(itinId: String?): Itin? {
            return null
        }
        override fun getItinList(): List<Itin> {
            return emptyList()
        }
    }

    @After
    fun tearDown() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppTripsHotelPricing)
    }
}
