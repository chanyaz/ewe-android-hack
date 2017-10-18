package com.expedia.vm.test.rail

import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.data.rail.responses.RailDateTime
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.rail.RailLegSummaryViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailLegSummaryViewModelTest {
    @Test
    fun testOvertaken() {
        val viewModel = RailLegSummaryViewModel(RuntimeEnvironment.application)

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

        val overtakenTestSubscriber = TestSubscriber.create<Boolean>()
        viewModel.overtakenSubject.subscribe(overtakenTestSubscriber)

        viewModel.railLegOptionObserver.onNext(leg)
        overtakenTestSubscriber.assertValueCount(1)
        assertFalse(overtakenTestSubscriber.onNextEvents[0])

        leg.overtakenJourney = true
        viewModel.railLegOptionObserver.onNext(leg)
        overtakenTestSubscriber.assertValueCount(2)
        assertTrue(overtakenTestSubscriber.onNextEvents[1])
    }

    @Test
    fun testRailcardsGetConcatenated() {
        val viewModel = RailLegSummaryViewModel(RuntimeEnvironment.application)
        val railProduct = RailProduct()
        val card1 = RailCard("cat", "prog", "name1")
        val card2 = RailCard("cat", "prog", "name2")
        railProduct.fareQualifierList = listOf(card1, card2)

        val cardNameTestSubscriber = TestSubscriber.create<String>()
        viewModel.railCardNameObservable.subscribe(cardNameTestSubscriber)

        viewModel.railProductObserver.onNext(railProduct)
        assertEquals("name1, name2", cardNameTestSubscriber.onNextEvents[0])
    }
}