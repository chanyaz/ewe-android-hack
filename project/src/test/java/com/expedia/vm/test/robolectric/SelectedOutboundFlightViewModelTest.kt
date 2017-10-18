package com.expedia.vm.test.robolectric

import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowDateFormat
import com.expedia.vm.flights.SelectedOutboundFlightViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import com.expedia.bookings.services.TestObserver
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowDateFormat::class))
class SelectedOutboundFlightViewModelTest {

    private val AIRLINE_NAME = "Tom Air"

    private lateinit var sut: SelectedOutboundFlightViewModel
    private val context = RuntimeEnvironment.application
    private val mockFlightSelectedSubject = PublishSubject.create<FlightLeg>()

    @Before
    fun setup() {
        sut = SelectedOutboundFlightViewModel(mockFlightSelectedSubject, context)
    }

    @Test
    fun airlineName() {
        val testSubscriber = TestObserver.create<String>()
        sut.airlineNameObservable.subscribe(testSubscriber)

        val flightLeg = createFakeFlightLeg()
        mockFlightSelectedSubject.onNext(flightLeg)

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        val resultAirline = testSubscriber.values()[0]

        assertEquals(AIRLINE_NAME, resultAirline)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
            MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun arrivalDepartureTimeAndDuration() {
        val testSubscriber = TestObserver.create<String>()
        sut.arrivalDepartureTimeObservable.subscribe(testSubscriber)

        mockFlightSelectedSubject.onNext(createFakeFlightLeg())

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        val resultArrivalDepartureTimeAndDuration = testSubscriber.values()[0]

        val expected = "1:10 am - 12:20 pm +1d (13h 59m)"
        assertEquals(expected, resultArrivalDepartureTimeAndDuration)
    }

    private fun createFakeFlightLeg(): FlightLeg {
        val flightLeg = FlightLeg()
        val airline = Airline(AIRLINE_NAME, "")

        flightLeg.airlines = listOf(airline)
        flightLeg.durationHour = 13
        flightLeg.durationMinute = 59
        flightLeg.stopCount = 1
        flightLeg.departureDateTimeISO = "2016-03-09T01:10:00.000-05:00"
        flightLeg.arrivalDateTimeISO = "2016-03-10T12:20:00.000-07:00"
        flightLeg.elapsedDays = 1

        return flightLeg
    }

}
