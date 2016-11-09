package com.expedia.vm.test.rail

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.rail.RailUtils
import com.expedia.bookings.widget.RailLegOptionViewModel
import com.mobiata.flightlib.utils.DateTimeUtils
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailLegOptionViewModelTest {
    val context = RuntimeEnvironment.application
    val testViewModel = RailLegOptionViewModel(context)

    val testDurationMinutes = 260
    val testNoOfChanges = 3
    val testAggregatedOperationCarrier = "The Pain Train"
    val testFormattedPrice = "$15"
    val testRoundTripOutboundFormattedPrice = "$25"
    val testRoundTripInboundFormattedPrice = "+$10"

    val expectedDuration = DateTimeUtils.formatDuration(context.resources, testDurationMinutes)
    val expectedChangeText = RailUtils.formatRailChangesText(context, testNoOfChanges)

    @Test
    fun testFormattedStopsAndDuration() {
        val legOption = buildMockLegOption()

        val testSub = TestSubscriber<String>()
        testViewModel.formattedStopsAndDurationObservable.subscribe(testSub)
        testViewModel.legOptionObservable.onNext(legOption)

        assertEquals("$expectedDuration, $expectedChangeText", testSub.onNextEvents[0])
    }

    @Test
    fun testAggregatedOperationCarrier() {
        val legOption = buildMockLegOption()

        val testSub = TestSubscriber<String>()
        testViewModel.aggregatedOperatingCarrierSubject.subscribe(testSub)
        testViewModel.legOptionObservable.onNext(legOption)

        assertEquals(testAggregatedOperationCarrier, testSub.onNextEvents[0])
    }

    @Test
    fun testOneWayResultPrice() {
        val legOption = buildMockLegOption()
        val leg = buildMockLeg(1, Money("5", "USD"), null)

        val testSub = TestSubscriber<String>()
        testViewModel.priceObservable.subscribe(testSub)
        testViewModel.legOptionObservable.onNext(legOption)
        testViewModel.legObservable.onNext(leg)

        assertEquals(testFormattedPrice, testSub.onNextEvents[0])
    }

    @Test
    fun testRoundTripOutboundTotalPrice() {
        val legOption = buildMockLegOption()

        val leg = buildMockLeg(1, Money("5", "USD"), Money("10", "USD"))

        val testSub = TestSubscriber<String>()
        testViewModel.priceObservable.subscribe(testSub)
        testViewModel.legOptionObservable.onNext(legOption)
        testViewModel.legObservable.onNext(leg)

        assertEquals(testRoundTripOutboundFormattedPrice, testSub.onNextEvents[0])
    }

    @Test
    fun testRoundTripInboundDeltaPrice() {
        val legOption = buildMockLegOption()

        val leg = buildMockLeg(2, Money("5", "USD"), null)

        val testSub = TestSubscriber<String>()
        testViewModel.priceObservable.subscribe(testSub)
        testViewModel.legOptionObservable.onNext(legOption)
        testViewModel.legObservable.onNext(leg)

        assertEquals(testRoundTripInboundFormattedPrice, testSub.onNextEvents[0])
    }

    @Test
    fun testRailCardAppliedOutput() {
        val legOption = buildMockLegOption()

        val testSub = TestSubscriber.create<Boolean>()

        testViewModel.railCardAppliedObservable.subscribe(testSub)

        testViewModel.legOptionObservable.onNext(legOption)
        assertTrue(testSub.onNextEvents[0])
    }

    private fun buildMockLegOption(): RailLegOption {
        val legOption = Mockito.mock(RailLegOption::class.java)
        Mockito.`when`(legOption.durationMinutes()).thenReturn(testDurationMinutes)
        legOption.noOfChanges = 3

        legOption.bestPrice = Money("15", "USD")
        legOption.bestPrice.formattedPrice = testFormattedPrice
        legOption.aggregatedOperatingCarrier = testAggregatedOperationCarrier
        legOption.doesAnyOfferHasFareQualifier = true

        Mockito.`when`(legOption.getDepartureDateTime()).thenReturn(DateTime.now())
        Mockito.`when`(legOption.getArrivalDateTime()).thenReturn(DateTime.now().plusHours(1))
        return legOption
    }

    private fun buildMockLeg(legBoundOrder: Int, cheapestPrice: Money, cheapestInboundPrice: Money?): RailSearchResponse.RailLeg {
        val leg = Mockito.mock(RailSearchResponse.RailLeg::class.java)
        leg.legBoundOrder = legBoundOrder
        leg.cheapestPrice = cheapestPrice
        leg.cheapestInboundPrice = cheapestInboundPrice

        return leg
    }

}