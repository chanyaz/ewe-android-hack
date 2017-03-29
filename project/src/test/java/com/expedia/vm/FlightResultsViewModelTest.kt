package com.expedia.vm

import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
class FlightResultsViewModelTest {

    val context = RuntimeEnvironment.application

    lateinit private var sut: FlightResultsViewModel

    @Before
    fun setUp() {
        sut = FlightResultsViewModel(context)
    }

    @Test
    fun airlineMayChargeFeesForApplicablePos() {
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_with_airlines_charge_additional_fees.json")

        val testSubscriber = TestSubscriber<Boolean>()
        sut.airlineChargesFeesSubject.subscribe(testSubscriber)

        sut.isOutboundResults.onNext(true)

        testSubscriber.assertValue(false)
    }

    @Test
    fun airlineDoesNotChargeFeesForNonOutboundApplicablePos() {
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_with_airlines_charge_additional_fees.json")

        val testSubscriber = TestSubscriber<Boolean>()
        sut.airlineChargesFeesSubject.subscribe(testSubscriber)

        sut.isOutboundResults.onNext(false)

        testSubscriber.assertValue(false)
    }
}
