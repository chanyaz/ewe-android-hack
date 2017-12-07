package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Rule
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.itin.data.FlightItinLegsDetailData
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

import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinManageBookingViewModelTest {
    lateinit private var context: Context
    lateinit private var activity: Activity
    lateinit private var sut: FlightItinManageBookingViewModel

    val itinCardDataValidSubscriber = TestSubscriber<Unit>()
    val itinCardDataSubscriber = TestSubscriber<ItinCardDataFlight>()
    val updateToolbarSubscriber = TestSubscriber<ItinToolbarViewModel.ToolbarParams>()
    val customerSupportDetailSubscriber = TestSubscriber<ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams>()

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
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.setUp()

        val title = context.getString(R.string.itin_flight_manage_booking_header)
        val destinationCity = Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE).
                put("destination", "Las Vegas").format().toString()

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
        val flightLegDetailWidgetSubject = TestSubscriber<ArrayList<FlightItinLegsDetailData>>()
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
        leg.legArrivaltime.localizedMediumDate = "Dec 13"
        leg.legArrivaltime.localizedShortTime = "12:19pm"
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
    fun testRulesAndRegualationText(){
        val flightLegDetailRulesAndRegulationSubject = TestSubscriber<String>()
        sut.flightLegDetailRulesAndRegulationSubject.subscribe(flightLegDetailRulesAndRegulationSubject)
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val flightTrip = (testItinCardData.tripComponent as TripFlight).flightTrip

        val cancelChange = "We understand that sometimes plans change. We do not charge a cancel or change fee. When the airline charges such fees in accordance with its own policies, the cost will be passed on to you."
        val refundability = "Tickets are nonrefundable, nontransferable and name changes are not allowed."
        val penaltyRules = "Please read the <a href=\"https://wwwexpediacom.trunk-stubbed.sb.karmalab.net/Fare-Rules?tripid=028c321c-fbb7-4a83-95ae-e6a7d9924474\">complete penalty rules for changes and cancellations<span class=\"icon icon-popup tooltip\" aria-hidden=\"true\"> </span><span class=\"visually-hidden alt\">Opens in a new window.</span></a> applicable to this fare."
        val liabilityRules = "Please read important information regarding href=\"https://wwwexpediacom.trunk-stubbed.sb.karmalab.net/p/info-main/warsaw?\">airline liability limitations<span class=\"icon icon-popup tooltip\" aria-hidden=\"true\"> </span><span class=\"visually-hidden alt\">Opens in a new window.</span></a>."
        flightTrip.addRule(getRule("cancelChangeIntroductionText",cancelChange,""))
        flightTrip.addRule(getRule("refundabilityText",refundability,""))
        flightTrip.addRule(getRule("completePenaltyRules","",penaltyRules))
        flightTrip.addRule(getRule("airlineLiabilityLimitations","",liabilityRules))

        val penaltyRulesAssert = "Please read the <a href=\"https://wwwexpediacom.trunk-stubbed.sb.karmalab.net/Fare-Rules?tripid=028c321c-fbb7-4a83-95ae-e6a7d9924474\">complete penalty rules for changes and cancellations</a> applicable to this fare."
        val liabilityRulesAssert = "Please read important information regarding href=\"https://wwwexpediacom.trunk-stubbed.sb.karmalab.net/p/info-main/warsaw?\">airline liability limitations</a>."


        val assertValue = cancelChange+"<br><br>"+"<b>"+refundability+"</b>"+"<br><br>"+penaltyRulesAssert+"<br><br>"+liabilityRulesAssert

        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.setUp()

        flightLegDetailRulesAndRegulationSubject.assertValue(assertValue)
    }

    @Test
    fun testIsSplitTicket() {
        val flightSplitTicketVisibilitySubject = TestSubscriber<Boolean>()
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
        val flightSplitTicketVisibilitySubject = TestSubscriber<Boolean>()
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

    class TestWayPoint(val city: String) : Waypoint(ACTION_UNKNOWN) {
        override fun getAirport(): Airport? {
            val airport = Airport()
            if (city.isEmpty()) {
                return null
            } else {
                airport.mCity = city
                return airport
            }
        }
    }

    private fun getRule(key:String, text: String, textAndURL :String): Rule {
        val rule = Rule()
        rule.name = key
        rule.text = text
        rule.textAndURL = textAndURL
        return rule
    }

}