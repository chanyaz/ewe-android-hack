package com.expedia.vm.test.rail

import com.expedia.bookings.data.rail.responses.RailDateTime
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailTripOffer
import com.expedia.bookings.data.rail.responses.RailTripProduct
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.rail.RailTripSummaryViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailTripSummaryViewModelTest {

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY,
            MultiBrand.AIRASIAGO, MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun testOutputs() {
        val viewModel = RailTripSummaryViewModel(RuntimeEnvironment.application)

        val testFormattedDatesSubscriber = TestSubscriber<String>()

        viewModel.formattedOutboundDateObservable.subscribe(testFormattedDatesSubscriber)

        viewModel.railOfferObserver.onNext(getRailOffer())
        viewModel.railOutboundLegObserver.onNext(getRailLegOption())

        assertEquals("Outbound - Sat Oct 08", testFormattedDatesSubscriber.onNextEvents[0])
    }

    private fun getRailOffer(): RailTripOffer {
        val railOffer = RailTripOffer()
        val railProduct = RailTripProduct()
        railOffer.railProductList = listOf(railProduct)
        return railOffer
    }

    private fun getRailLegOption(): RailLegOption {
        val leg = RailLegOption()
        val railDateTime = RailDateTime()
        railDateTime.raw = "2016-10-08T09:30:00"
        leg.departureDateTime = railDateTime
        return leg
    }
}