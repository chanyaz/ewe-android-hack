package com.expedia.vm.test.rail

import com.expedia.bookings.data.rail.responses.RailDateTime
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.rail.RailLegSummaryViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailLegSummaryViewModelTest {
    @Test
    fun testOvertaken() {
        val viewModel = RailLegSummaryViewModel(RuntimeEnvironment.application)

        val railOffer = RailSearchResponse.RailOffer()
        val leg = RailLegOption()
        val railDateTime = RailDateTime()
        railDateTime.raw = "2016-10-08T09:30:00"
        leg.departureDateTime = railDateTime
        leg.arrivalDateTime = railDateTime
        leg.duration = "PT4H31M"
        leg.noOfChanges = 0
        leg.travelSegmentList = emptyList()
        val railProduct = RailProduct()
        railProduct.aggregatedFareDescription = "Fare Description"
        railOffer.railProductList = listOf(railProduct)
        railOffer.outboundLeg = leg

        val overtakenTestSubscriber = TestSubscriber.create<Boolean>()
        viewModel.overtakenSubject.subscribe(overtakenTestSubscriber)

        viewModel.railOfferObserver.onNext(railOffer)
        overtakenTestSubscriber.assertValueCount(1)
        assertFalse(overtakenTestSubscriber.onNextEvents[0])

        leg.overtakenJourney = true
        viewModel.railOfferObserver.onNext(railOffer)
        overtakenTestSubscriber.assertValueCount(2)
        assertTrue(overtakenTestSubscriber.onNextEvents[1])
    }
}