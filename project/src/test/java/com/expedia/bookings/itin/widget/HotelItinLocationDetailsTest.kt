package com.expedia.bookings.itin.widget

import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinLocationDetailsTest {

    lateinit var locationDetailsWidget: HotelItinLocationDetails
    lateinit private var activity: HotelItinDetailsActivity
    var itinCardDataHotel = ItinCardDataHotelBuilder().build()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinDetailsActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        locationDetailsWidget = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_location_details, null) as HotelItinLocationDetails
    }

    @Test
    fun testMapWidgetMessagingFeatureOff() {
        SettingUtils.save(activity, R.string.preference_enable_trips_hotel_messaging, false)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppTripsMessageHotel, 0)
        locationDetailsWidget.setupWidget(itinCardDataHotel)
        assertEquals(View.VISIBLE, locationDetailsWidget.mapView.visibility)
        assertEquals(View.GONE, locationDetailsWidget.directionsButton.visibility)
        assertEquals(locationDetailsWidget.addressLine1.text, itinCardDataHotel.propertyLocation.streetAddressString)
        assertEquals(locationDetailsWidget.addressLine2.text, itinCardDataHotel.propertyLocation.toTwoLineAddressFormattedString())
        assertEquals(locationDetailsWidget.actionButtons.getmLeftButton().text, itinCardDataHotel.localPhone)
        locationDetailsWidget.address.performClick()
        val address: String = Phrase.from(activity, R.string.itin_hotel_details_address_clipboard_TEMPLATE)
                .put("addresslineone", locationDetailsWidget.addressLine1.text.toString())
                .put("addresslinetwo", locationDetailsWidget.addressLine2.text.toString()).format().toString()
        assertEquals(address, ClipboardUtils.getText(activity))
    }

    @Test
    fun testMapWidgetMessagingFeatureOn() {
        SettingUtils.save(activity, R.string.preference_enable_trips_hotel_messaging, true)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppTripsMessageHotel, 1)
        locationDetailsWidget.setupWidget(itinCardDataHotel)
        assertEquals(View.VISIBLE, locationDetailsWidget.mapView.visibility)
        assertEquals(View.VISIBLE, locationDetailsWidget.directionsButton.visibility)
        assertEquals(locationDetailsWidget.addressLine1.text, itinCardDataHotel.propertyLocation.streetAddressString)
        assertEquals(locationDetailsWidget.addressLine2.text, itinCardDataHotel.propertyLocation.toTwoLineAddressFormattedString())
        assertEquals(View.GONE, locationDetailsWidget.actionButtons.visibility)
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
    fun testDirectionsOmnitureClickMessagingFeatureOff() {
        SettingUtils.save(activity, R.string.preference_enable_trips_hotel_messaging, false)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppTripsMessageHotel, 0)
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        locationDetailsWidget.setupWidget(itinCardDataHotel)
        val directionsButton = locationDetailsWidget.actionButtons.getmRightButton()
        directionsButton.performClick()

        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Directions", mockAnalyticsProvider)
    }

    @Test
    fun testDirectionsOmnitureClickMessagingFeatureOn() {
        SettingUtils.save(activity, R.string.preference_enable_trips_hotel_messaging, true)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppTripsMessageHotel, 1)
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        locationDetailsWidget.setupWidget(itinCardDataHotel)
        val directionsButton = locationDetailsWidget.directionsButton
        directionsButton.performClick()

        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Directions", mockAnalyticsProvider)
    }
}
