package com.expedia.bookings.itin.widget

import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.itin.activity.HotelItinManageBookingActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.squareup.phrase.Phrase
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.hamcrest.MockitoHamcrest
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowActivity
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinCustomerSupportDetailsTest {

    lateinit var customerSupportWidget: HotelItinCustomerSupportDetails
    lateinit var activity: HotelItinManageBookingActivity
    lateinit var intent: Intent
    lateinit private var intentBuilder: WebViewActivity.IntentBuilder
    lateinit var shadowActivity: ShadowActivity
    lateinit private var itinCardDataHotel: ItinCardDataHotel

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinManageBookingActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        customerSupportWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_customer_support_details, null) as HotelItinCustomerSupportDetails
        intentBuilder = WebViewActivity.IntentBuilder(RuntimeEnvironment.application)
        shadowActivity = Shadows.shadowOf(activity)
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
        customerSupportWidget.setUpWidget(itinCardDataHotel)
    }

    @Test
    fun testItinHotelCustomerSupportWidget() {
        val customerSupportHeaderText = Phrase.from(activity, R.string.itin_hotel_customer_support_header_text_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        assertEquals(customerSupportHeaderText, customerSupportWidget.customerSupportTextView.text)

        val itinNumber = Phrase.from(activity, R.string.itin_hotel_itinerary_number_TEMPLATE).put("itinnumber", itinCardDataHotel.tripNumber).format().toString()
        assertEquals(itinNumber, customerSupportWidget.itineraryNumberTextView.text)

        val phoneNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser())
        assertEquals(phoneNumber, customerSupportWidget.callSupportActionButton.text)

        val supportSite = Phrase.from(activity, R.string.itin_hotel_customer_support_site_header_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        assertEquals(supportSite, customerSupportWidget.customerSupportSiteButton.text)

        customerSupportWidget.customerSupportSiteButton.performClick()
        intent = shadowActivity.nextStartedActivity
        assertEquals(WebViewActivity::class.java.name, intent.component.className)
        assertEquals("Customer service", intent.extras.getString("ARG_TITLE"))
    }

    @Test
    fun clickCallSupportIsTrackedInOmnitureWhenPhoneIsAvailable() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        RuntimeEnvironment.getRobolectricPackageManager().setSystemFeature(PackageManager.FEATURE_TELEPHONY, true)

        customerSupportWidget.callSupportActionButton.performClick()
        assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.Call.Expedia", mockAnalyticsProvider)
    }

    @Test
    fun clickCallSupportIsNotTrackedInOmnitureWhenNoPhoneIsAvailable() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        RuntimeEnvironment.getRobolectricPackageManager().setSystemFeature(PackageManager.FEATURE_TELEPHONY, false)

        customerSupportWidget.callSupportActionButton.performClick()
        assertLinkNotTracked(mockAnalyticsProvider)
    }

    @Test
    fun clickSupportWebsiteIsTrackedInOmniture() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        customerSupportWidget.customerSupportSiteButton.performClick()
        assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.CSP", mockAnalyticsProvider)
    }

    private fun assertLinkTracked(linkName: String, rfrrId: String, mockAnalyticsProvider: AnalyticsProvider) {
        val expectedData = mapOf(
                "&&linkType" to "o",
                "&&linkName" to linkName,
                "&&v28" to rfrrId,
                "&&c16" to rfrrId
        )

        Mockito.verify(mockAnalyticsProvider).trackAction(Mockito.eq(linkName), mapThat(hasEntries(expectedData)))
    }

    private fun assertLinkNotTracked(mockAnalyticsProvider: AnalyticsProvider) {
        Mockito.verify(mockAnalyticsProvider, Mockito.never())
                .trackAction(Mockito.anyString(), Mockito.anyMapOf(String::class.java, Any::class.java))
    }

    private fun hasEntries(data: Map<String, Any>): Matcher<Map<String, Any>> =
            Matchers.allOf(data.map { Matchers.hasEntry(it.key, it.value) })

    private fun mapThat(matcher: Matcher<Map<String, Any>>): Map<String, Any> {
        MockitoHamcrest.argThat(matcher)
        return emptyMap()
    }
}