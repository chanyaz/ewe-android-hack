package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Rule
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.trips.FlightAction
import com.expedia.bookings.data.trips.FlightConfirmation
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.itin.data.FlightItinLegsDetailData
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.mobiata.flightlib.data.Airport
import com.mobiata.flightlib.data.Waypoint
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when` as whenever
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment

import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinManageBookingViewModelTest {
    private lateinit var context: Context
    private lateinit var activity: Activity
    private lateinit var sut: FlightItinManageBookingViewModel

    private val itinCardDataValidSubscriber = TestObserver<Unit>()
    private val itinCardDataSubscriber = TestObserver<ItinCardDataFlight>()
    private val updateToolbarSubscriber = TestObserver<ItinToolbarViewModel.ToolbarParams>()
    private val customerSupportDetailSubscriber = TestObserver<ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinManageBookingViewModel(activity, "TEST_ITIN_ID")
        context = RuntimeEnvironment.application
    }

    @Test
    fun testItinCardDataFlightForNotNull() {
        sut.itinCardDataNotValidSubject.subscribe(itinCardDataValidSubscriber)
        sut.itinCardDataFlightObservable.subscribe(itinCardDataSubscriber)

        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.updateItinCardDataFlight()

        itinCardDataValidSubscriber.assertNoValues()
        itinCardDataSubscriber.assertValueCount(1)
        itinCardDataSubscriber.assertValue(testItinCardData)
        assertEquals(testItinCardData, sut.itinCardDataFlight)
    }

    @Test
    fun testItinCardDataFlightNull() {
        sut.itinCardDataNotValidSubject.subscribe(itinCardDataValidSubscriber)
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(null)
        sut.itineraryManager = mockItinManager
        sut.updateItinCardDataFlight()

        itinCardDataValidSubscriber.assertValue(Unit)
        itinCardDataSubscriber.assertValueCount(0)
    }

    @Test
    fun testToolBar() {
        sut.updateToolbarSubject.subscribe(updateToolbarSubscriber)
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val segments = testItinCardData.flightLeg.segments
        testItinCardData.flightLeg.segments[segments.size - 1].destinationWaypoint = TestWayPoint("Las Vegas")
        testItinCardData.flightLeg.segments[segments.size - 1].originWaypoint = TestWayPoint("San Francisco")
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.setUp()

        val title = context.getString(R.string.itin_flight_manage_booking_header)
        val destinationCity = Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE)
                .put("destination", "Las Vegas").format().toString()

        updateToolbarSubscriber.assertValue(ItinToolbarViewModel.ToolbarParams(title, destinationCity, false))
    }

    @Test
    fun testCustomerSupportDetails() {
        sut.customerSupportDetailsSubject.subscribe(customerSupportDetailSubscriber)
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        testItinCardData.tripComponent.parentTrip.tripNumber = "123456789"
        testItinCardData.tripComponent.parentTrip.customerSupport.supportPhoneNumberDomestic = "+1-866-230-3837"
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.setUp()

        val header = Phrase.from(context, R.string.itin_flight_customer_support_header_text_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        val itineraryNumb = Phrase.from(context, R.string.itin_flight_itinerary_number_TEMPLATE).put("itin_number", "123456789").format().toString()
        val customerSupportNumber = "+1-866-230-3837"
        val customerSupportButton = Phrase.from(context, R.string.itin_flight_customer_support_site_header_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        val customerSupportURL = "http://www.expedia.com/service/"
        customerSupportDetailSubscriber.assertValue(ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams(header, itineraryNumb, customerSupportNumber, customerSupportButton, customerSupportURL))
    }

    @Test
    fun createOmnitureValues() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        sut.itinCardDataFlight = testItinCardData
        val startDate = DateTime.now().plusDays(30)
        val formattedStartDate = JodaUtils.format(startDate, "yyyy-MM-dd")
        val endDate = JodaUtils.format(startDate.plusDays(7), "yyyy-MM-dd")
        val expectedValues = HashMap<String, String?>()
        expectedValues.put("duration", "8")
        expectedValues.put("productString", ";Flight:UA:OW;;")
        expectedValues.put("tripStartDate", formattedStartDate)
        expectedValues.put("daysUntilTrip", "30")
        expectedValues.put("tripEndDate", endDate)
        expectedValues.put("orderAndTripNumbers", "8063550177859|7238007847306")

        val values = sut.createOmnitureTrackingValues()

        assertEquals(expectedValues, values)
    }

    @Test
    fun testCreateFlightLegDetailWidgetData() {
        val flightLegDetailWidgetSubject = TestObserver<ArrayList<FlightItinLegsDetailData>>()
        sut.flightLegDetailWidgetLegDataSubject.subscribe(flightLegDetailWidgetSubject)
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val legs = (testItinCardData.tripComponent as TripFlight).flightTrip.legs
        val leg = legs[legs.size - 1]

        val imgPath = "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smVX.gif"
        leg.airlineLogoURL = imgPath
        leg.firstWaypoint.mAirportCode = "SFO"
        leg.lastWaypoint.mAirportCode = "SEA"
        leg.legDepartureTime.localizedMediumDate = "Dec 13"
        leg.legDepartureTime.localizedShortTime = "11:39pm"
        leg.legArrivalTime.localizedMediumDate = "Dec 13"
        leg.legArrivalTime.localizedShortTime = "12:19pm"
        leg.numberOfStops = "1"

        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.setUp()

        val list = ArrayList<FlightItinLegsDetailData>()
        val flightItinLegsDetailData = FlightItinLegsDetailData(imgPath, "SFO", "SEA", "Dec 13", "11:39pm", "Dec 13", "12:19pm", "1")
        list.add(flightItinLegsDetailData)
        flightLegDetailWidgetSubject.assertValue(list)
    }

    @Test
    fun testRulesAndRegualationText() {
        val flightLegDetailRulesAndRegulationSubject = TestObserver<String>()
        sut.flightLegDetailRulesAndRegulationSubject.subscribe(flightLegDetailRulesAndRegulationSubject)
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val flightTrip = (testItinCardData.tripComponent as TripFlight).flightTrip

        val cancelChange = "We understand that sometimes plans change. We do not charge a cancel or change fee. When the airline charges such fees in accordance with its own policies, the cost will be passed on to you."
        val refundability = "Tickets are nonrefundable, nontransferable and name changes are not allowed."
        val penaltyRules = "Please read the <a href=\"https://wwwexpediacom.trunk-stubbed.sb.karmalab.net/Fare-Rules?tripid=028c321c-fbb7-4a83-95ae-e6a7d9924474\">complete penalty rules for changes and cancellations<span class=\"icon icon-popup tooltip\" aria-hidden=\"true\"> </span><span class=\"visually-hidden alt\">Opens in a new window.</span></a> applicable to this fare."
        val liabilityRules = "Please read important information regarding href=\"https://wwwexpediacom.trunk-stubbed.sb.karmalab.net/p/info-main/warsaw?\">airline liability limitations<span class=\"icon icon-popup tooltip\" aria-hidden=\"true\"> </span><span class=\"visually-hidden alt\">Opens in a new window.</span></a>."
        flightTrip.addRule(getRule("cancelChangeIntroductionText", cancelChange, ""))
        flightTrip.addRule(getRule("refundabilityText", refundability, ""))
        flightTrip.addRule(getRule("completePenaltyRules", "", penaltyRules))
        flightTrip.addRule(getRule("airlineLiabilityLimitations", "", liabilityRules))

        val penaltyRulesAssert = "Please read the <a href=\"https://wwwexpediacom.trunk-stubbed.sb.karmalab.net/Fare-Rules?tripid=028c321c-fbb7-4a83-95ae-e6a7d9924474\">complete penalty rules for changes and cancellations</a> applicable to this fare."
        val liabilityRulesAssert = "Please read important information regarding href=\"https://wwwexpediacom.trunk-stubbed.sb.karmalab.net/p/info-main/warsaw?\">airline liability limitations</a>."

        val assertValue = "$cancelChange<br><br><b>$refundability</b><br><br>$penaltyRulesAssert<br><br>$liabilityRulesAssert"

        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.setUp()

        flightLegDetailRulesAndRegulationSubject.assertValue(assertValue)
    }

    @Test
    fun testIsSplitTicket() {
        val flightSplitTicketVisibilitySubject = TestObserver<Boolean>()
        sut.flightSplitTicketVisibilitySubject.subscribe(flightSplitTicketVisibilitySubject)

        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val flightTrip = (testItinCardData.tripComponent as TripFlight).flightTrip
        flightTrip.isSplitTicket = true
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.setUp()

        flightSplitTicketVisibilitySubject.assertValue(true)
    }

    @Test
    fun testIsNotSplitTicket() {
        val flightSplitTicketVisibilitySubject = TestObserver<Boolean>()
        sut.flightSplitTicketVisibilitySubject.subscribe(flightSplitTicketVisibilitySubject)

        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val flightTrip = (testItinCardData.tripComponent as TripFlight).flightTrip
        flightTrip.isSplitTicket = false
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.setUp()

        flightSplitTicketVisibilitySubject.assertValue(false)
    }

    @Test
    fun testAirlineSupportWidgetWithValues() {
        val flightItinAirlineSupportDetailsSubject = TestObserver<FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams>()
        sut.flightItinAirlineSupportDetailsSubject.subscribe(flightItinAirlineSupportDetailsSubject)

        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val tripFlight = testItinCardData.tripComponent as TripFlight
        val airlineName = "UNITED"
        val ticketValue = "0167939252191"
        val confirmationValue = "IKQVCR"
        val itineraryNumber = "7238007847306"
        val airlineSupportUrlValue = "https://www.expedia.com/trips/airline/manage?airlineCode=UA&firstName=Girija&lastName=Balachandran&confirmation=IKQVCR&departureAirport=SFO&flightNumber=681&email=gbalachandran%40expedia.com&ticketNumber=0167939252191&flightDay=5&flightMonth=9"

        tripFlight.travelers.clear()
        val traveler = Traveler()
        val ticketNumberList = ArrayList<String>()
        ticketNumberList.add(ticketValue)
        traveler.ticketNumbers = ticketNumberList
        tripFlight.travelers.add(traveler)

        tripFlight.flightTrip.airlineManageBookingURL = airlineSupportUrlValue
        tripFlight.parentTrip.tripNumber = itineraryNumber

        tripFlight.confirmations.clear()
        val flightConfirmation = FlightConfirmation()
        flightConfirmation.confirmationCode = confirmationValue
        flightConfirmation.carrier = airlineName
        tripFlight.confirmations.add(flightConfirmation)

        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.updateItinCardDataFlight()

        sut.airlineSupportDetailsData()

        val title = Phrase.from(context, R.string.itin_flight_airline_support_widget_airlines_for_help_TEMPLATE).put("airline_name", airlineName).format().toString()
        val airlineSupport = Phrase.from(context, R.string.itin_flight_airline_support_widget_airlines_support_TEMPLATE).put("airline_name", airlineName).format().toString()
        val ticket = Phrase.from(context, R.string.itin_flight_airline_support_widget_ticket_TEMPLATE).put("ticket_number", ticketValue).format().toString()
        val confirmation = Phrase.from(context, R.string.itin_flight_airline_support_widget_confirmation_TEMPLATE).put("confirmation_number", confirmationValue).format().toString()
        val itinerary = Phrase.from(context, R.string.itin_flight_airline_support_widget_itinerary_TEMPLATE).put("itinerary_number", itineraryNumber).format().toString()
        val callSupportNumber = ""
        val customerSupportSitetext = Phrase.from(context, R.string.itin_flight_airline_support_widget_customer_support_TEMPLATE).put("airline_name", airlineName).format().toString()
        flightItinAirlineSupportDetailsSubject.assertValue(FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams(title, airlineSupport, ticket, confirmation, itinerary, callSupportNumber, customerSupportSitetext, airlineSupportUrlValue))
    }

    @Test
    fun testAirlineSupportWidgetWithNoValues() {
        val flightItinAirlineSupportDetailsSubject = TestObserver<FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams>()
        sut.flightItinAirlineSupportDetailsSubject.subscribe(flightItinAirlineSupportDetailsSubject)

        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val tripFlight = testItinCardData.tripComponent as TripFlight

        tripFlight.travelers.clear()
        tripFlight.flightTrip.airlineManageBookingURL = null
        tripFlight.parentTrip.tripNumber = null
        tripFlight.confirmations.clear()
        tripFlight.flightTrip.legs.clear()

        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.updateItinCardDataFlight()
        sut.airlineSupportDetailsData()

        val title = Phrase.from(context, R.string.itin_flight_airline_support_widget_airlines_for_help_TEMPLATE).put("airline_name", context.getString(R.string.itin_flight_airline_support_widget_the_airline_text)).format().toString()
        val airlineSupport = Phrase.from(context, R.string.itin_flight_airline_support_widget_airlines_support_TEMPLATE).put("airline_name", context.getString(R.string.itin_flight_airline_support_widget_airline_text)).format().toString()
        val ticket = ""
        val confirmation = ""
        val itinerary = ""
        val callSupportNumber = ""
        val airlineSupportUrlValue = ""
        val customerSupportSitetext = Phrase.from(context, R.string.itin_flight_airline_support_widget_customer_support_TEMPLATE).put("airline_name", context.getString(R.string.itin_flight_airline_support_widget_airline_text)).format().toString()
        flightItinAirlineSupportDetailsSubject.assertValue(FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams(title, airlineSupport, ticket, confirmation, itinerary, callSupportNumber, customerSupportSitetext, airlineSupportUrlValue))
    }

    @Test
    fun testModifyReservationWithData() {
        val flightItinModifyReservationSubject = TestObserver<FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams>()
        sut.flightItinModifyReservationSubject.subscribe(flightItinModifyReservationSubject)

        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val flightTrip = (testItinCardData.tripComponent as TripFlight).flightTrip
        testItinCardData.tripComponent.parentTrip.customerSupport.supportPhoneNumberDomestic = "+1-866-230-3837"
        val action = FlightAction()
        action.isCancellable = true
        action.isChangeable = true
        flightTrip.action = action
        flightTrip.webCancelPathURL = "https://www.expedia.com/flight-cancel-exchange?itinnumber=1157495899343&arl=304697418&bookingid=ZBZTFR"
        flightTrip.webChangePathURL = "https://www.expedia.com/flight-change-gds?itinnumber=1157495899343&arl=304697418&cname=United"

        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.setUp()

        flightItinModifyReservationSubject.assertValue(FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams(flightTrip.webChangePathURL, true, flightTrip.webCancelPathURL, true, "+1-866-230-3837"))
    }

    @Test
    fun testModifyReservationWithoutData() {
        val flightItinModifyReservationSubject = TestObserver<FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams>()
        sut.flightItinModifyReservationSubject.subscribe(flightItinModifyReservationSubject)

        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val flightTrip = (testItinCardData.tripComponent as TripFlight).flightTrip
        testItinCardData.tripComponent.parentTrip.customerSupport.supportPhoneNumberDomestic = ""
        flightTrip.action = null

        flightTrip.webCancelPathURL = null
        flightTrip.webChangePathURL = null

        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.setUp()

        flightItinModifyReservationSubject.assertValue(FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams("", false, "", false, ""))
    }

    class TestWayPoint(val city: String) : Waypoint(ACTION_UNKNOWN) {
        override fun getAirport(): Airport? {
            val airport = Airport()
            return when {
                city.isEmpty() -> null
                else -> {
                    airport.mCity = city
                    airport
                }
            }
        }
    }

    private fun getRule(key: String, text: String, textAndURL: String): Rule {
        val rule = Rule()
        rule.name = key
        rule.text = text
        rule.textAndURL = textAndURL
        return rule
    }
}
