package com.expedia.vm.test.rail

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.data.rail.responses.RailDateTime
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.rail.RailTripSummaryViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailTripSummaryViewModelTest {

    val fareDescription = "Fare Description"
    val railCardName = "Rail Card Name"
    @Test
    fun testOutputs() {
        val viewModel = RailTripSummaryViewModel(RuntimeEnvironment.application)

        val testFormattedDatesSubscriber = TestSubscriber<String>()
        val testFareDescriptionSubscriber = TestSubscriber<String>()
        val testRailCardNameSubscriber = TestSubscriber<String>()

        viewModel.formattedDatesObservable.subscribe(testFormattedDatesSubscriber)
        viewModel.fareDescriptionObservable.subscribe(testFareDescriptionSubscriber)
        viewModel.railCardNameObservable.subscribe(testRailCardNameSubscriber)

        viewModel.railOfferObserver.onNext(getRailOffer())
        viewModel.railLegObserver.onNext(getRailLegOption())

        assertEquals(fareDescription, testFareDescriptionSubscriber.onNextEvents[0])
        assertEquals(railCardName, testRailCardNameSubscriber.onNextEvents[0])
        assertEquals("Outbound - Sat Oct 08", testFormattedDatesSubscriber.onNextEvents[0])
    }

    private fun getRailOffer(): RailSearchResponse.RailOffer {
        val railOffer = RailSearchResponse.RailOffer()
        val railProduct = RailProduct()
        railProduct.aggregatedFareDescription = fareDescription
        val fareQualifierList = listOf(RailCard("", "", railCardName))
        railProduct.fareQualifierList = fareQualifierList
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