package com.expedia.bookings.itin.widget

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.hotel.details.HotelItinLocationDetails
import com.expedia.bookings.itin.hotel.taxi.HotelItinTaxiActivity
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelItinLocationDetailsTest {

    lateinit var locationDetailsWidget: HotelItinLocationDetails
    lateinit var itinCardDataHotel: ItinCardDataHotel
    private lateinit var activity: Activity

    @Before
    fun before() {
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        locationDetailsWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_location_details, null) as HotelItinLocationDetails
    }

    @Test
    fun testMapWidget() {
        locationDetailsWidget.setupWidget(itinCardDataHotel)
        assertEquals(View.VISIBLE, locationDetailsWidget.mapView.visibility)
        assertEquals(View.VISIBLE, locationDetailsWidget.directionsButton.visibility)
        assertEquals(activity.getString(R.string.itin_action_directions), locationDetailsWidget.directionsButton.contentDescription)
        assertEquals(locationDetailsWidget.addressLine1.text, itinCardDataHotel.propertyLocation.streetAddressString)
        assertEquals(locationDetailsWidget.addressLine2.text, itinCardDataHotel.propertyLocation.toTwoLineAddressFormattedString())
        locationDetailsWidget.address.performClick()
        val address: String = Phrase.from(activity, R.string.itin_hotel_details_address_clipboard_TEMPLATE)
                .put("addresslineone", locationDetailsWidget.addressLine1.text.toString())
                .put("addresslinetwo", locationDetailsWidget.addressLine2.text.toString()).format().toString()
        assertEquals(address, ClipboardUtils.getText(activity))
    }

    @Test
    fun testMapOmnitureClick() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        locationDetailsWidget.setupWidget(itinCardDataHotel)
        locationDetailsWidget.mapView.performClick()

        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Map", mockAnalyticsProvider)
    }

    @Test
    fun testDirectionsIconOmnitureClick() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        locationDetailsWidget.setupWidget(itinCardDataHotel)
        val directionsButton = locationDetailsWidget.directionsButton
        directionsButton.performClick()

        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Directions", mockAnalyticsProvider)
    }

    @Test
    fun taxiSetupHappy() {
        val button = locationDetailsWidget.taxiButton
        val tracker = MockTripsTracking()
        val shadow = Shadows.shadowOf(activity)
        locationDetailsWidget.tripTracking = tracker
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppHotelTripTaxiCard)
        locationDetailsWidget.gsonUtil = GoodLocaleJsonToItinUtil()
        val buttonExpectedText = "Translate to English"
        assertEquals(View.GONE, locationDetailsWidget.taxiContainer.visibility)
        locationDetailsWidget.taxiSetup("test123")

        assertEquals(View.VISIBLE, locationDetailsWidget.taxiContainer.visibility)
        assertEquals(buttonExpectedText, button.text.toString())
        assertEquals("$buttonExpectedText Button", button.contentDescription.toString())
        assertFalse(tracker.trackHotelTaxiClick)
        assertNull(shadow.peekNextStartedActivity())

        button.performClick()
        val intent = shadow.peekNextStartedActivity()

        assertTrue(tracker.trackHotelTaxiClick)
        assertEquals(HotelItinTaxiActivity::class.java.name, intent.component.className)
        assertEquals("test123", intent.extras.getString("ITINID", ""))
    }

    @Test
    fun taxiSetupNullLocalizationLanguage() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppHotelTripTaxiCard)
        locationDetailsWidget.gsonUtil = NullLocaleJsonToItinUtil()
        assertEquals(View.GONE, locationDetailsWidget.taxiContainer.visibility)
        locationDetailsWidget.taxiSetup("test123")
        assertEquals(View.GONE, locationDetailsWidget.taxiContainer.visibility)
    }

    @Test
    fun taxiSetupNullId() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppHotelTripTaxiCard)
        locationDetailsWidget.gsonUtil = GoodLocaleJsonToItinUtil()
        assertEquals(View.GONE, locationDetailsWidget.taxiContainer.visibility)
        locationDetailsWidget.taxiSetup(null)
        assertEquals(View.GONE, locationDetailsWidget.taxiContainer.visibility)
    }

    @Test
    fun taxiSetupUnBucketed() {
        locationDetailsWidget.gsonUtil = GoodLocaleJsonToItinUtil()
        assertEquals(View.GONE, locationDetailsWidget.taxiContainer.visibility)
        locationDetailsWidget.taxiSetup(null)
        assertEquals(View.GONE, locationDetailsWidget.taxiContainer.visibility)
    }

    private class NullLocaleJsonToItinUtil : IJsonToItinUtil {
        override fun getItin(itinId: String?): Itin? {
            return ItinMocker.hotelDetailsNoPriceDetails
        }
        override fun getItinList(): List<Itin> {
            return emptyList()
        }
    }

    private class GoodLocaleJsonToItinUtil : IJsonToItinUtil {
        override fun getItin(itinId: String?): Itin? {
            return ItinMocker.hotelDetailsHappy
        }
        override fun getItinList(): List<Itin> {
            return emptyList()
        }
    }
}
