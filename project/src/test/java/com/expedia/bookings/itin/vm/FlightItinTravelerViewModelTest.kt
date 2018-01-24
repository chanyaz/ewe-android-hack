package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.mobiata.flightlib.data.Airport
import com.mobiata.flightlib.data.Waypoint
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@RunWith(RobolectricRunner::class)
class FlightItinTravelerViewModelTest {

    private lateinit var activity: Activity
    lateinit var sut: FlightItinTravelerViewModel
    lateinit var sutSpy: FlightItinTravelerViewModel
    private lateinit var context: Context
    lateinit var testItinCardData: ItinCardDataFlight

    val updateToolbarSubscriber = TestObserver<ItinToolbarViewModel.ToolbarParams>()
    val updateTravelerListSubscriber = TestObserver<List<Traveler>>()
    val itinCardDataNotValidSubscriber = TestObserver<Unit>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).get()
        sut = FlightItinTravelerViewModel(activity, "TEST_ITIN_ID")
        context = RuntimeEnvironment.application

        testItinCardData = ItinCardDataFlightBuilder().build()
        sutSpy = Mockito.spy(FlightItinTravelerViewModel(activity, "TEST_ITIN_ID"))
    }

    @Test
    fun testUpdateToolbar() {
        sut.updateToolbarSubject.subscribe(updateToolbarSubscriber)
        updateToolbarSubscriber.assertNoValues()
        val segments = testItinCardData.flightLeg.segments
        testItinCardData.flightLeg.segments[segments.size - 1].destinationWaypoint = TestWayPoint("Las Vegas")
        sut.itinCardDataFlight = testItinCardData
        sut.updateToolbar()
        updateToolbarSubscriber.assertValue(ItinToolbarViewModel.ToolbarParams(
                context.getString(R.string.itin_flight_traveler_info),
                Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE)
                        .put("destination", "Las Vegas").format().toString(),
                false
        ))
    }

    @Test
    fun testToolBarWithoutWaypoint() {
        sut.updateToolbarSubject.subscribe(updateToolbarSubscriber)
        updateToolbarSubscriber.assertNoValues()
        testItinCardData.flightLeg.segments.clear()
        sut.itinCardDataFlight = testItinCardData
        sut.updateToolbar()
        updateToolbarSubscriber.assertValue(ItinToolbarViewModel.ToolbarParams(
                context.getString(R.string.itin_flight_traveler_info),
                Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE)
                        .put("destination", "").format().toString(),
                false
        ))
    }

    @Test
    fun testUpdateTravelList() {
        sut.itinCardDataFlight = testItinCardData
        sut.updateTravelerListSubject.subscribe(updateTravelerListSubscriber)
        updateTravelerListSubscriber.assertNoValues()
        sut.updateTravelList()
        val travelers = (testItinCardData.tripComponent as TripFlight).travelers
        updateTravelerListSubscriber.assertValue(travelers)
    }

    @Test
    fun testUpdateItinCardDataFlight() {
        val anotherItinCard = ItinCardDataFlightBuilder().build(multiSegment = true)
        sut.itinCardDataFlight = anotherItinCard
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        Mockito.`when`(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.onResume()
        assertNotEquals(anotherItinCard, sut.itinCardDataFlight)
    }

    @Test
    fun testUpdateNullItinCardDataFlight() {
        val anotherItinCard = ItinCardDataFlightBuilder().build(multiSegment = true)
        sut.itinCardDataNotValidSubject.subscribe(itinCardDataNotValidSubscriber)
        itinCardDataNotValidSubscriber.assertNoValues()
        sut.itinCardDataFlight = anotherItinCard
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        Mockito.`when`(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(null)
        sut.itineraryManager = mockItinManager
        sut.onResume()
        assertEquals(anotherItinCard, sut.itinCardDataFlight)
        itinCardDataNotValidSubscriber.assertValueCount(1)
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
}
