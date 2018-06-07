package com.expedia.bookings.widget

import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class FlightCabinClassWidgetTest {

    private var activity = RuntimeEnvironment.application
    private lateinit var sut: FlightCabinClassWidget
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        sut = LayoutInflater.from(activity).inflate(R.layout.flight_cabin_class_widget, null) as FlightCabinClassWidget
    }

    @Test
    fun testTrackFlightCabinClassViewDisplayedPackages() {
        sut.lob = LineOfBusiness.PACKAGES
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        sut.performClick()

        val evar18 = mapOf(18 to "App.Package.DS.SeatingClass")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(evar18), mockAnalyticsProvider)
    }

    @Test
    fun testTrackFlightCabinClassViewDisplayedFlights() {
        sut.lob = LineOfBusiness.FLIGHTS_V2
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        sut.performClick()

        val evar18 = mapOf(18 to "App.Flight.DS.SeatingClass")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(evar18), mockAnalyticsProvider)
    }

    @Test
    fun testTrackFlightCabinClassSelectPackages() {
        sut.lob = LineOfBusiness.PACKAGES
        sut.performClick()

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        sut.flightCabinClassView.businessClassRadioButton.isChecked = true
        sut.dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()

        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to "App.Package.DS.SeatingClass.BUSINESS")), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to "App.Package.DS.SeatingClass.BUSINESS")), mockAnalyticsProvider)
    }

    @Test
    fun testTrackFlightCabinClassSelectFlights() {
        sut.lob = LineOfBusiness.FLIGHTS_V2
        sut.performClick()

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        sut.flightCabinClassView.businessClassRadioButton.isChecked = true
        sut.dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()

        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to "App.Flight.DS.SeatingClass.BUSINESS")), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to "App.Flight.DS.SeatingClass.BUSINESS")), mockAnalyticsProvider)
    }
}
