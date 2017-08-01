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
import com.expedia.bookings.services.TestObserver
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

        val overtakenTestObserver = TestObserver.create<Boolean>()
        viewModel.overtakenSubject.subscribe(overtakenTestObserver)

        viewModel.railLegOptionObserver.onNext(leg)
        overtakenTestObserver.assertValueCount(1)
        assertFalse(overtakenTestObserver.onNextEvents[0])

        leg.overtakenJourney = true
        viewModel.railLegOptionObserver.onNext(leg)
        overtakenTestObserver.assertValueCount(2)
        assertTrue(overtakenTestObserver.onNextEvents[1])
    }

    @Test
    fun testRailcardsGetConcatenated() {
        val viewModel = RailLegSummaryViewModel(RuntimeEnvironment.application)
        val railProduct = RailProduct()
        val card1 = RailCard("cat", "prog", "name1")
        val card2 = RailCard("cat", "prog", "name2")
        railProduct.fareQualifierList = listOf(card1, card2)

        val cardNameTestObserver = TestObserver.create<String>()
        viewModel.railCardNameObservable.subscribe(cardNameTestObserver)

        viewModel.railProductObserver.onNext(railProduct)
        assertEquals("name1, name2", cardNameTestObserver.onNextEvents[0])
    }
}