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
    fun testMapWidget() {
        locationDetailsWidget.setupWidget(itinCardDataHotel)
        assertEquals(View.VISIBLE, locationDetailsWidget.mapView.visibility)
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
    fun testMapOmnitureClick() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        locationDetailsWidget.setupWidget(itinCardDataHotel)
        locationDetailsWidget.mapView.performClick()

        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Map", mockAnalyticsProvider)
    }

    @Test
    fun testDirectionsOmnitureClick() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        locationDetailsWidget.setupWidget(itinCardDataHotel)
        val directionsButton = locationDetailsWidget.actionButtons.getmRightButton()
        directionsButton.performClick()

        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Hotel.Directions", mockAnalyticsProvider)
    }
}