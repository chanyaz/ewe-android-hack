package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TripFlight
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
import rx.observers.TestSubscriber
import kotlin.test.assertNotEquals

@RunWith(RobolectricRunner::class)
class FlightItinTravelerViewModelTest {

    lateinit private var activity: Activity
    lateinit var sut: FlightItinTravelerViewModel
    lateinit var sutSpy: FlightItinTravelerViewModel
    lateinit private var context: Context
    lateinit var testItinCardData: ItinCardDataFlight

    val updateToolbarSubscriber = TestSubscriber<ItinToolbarViewModel.ToolbarParams>()
    val updateTravelerListSubscriber = TestSubscriber<List<Traveler>>()
    val updateTierSubscriber = TestSubscriber<LoyaltyMembershipTier>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).get()
        sut = FlightItinTravelerViewModel(activity, "TEST_ITIN_ID")
        context = RuntimeEnvironment.application

        testItinCardData = ItinCardDataFlightBuilder().build()
        sutSpy = Mockito.spy(sut)
    }

    @Test
    fun testUpdateToolbar() {
        sut.updateToolbarSubject.subscribe(updateToolbarSubscriber)
        updateToolbarSubscriber.assertNoValues()
        val segments = testItinCardData.flightLeg.segments
        testItinCardData.flightLeg.segments[segments.size-1].destinationWaypoint = TestWayPoint("Las Vegas")
        sut.itinCardDataFlight = testItinCardData
        sut.updateToolbar()
        updateToolbarSubscriber.assertValue(ItinToolbarViewModel.ToolbarParams(
                context.getString(R.string.itin_flight_traveler_info),
                Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE).
                        put("destination", "Las Vegas").format().toString(),
                false
        ))
    }

    @Test
    fun testUpdateTravelList() {
        sut.itinCardDataFlight = testItinCardData
        sut.updateTravelerListSubject.subscribe(updateTravelerListSubscriber)
        updateTravelerListSubscriber.assertNoValues()
        sut.updateTravelList()
        val travelers  = (testItinCardData.tripComponent as TripFlight).travelers
        updateTravelerListSubscriber.assertValue(travelers)
    }

    @Test
    fun testUpdateUserLoyaltyTier() {
        sut.updateUserLoyaltyTierSubject.subscribe(updateTierSubscriber)
        updateTierSubscriber.assertNoValues()
        sut.updateUserLoyaltyTier()
        updateTierSubscriber.assertValue(LoyaltyMembershipTier.NONE)
    }

    @Test
    fun testUpdateItinCardDataFlight() {
//        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
//        Mockito.`when`(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
//        sutSpy.itineraryManager = mockItinManager
//        sutSpy.onResume()
//        Mockito.verify(sutSpy, Mockito.times(1)).updateItinCardDataFlight()
//        val anotherCard = ItinCardDataFlightBuilder().build(multiSegment = true)
//        sutSpy.itinCardDataFlight = anotherCard
//        sutSpy.updateItinCardDataFlight()
//        assertNotEquals(anotherCard, sutSpy.itinCardDataFlight)
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