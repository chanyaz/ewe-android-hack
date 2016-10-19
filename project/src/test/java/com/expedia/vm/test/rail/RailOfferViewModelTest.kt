package com.expedia.vm.test.rail

import com.expedia.bookings.data.rail.responses.RailDateTime
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.rail.RailOfferViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailOfferViewModelTest {

    @Test
    fun testOvertaken() {
        val viewModel = RailOfferViewModel(RuntimeEnvironment.application)

        val railOffer = RailSearchResponse.RailOffer()
        val leg = RailLegOption()
        val railDateTime = RailDateTime()
        railDateTime.raw = "2016-10-08T09:30:00"
        leg.departureDateTime = railDateTime
        leg.arrivalDateTime = railDateTime
        leg.duration = "PT4H31M"
        leg.noOfChanges = 0
        railOffer.outboundLeg = leg

        val overtakenTestSubscriber = TestSubscriber.create<Boolean>()
        viewModel.overtaken.subscribe(overtakenTestSubscriber)

        viewModel.offerSubject.onNext(railOffer)
        overtakenTestSubscriber.assertValueCount(1)
        assertFalse(overtakenTestSubscriber.onNextEvents[0])

        leg.overtakenJourney = true
        viewModel.offerSubject.onNext(railOffer)
        overtakenTestSubscriber.assertValueCount(2)
        assertTrue(overtakenTestSubscriber.onNextEvents[1])
    }
}