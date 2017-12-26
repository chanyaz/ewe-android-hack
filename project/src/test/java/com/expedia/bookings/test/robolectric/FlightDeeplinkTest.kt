package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.data.FlightSearchParams
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.Ui
import com.expedia.ui.FlightActivity
import com.expedia.vm.FlightSearchViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightDeeplinkTest {

    lateinit var activity: Activity
    lateinit var flightSearchViewModel: FlightSearchViewModel

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultTravelerComponent()
        flightSearchViewModel = FlightSearchViewModel(activity)
    }

    @Test
    fun testIncompleteParams() {
        val testTransitionSubscriber = TestSubscriber<FlightActivity.Screen>()
        flightSearchViewModel.deeplinkDefaultTransitionObservable.subscribe(testTransitionSubscriber)
        flightSearchViewModel.performDeepLinkFlightSearch(makeDummyFlightSearchParams(false, false, 1))
        testTransitionSubscriber.assertReceivedOnNext(listOf(FlightActivity.Screen.SEARCH))
    }

    @Test
    fun testCompleteParams() {
        val testTransitionSubscriber = TestSubscriber<FlightActivity.Screen>()
        val testOriginSubscriber = TestSubscriber<String>()
        val testDestinationSubscriber = TestSubscriber<String>()
        val testTravelersSubscriber = TestSubscriber<TravelerParams>()
        val roundTripTestSubscriber = TestSubscriber<Boolean>()

        flightSearchViewModel.formattedOriginObservable.subscribe(testOriginSubscriber)
        flightSearchViewModel.formattedDestinationObservable.subscribe(testDestinationSubscriber)
        flightSearchViewModel.deeplinkDefaultTransitionObservable.subscribe(testTransitionSubscriber)
        flightSearchViewModel.travelersObservable.subscribe(testTravelersSubscriber)
        flightSearchViewModel.isRoundTripSearchObservable.subscribe(roundTripTestSubscriber)

        val flightSearchParams = makeDummyFlightSearchParams(complete = true, roundTrip = true, numAdults = 1)
        flightSearchViewModel.performDeepLinkFlightSearch(flightSearchParams)

        testTransitionSubscriber.assertReceivedOnNext(listOf(FlightActivity.Screen.RESULTS))
        assertTrue(flightSearchViewModel.getParamsBuilder().areRequiredParamsFilled())

        assertEquals(Pair(flightSearchParams.departureDate, flightSearchParams.returnDate),
                Pair(flightSearchViewModel.startDate(), flightSearchViewModel.endDate()))
        testOriginSubscriber.assertValue(HtmlCompat.stripHtml(flightSearchParams.departureLocation.destinationId))
        testDestinationSubscriber.assertValue(HtmlCompat.stripHtml(flightSearchParams.arrivalLocation.destinationId))
        val travelerParams = testTravelersSubscriber.onNextEvents[0]
        assertEquals(flightSearchParams.numAdults, travelerParams.numberOfAdults)
        assertEquals(0, travelerParams.childrenAges.size)
        assertEquals(0, travelerParams.seniorAges.size)
        assertEquals(0, travelerParams.youthAges.size)
        roundTripTestSubscriber.assertValues(true, true)
    }

    @Test
    fun testOneWaySearch() {
        val roundTripTestSubscriber = TestSubscriber<Boolean>()
        val flightSearchParams = makeDummyFlightSearchParams(complete = true, roundTrip = false, numAdults = 1)

        flightSearchViewModel.isRoundTripSearchObservable.subscribe(roundTripTestSubscriber)
        flightSearchViewModel.performDeepLinkFlightSearch(flightSearchParams)

        assertTrue(flightSearchViewModel.getParamsBuilder().areRequiredParamsFilled())
        roundTripTestSubscriber.assertValues(true, false)
    }

    @Test
    fun testRoundTripSearch() {
        val roundTripTestSubscriber = TestSubscriber<Boolean>()
        val flightSearchParams = makeDummyFlightSearchParams(complete = true, roundTrip = true, numAdults = 1)

        flightSearchViewModel.isRoundTripSearchObservable.subscribe(roundTripTestSubscriber)
        flightSearchViewModel.performDeepLinkFlightSearch(flightSearchParams)

        assertTrue(flightSearchViewModel.getParamsBuilder().areRequiredParamsFilled())
        roundTripTestSubscriber.assertValues(true, true)
    }

    @Test
    fun testMultiTravelerSearch() {
        val numAdults = 3
        val flightSearchParams = makeDummyFlightSearchParams(complete = true, roundTrip = true, numAdults = numAdults)

        flightSearchViewModel.performDeepLinkFlightSearch(flightSearchParams)
        val searchParams = flightSearchViewModel.getParamsBuilder().build()

        assertTrue(flightSearchViewModel.getParamsBuilder().areRequiredParamsFilled())
        assertEquals(numAdults, searchParams.adults)
    }

    private fun makeDummyFlightSearchParams(complete: Boolean, roundTrip: Boolean, numAdults: Int): FlightSearchParams {
        val flightSearchParams = FlightSearchParams()
        val arrival = Location()
        val destination = Location()
        destination.destinationId = "LAS"
        arrival.destinationId = "SFO"
        flightSearchParams.arrivalLocation = arrival
        if (complete) {
            val departureDate = LocalDate.now()
            val returnDate = departureDate.plusDays(1)
            flightSearchParams.departureLocation = destination
            flightSearchParams.departureDate = departureDate
            flightSearchParams.numAdults = numAdults
            if (roundTrip) {
                flightSearchParams.returnDate = returnDate
            }
        }
        return flightSearchParams
    }
}
