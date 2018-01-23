package com.expedia.bookings.itin.widget

import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertLinkTracked
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.itin.activity.HotelItinManageBookingActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
    private lateinit var intentBuilder: WebViewActivity.IntentBuilder
    lateinit var shadowActivity: ShadowActivity
    private lateinit var itinCardDataHotel: ItinCardDataHotel

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

        val userStateManager = Ui.getApplication(RuntimeEnvironment.application).appComponent().userStateManager()
        val user = userStateManager.userSource.user

        val phoneNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(user)
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

        val shadowPackageManager = Shadows.shadowOf(RuntimeEnvironment.application.packageManager)
        shadowPackageManager.setSystemFeature(PackageManager.FEATURE_TELEPHONY, true)

        customerSupportWidget.callSupportActionButton.performClick()
        assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.Call.Expedia", mockAnalyticsProvider)
    }

    @Test
    fun clickCallSupportIsTrackedInOmnitureWhenNoPhoneIsAvailable() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        val shadowPackageManager = Shadows.shadowOf(RuntimeEnvironment.application.packageManager)
        shadowPackageManager.setSystemFeature(PackageManager.FEATURE_TELEPHONY, false)

        customerSupportWidget.callSupportActionButton.performClick()
        assertEquals(customerSupportWidget.callSupportActionButton.text, ClipboardUtils.getText(activity))
        assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.Call.Expedia", mockAnalyticsProvider)
    }

    @Test
    fun clickSupportWebsiteIsTrackedInOmniture() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        customerSupportWidget.customerSupportSiteButton.performClick()
        assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Manage.CSP", mockAnalyticsProvider)
    }
}
