package com.expedia.vm

import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
class FlightResultsViewModelTest {

    val context = RuntimeEnvironment.application

    lateinit private var sut: FlightResultsViewModel

    @Before
    fun setUp() {
        sut = FlightResultsViewModel()
    }

    @Test
    fun airlineMayChargeFeesForApplicablePos() {
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_with_airlines_charge_additional_fees.json")

        val testSubscriber = TestSubscriber<Boolean>()
        sut.airlineChargesFeesSubject.subscribe(testSubscriber)

        sut.flightResultsObservable.onNext(noFeesLegs())
        sut.isOutboundResults.onNext(true)

        testSubscriber.assertValue(true)
    }

    @Test
    fun legMayChargeFeesShowFeeMessaging() {
        val testSubscriber = TestSubscriber<Boolean>()
        sut.airlineChargesFeesSubject.subscribe(testSubscriber)

        sut.flightResultsObservable.onNext(legsMayHaveFees())
        sut.isOutboundResults.onNext(true)

        testSubscriber.assertValue(true)
    }

    @Test
    fun goodLegsNoFeesDontShowMessaging() {
        val testSubscriber = TestSubscriber<Boolean>()
        sut.airlineChargesFeesSubject.subscribe(testSubscriber)

        sut.flightResultsObservable.onNext(noFeesLegs())
        sut.isOutboundResults.onNext(true)

        testSubscriber.assertValue(false)
    }

    @Test
    fun returnResultsDontShowFeesMessaging() {
        val testSubscriber = TestSubscriber<Boolean>()
        sut.airlineChargesFeesSubject.subscribe(testSubscriber)

        sut.flightResultsObservable.onNext(legsMayHaveFees())
        sut.isOutboundResults.onNext(false)

        testSubscriber.assertValue(false)
    }

    private fun legsMayHaveFees(): List<FlightLeg> {
        val legWithFees = FlightLeg()
        legWithFees.mayChargeObFees = true
        val legNoFees = FlightLeg()

        return listOf(legNoFees, legWithFees)
    }

    private fun noFeesLegs(): List<FlightLeg> {
        val legOne = FlightLeg()
        return listOf(legOne)
    }
}
