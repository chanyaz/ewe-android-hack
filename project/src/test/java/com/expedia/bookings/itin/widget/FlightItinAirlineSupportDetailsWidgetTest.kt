package com.expedia.bookings.itin.widget

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinAirlineSupportDetailsViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.ClipboardUtils
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinAirlineSupportDetailsWidgetTest {
    lateinit var activity: Activity
    lateinit var supportDetailsWidget: FlightItinAirlineSupportDetailsWidget

    @Before
    fun before() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().start().get()
        activity.setTheme(R.style.ItinTheme)
        supportDetailsWidget = LayoutInflater.from(activity).inflate(R.layout.test_flight_itin_airline_support_details_widget, null) as FlightItinAirlineSupportDetailsWidget
        supportDetailsWidget.viewModel = FlightItinAirlineSupportDetailsViewModel()
    }

    @Test
    fun testViewVisibility() {

        val title = Phrase.from(activity, R.string.itin_flight_airline_support_widget_airlines_for_help_TEMPLATE).put("airline_name", "UNITED").format().toString()
        val airlineSupport = Phrase.from(activity, R.string.itin_flight_airline_support_widget_airlines_support_TEMPLATE).put("airline_name", "UNITED").format().toString()
        val customerSupportSitetext = Phrase.from(activity, R.string.itin_flight_airline_support_widget_customer_support_TEMPLATE).put("airline_name", "UNITED").format().toString()

        supportDetailsWidget.viewModel.airlineSupportDetailsWidgetSubject.onNext(FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams(title, airlineSupport, "", "", "", "", customerSupportSitetext, ""))

        assertEquals(supportDetailsWidget.title.visibility, View.VISIBLE)
        assertEquals(supportDetailsWidget.airlineSupport.visibility, View.VISIBLE)
        assertEquals(supportDetailsWidget.ticket.visibility, View.GONE)
        assertEquals(supportDetailsWidget.confirmation.visibility, View.GONE)
        assertEquals(supportDetailsWidget.itinerary.visibility, View.GONE)
        assertEquals(supportDetailsWidget.customerSupportCallButton.visibility, View.GONE)
        assertEquals(supportDetailsWidget.customerSupportSiteButton.visibility, View.GONE)

        supportDetailsWidget.viewModel.airlineSupportDetailsWidgetSubject.onNext(createAirlineSupportDetailsParam())

        assertEquals(supportDetailsWidget.title.visibility, View.VISIBLE)
        assertEquals(supportDetailsWidget.airlineSupport.visibility, View.VISIBLE)
        assertEquals(supportDetailsWidget.ticket.visibility, View.VISIBLE)
        assertEquals(supportDetailsWidget.confirmation.visibility, View.VISIBLE)
        assertEquals(supportDetailsWidget.itinerary.visibility, View.VISIBLE)
        assertEquals(supportDetailsWidget.customerSupportCallButton.visibility, View.VISIBLE)
        assertEquals(supportDetailsWidget.customerSupportSiteButton.visibility, View.VISIBLE)
    }

    @Test
    fun testOmnitureForCallSupport() {
        supportDetailsWidget.viewModel.airlineSupportDetailsWidgetSubject.onNext(createAirlineSupportDetailsParam())
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        supportDetailsWidget.customerSupportCallButton.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Manage.Call.Airline", mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureForWebSupport() {
        supportDetailsWidget.viewModel.airlineSupportDetailsWidgetSubject.onNext(createAirlineSupportDetailsParam())
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        supportDetailsWidget.customerSupportSiteButton.performClick()
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Manage.Support.Airline", mockAnalyticsProvider)
    }

    @Test
    fun testGetContentDescriptionForView() {
        var ticketValue = "0167939252191"
        var confirmationValue = "IKQVCR"
        var itineraryNumber = "7238007847306"

        supportDetailsWidget.viewModel.airlineSupportDetailsWidgetSubject.onNext(FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams("", "", ticketValue, confirmationValue, itineraryNumber, "", "", ""))

        assertEquals(supportDetailsWidget.ticket.contentDescription, "Ticket number 0 1 6 7 9 3 9 2 5 2 1 9 1")
        assertEquals(supportDetailsWidget.confirmation.contentDescription, "Confirmation number I K Q V C R")
        assertEquals(supportDetailsWidget.itinerary.contentDescription, "Itinerary number 7 2 3 8 0 0 7 8 4 7 3 0 6")

        ticketValue = "0167939252191 , 094572375845"
        confirmationValue = "IKQVCR , KH67JH"
        itineraryNumber = "7238007847306"

        supportDetailsWidget.viewModel.airlineSupportDetailsWidgetSubject.onNext(FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams("", "", ticketValue, confirmationValue, itineraryNumber, "", "", ""))

        assertEquals(supportDetailsWidget.ticket.contentDescription, "Ticket number 0 1 6 7 9 3 9 2 5 2 1 9 1 , 0 9 4 5 7 2 3 7 5 8 4 5")
        assertEquals(supportDetailsWidget.confirmation.contentDescription, "Confirmation number I K Q V C R , K H 6 7 J H")
        assertEquals(supportDetailsWidget.itinerary.contentDescription, "Itinerary number 7 2 3 8 0 0 7 8 4 7 3 0 6")
    }

    @Test
    fun testItineraryAndConfirmationClick() {
        var ticketValue = "0167939252191"
        var confirmationValue = "IKQVCR"
        var itineraryNumber = "7238007847306"

        supportDetailsWidget.viewModel.airlineSupportDetailsWidgetSubject.onNext(FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams("", "", ticketValue, confirmationValue, itineraryNumber, "", "", ""))
        supportDetailsWidget.confirmation.performClick()
        assertEquals(ClipboardUtils.getText(activity), confirmationValue)
        supportDetailsWidget.itinerary.performClick()
        assertEquals(ClipboardUtils.getText(activity), itineraryNumber)
    }

    @Test
    fun testTicketClick() {
        var ticketValue = "0167939252191"
        var confirmationValue = "IKQVCR"
        var itineraryNumber = "7238007847306"

        supportDetailsWidget.viewModel.airlineSupportDetailsWidgetSubject.onNext(FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams("", "", ticketValue, confirmationValue, itineraryNumber, "", "", ""))
        supportDetailsWidget.ticket.performClick()
        assertEquals(ClipboardUtils.getText(activity), ticketValue)
    }

    private fun createAirlineSupportDetailsParam(): FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams {
        val airlineName = "UNITED"
        val ticketValue = "0167939252191"
        val confirmationValue = "IKQVCR"
        val itineraryNumber = "7238007847306"
        val airlineSupportUrlValue = "https://www.expedia.com/trips/airline/manage?airlineCode=UA&firstName=Girija&lastName=Balachandran&confirmation=IKQVCR&departureAirport=SFO&flightNumber=681&email=gbalachandran%40expedia.com&ticketNumber=0167939252191&flightDay=5&flightMonth=9"

        val title = Phrase.from(activity, R.string.itin_flight_airline_support_widget_airlines_for_help_TEMPLATE).put("airline_name", airlineName).format().toString()
        val airlineSupport = Phrase.from(activity, R.string.itin_flight_airline_support_widget_airlines_support_TEMPLATE).put("airline_name", airlineName).format().toString()
        val ticket = Phrase.from(activity, R.string.itin_flight_airline_support_widget_ticket_TEMPLATE).put("ticket_number", ticketValue).format().toString()
        val confirmation = Phrase.from(activity, R.string.itin_flight_airline_support_widget_confirmation_TEMPLATE).put("confirmation_number", confirmationValue).format().toString()
        val itinerary = Phrase.from(activity, R.string.itin_flight_airline_support_widget_itinerary_TEMPLATE).put("itinerary_number", itineraryNumber).format().toString()
        val callSupportNumber = "(217)-787-8504"
        val customerSupportSitetext = Phrase.from(activity, R.string.itin_flight_airline_support_widget_customer_support_TEMPLATE).put("airline_name", airlineName).format().toString()
        return FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams(title, airlineSupport, ticket, confirmation, itinerary, callSupportNumber, customerSupportSitetext, airlineSupportUrlValue)
    }
}
