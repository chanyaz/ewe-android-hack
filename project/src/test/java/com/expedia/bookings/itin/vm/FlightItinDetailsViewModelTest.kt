package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TicketingStatus
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.mobiata.flightlib.data.Airport
import com.mobiata.flightlib.data.Seat
import com.mobiata.flightlib.data.Waypoint
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals
import org.mockito.Mockito.`when` as whenever

@RunWith(RobolectricRunner::class)
class FlightItinDetailsViewModelTest {
    lateinit private var activity: Activity
    lateinit private var sut: FlightItinDetailsViewModel
    lateinit private var context: Context
    lateinit private var dateTime: DateTime
    val itinCardDataValidSubscriber = TestObserver<Unit>()
    val updateToolbarSubscriber = TestObserver<ItinToolbarViewModel.ToolbarParams>()
    val clearLegSummaryContainerSubscriber = TestObserver<Unit>()
    val createLegSummaryWidgetsSubscriber = TestObserver<FlightItinSegmentSummaryViewModel.SummaryWidgetParams>()
    val updateConfirmationSubscriber = TestObserver<ItinConfirmationViewModel.WidgetParams>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinDetailsViewModel(activity, "TEST_ITIN_ID")
        context = RuntimeEnvironment.application
        dateTime = DateTime.now()
    }

    @Test
    fun testUpdateItinCardDataFlightNull() {
        sut.itinCardDataNotValidSubject.subscribe(itinCardDataValidSubscriber)

        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(null)
        sut.itineraryManager = mockItinManager
        sut.updateItinCardDataFlight()
        itinCardDataValidSubscriber.assertValue(Unit)
    }

    @Test
    fun testUpdateItinCardDataFlightNotNull() {
        sut.itinCardDataNotValidSubject.subscribe(itinCardDataValidSubscriber)

        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.updateItinCardDataFlight()
        itinCardDataValidSubscriber.assertNoValues()
        assertEquals(testItinCardData, sut.itinCardDataFlight)
    }

    @Test
    fun testOnResume() {
        sut.updateToolbarSubject.subscribe(updateToolbarSubscriber)
        sut.updateConfirmationSubject.subscribe(updateConfirmationSubscriber)
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val now = DateTime.now()
        val startTime = now.plusDays(30)
        val startDate = LocaleBasedDateFormatUtils.dateTimeToMMMd(startTime).capitalize()
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.onResume()
        updateToolbarSubscriber.assertValue(ItinToolbarViewModel.ToolbarParams("Las Vegas", startDate, true))
    }

    @Test
    fun testUpdateConfirmation() {
        sut.updateConfirmationSubject.subscribe(updateConfirmationSubscriber)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        sut.itinCardDataFlight = testItinCardData
        sut.updateConfirmationWidget()
        updateConfirmationSubscriber.assertValueCount(1)
        val charSeq = updateConfirmationSubscriber.values()[0].confirmationNumbers
        updateConfirmationSubscriber.assertValue(ItinConfirmationViewModel.WidgetParams(TicketingStatus.COMPLETE, charSeq))
        assertEquals<CharSequence>(charSeq.toString(), "IKQVCR")
    }

    @Test
    fun testUpdateLegSummaryWidget() {
        sut.clearLegSummaryContainerSubject.subscribe(clearLegSummaryContainerSubscriber)
        sut.createSegmentSummaryWidgetsSubject.subscribe(createLegSummaryWidgetsSubscriber)

        val testItinCardData = ItinCardDataFlightBuilder().build()
        testItinCardData.flightLeg.segments[0].originWaypoint = TestWayPoint("SFO", "San Francisco", dateTime)
        testItinCardData.flightLeg.segments[0].originWaypoint.gate = "52A"
        testItinCardData.flightLeg.segments[0].destinationWaypoint = TestWayPoint("LAS", "Las Vegas", dateTime)
        sut.itinCardDataFlight = testItinCardData
        sut.updateLegSummaryWidget()
        clearLegSummaryContainerSubscriber.assertValueCount(1)
        clearLegSummaryContainerSubscriber.assertValue(Unit)
        createLegSummaryWidgetsSubscriber.assertValueCount(1)
        createLegSummaryWidgetsSubscriber.assertValue(FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 681",
                "COMPASS AIRLINES",
                dateTime,
                dateTime,
                "SFO",
                "San Francisco",
                "LAS",
                "Las Vegas",
                null,
                "52A",
                "3",
                null,
                "22F",
                " • Economy / Coach",
                "Confirm or change seats with airline"

        ))
    }
    @Test
    fun testUpdateLegSummaryNoSeatsAndMapWidget() {
        sut.clearLegSummaryContainerSubject.subscribe(clearLegSummaryContainerSubscriber)
        sut.createSegmentSummaryWidgetsSubject.subscribe(createLegSummaryWidgetsSubscriber)

        val testItinCardData = ItinCardDataFlightBuilder().build()
        val dateTime = DateTime.now()
        testItinCardData.flightLeg.segments[0].originWaypoint = TestWayPoint("SFO", "San Francisco", dateTime)
        testItinCardData.flightLeg.segments[0].originWaypoint.gate = "52A"
        testItinCardData.flightLeg.segments[0].destinationWaypoint = TestWayPoint("LAS", "Las Vegas", dateTime)
        testItinCardData.flightLeg.segments[0].setIsSeatMapAvailable(false)
        testItinCardData.flightLeg.segments[0].removeSeat(0)
        sut.itinCardDataFlight = testItinCardData
        sut.updateLegSummaryWidget()
        clearLegSummaryContainerSubscriber.assertValueCount(1)
        clearLegSummaryContainerSubscriber.assertValue(Unit)
        createLegSummaryWidgetsSubscriber.assertValueCount(1)
        createLegSummaryWidgetsSubscriber.assertValue(FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 681",
                "COMPASS AIRLINES",
                dateTime,
                dateTime,
                "SFO",
                "San Francisco",
                "LAS",
                "Las Vegas",
                null,
                "52A",
                "3",
                null,
                "Seat selection not available",
                " • Economy / Coach",
                null
        ))
    }
    @Test
    fun testUpdateLegSummaryNoSeatsWidget() {
        sut.clearLegSummaryContainerSubject.subscribe(clearLegSummaryContainerSubscriber)
        sut.createSegmentSummaryWidgetsSubject.subscribe(createLegSummaryWidgetsSubscriber)

        val testItinCardData = ItinCardDataFlightBuilder().build()
        val dateTime = DateTime.now()
        testItinCardData.flightLeg.segments[0].originWaypoint = TestWayPoint("SFO", "San Francisco", dateTime)
        testItinCardData.flightLeg.segments[0].originWaypoint.gate = "52A"
        testItinCardData.flightLeg.segments[0].destinationWaypoint = TestWayPoint("LAS", "Las Vegas", dateTime)
        testItinCardData.flightLeg.segments[0].removeSeat(0)
        sut.itinCardDataFlight = testItinCardData
        sut.updateLegSummaryWidget()
        clearLegSummaryContainerSubscriber.assertValueCount(1)
        clearLegSummaryContainerSubscriber.assertValue(Unit)
        createLegSummaryWidgetsSubscriber.assertValueCount(1)
        createLegSummaryWidgetsSubscriber.assertValue(FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 681",
                "COMPASS AIRLINES",
                dateTime,
                dateTime,
                "SFO",
                "San Francisco",
                "LAS",
                "Las Vegas",
                null,
                "52A",
                "3",
                null,
                "No seats selected",
                " • Economy / Coach",
                null
        ))
    }

    @Test
    fun testUpdateLegSummaryWidgetMultiSegment() {
        sut.clearLegSummaryContainerSubject.subscribe(clearLegSummaryContainerSubscriber)
        sut.createSegmentSummaryWidgetsSubject.subscribe(createLegSummaryWidgetsSubscriber)

        val testItinCardData = ItinCardDataFlightBuilder().build(multiSegment = true)
        testItinCardData.flightLeg.segments[0].originWaypoint = TestWayPoint("SFO", "San Francisco", dateTime)
        testItinCardData.flightLeg.segments[0].destinationWaypoint = TestWayPoint("EWR", "Newark", dateTime)
        testItinCardData.flightLeg.segments[1].originWaypoint = TestWayPoint("EWR", "Newark", dateTime)
        testItinCardData.flightLeg.segments[1].destinationWaypoint = TestWayPoint("PBI", "West Palm Beach", dateTime)
        testItinCardData.flightLeg.segments[1].destinationWaypoint.terminal = "5"
        testItinCardData.flightLeg.segments[1].destinationWaypoint.gate = "7A"
        sut.itinCardDataFlight = testItinCardData
        sut.updateLegSummaryWidget()
        clearLegSummaryContainerSubscriber.assertValueCount(1)
        clearLegSummaryContainerSubscriber.assertValue(Unit)
        createLegSummaryWidgetsSubscriber.assertValueCount(2)
        createLegSummaryWidgetsSubscriber.assertValues(FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 1796",
                null,
                dateTime,
                dateTime,
                "SFO",
                "San Francisco",
                "EWR",
                "Newark",
                "3",
                null,
                "C",
                null,
                "No seats selected",
                " • Economy / Coach",
                null
        ), FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 1489",
                null,
                dateTime,
                dateTime,
                "EWR",
                "Newark",
                "PBI",
                "West Palm Beach",
                "C",
                null,
                "5",
                "7A",
                "No seats selected",
                " • Economy / Coach",
                null
        ))
    }

    @Test
    fun testGetTerminalAndGate() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val fallBackTerminal = testItinCardData.flightLeg.segments[0].departureTerminal
        val testWaypoint = TestWayPoint("SFO", "San Francisco", dateTime)

        var terminalAndGate = sut.getTerminalAndGate(testWaypoint, fallBackTerminal)
        assertEquals(Pair(fallBackTerminal, null), terminalAndGate)

        testWaypoint.terminal = "5"
        terminalAndGate = sut.getTerminalAndGate(testWaypoint, fallBackTerminal)
        assertEquals(Pair("5", null), terminalAndGate)

        testWaypoint.gate = "3A"
        terminalAndGate = sut.getTerminalAndGate(testWaypoint, fallBackTerminal)
        assertEquals(Pair("5", "3A"), terminalAndGate)

        testWaypoint.terminal = null
        terminalAndGate = sut.getTerminalAndGate(testWaypoint, fallBackTerminal)
        assertEquals(Pair(null, "3A"), terminalAndGate)

        testWaypoint.gate = ""
        terminalAndGate = sut.getTerminalAndGate(testWaypoint, fallBackTerminal)
        assertEquals(Pair(fallBackTerminal, null), terminalAndGate)
    }

    @Test
    fun testGetSeatStringNoneSelected() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val flight = testItinCardData.flightLeg.segments[0]
        flight.removeSeat(0)
        assertEquals(sut.getSeatString(flight), "No seats selected")
    }

    @Test
    fun testGetSeatStringSelected() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val flight = testItinCardData.flightLeg.segments[0]
        val seatA = Seat("12A")
        val seatB = Seat("12B")
        flight.addSeat(seatA)
        flight.addSeat(seatB)
        assertEquals(sut.getSeatString(flight), "22F, 12A, 12B")
    }

    @Test
    fun testGetSeatStringNotAvailable() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val flight = testItinCardData.flightLeg.segments[0]
        flight.setIsSeatMapAvailable(false)
        flight.removeSeat(0)
        assertEquals(sut.getSeatString(flight), "Seat selection not available")
    }

    class TestWayPoint(val code: String, val city: String, val dateTime: DateTime) : Waypoint(ACTION_UNKNOWN) {
        override fun getAirport(): Airport {
            val airport = Airport()
            airport.mAirportCode = code
            airport.mCity = city
            return airport
        }

        override fun getBestSearchDateTime(): DateTime = dateTime
    }
}
