package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightLeg
import com.expedia.bookings.data.FlightTrip
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.flightlib.data.Flight
import com.mobiata.flightlib.data.Waypoint
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ManageBookingFlightItinViewModelTest {
    lateinit private var context: Context
    lateinit private var activity: Activity
    lateinit private var sut: ManageBookingFlightItinViewModel

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = ManageBookingFlightItinViewModel(activity)
        context = RuntimeEnvironment.application
    }

    @Test
    fun testItinCardDataFlightForNotNull() {
        val itinCardDataValidSubscriber = TestSubscriber<Unit>()
        sut.itinCardDataNotValidSubject.subscribe(itinCardDataValidSubscriber)
        val tripFlight = getMockTripFlight()
        tripFlight.uniqueId = "TEST_ID"

        var itinCard = ItinCardDataFlight(tripFlight, 0)
        ItineraryManager.getInstance().itinCardData.clear()
        ItineraryManager.getInstance().itinCardData.add(itinCard)

        sut.setUp("TEST_ID:0")
        itinCardDataValidSubscriber.assertNoValues()
        assertEquals(itinCard, sut.itinCardDataFlight)
    }

    @Test
    fun testItinCardDataFlightForNull() {
        val itinCardDataValidSubscriber = TestSubscriber<Unit>()
        sut.itinCardDataNotValidSubject.subscribe(itinCardDataValidSubscriber)
        val tripFlight = getMockTripFlight()
        tripFlight.uniqueId = "TEST_ID"

        var itinCard = ItinCardDataFlight(tripFlight, 0)
        ItineraryManager.getInstance().itinCardData.clear()
        ItineraryManager.getInstance().itinCardData.add(itinCard)

        sut.setUp("TEST_ID:1")
        itinCardDataValidSubscriber.assertValue(Unit)
    }

    @Test
    fun testToolBar() {
        val updateToolbarSubscriber = TestSubscriber<ItinToolbarViewModel.ToolbarParams>()
        sut.updateToolbarSubject.subscribe(updateToolbarSubscriber)
        val tripFlight = getMockTripFlight()
        tripFlight.uniqueId = "TEST_ID"

        var itinCard = ItinCardDataFlight(tripFlight, 0)
        ItineraryManager.getInstance().itinCardData.clear()
        ItineraryManager.getInstance().itinCardData.add(itinCard)

        sut.setUp("TEST_ID:0")
        val title = context.getString(R.string.itin_flight_manage_booking_header)
        val destinationCity = Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE).
                put("destination", "San Francisco").format().toString()
        updateToolbarSubscriber.assertValue(ItinToolbarViewModel.ToolbarParams(title, destinationCity, false))
    }

    private fun getMockTripFlight(): TripFlight {
        val tripFlight = TripFlight()
        tripFlight.flightTrip = FlightTrip()
        val flightLeg = FlightLeg()
        val flight = Flight()
        val waypointOrigin = Waypoint(Waypoint.ACTION_DEPARTURE)
        waypointOrigin.mAirportCode = "DEL"
        val waypointArrival = Waypoint(Waypoint.ACTION_ARRIVAL)
        waypointArrival.mAirportCode = "SFO"
        flight.destinationWaypoint = waypointArrival
        flight.originWaypoint = waypointOrigin
        flightLeg.addSegment(flight)
        tripFlight.flightTrip.addLeg(flightLeg)
        return tripFlight
    }
}