package com.expedia.vm.test.rail

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.RailViewModel
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailViewModelTest {
    val testDurationMinutes = 260
    val testFormattedDuration = "4h 20m, 3 Changes"
    val testAggregatedOperationCarrier = "The Pain Train"
    val testFormattedPrice = "$15"
    val testRoundTripFormattedPrice = "$25"

    @Test
    fun testOutputs() {
        val viewModel = RailViewModel(RuntimeEnvironment.application)
        val testStopsAndDurationSubscriber = TestSubscriber.create<String>()
        val testRailCardAppliedSubscriber = TestSubscriber.create<Boolean>()

        viewModel.formattedStopsAndDurationObservable.subscribe(testStopsAndDurationSubscriber)
        viewModel.railCardAppliedObservable.subscribe(testRailCardAppliedSubscriber)

        val railLegOption = buildMockLegOption()
        viewModel.legOptionObservable.onNext(railLegOption)
        assertTrue(testRailCardAppliedSubscriber.onNextEvents[0])
        assertEquals(testFormattedDuration, testStopsAndDurationSubscriber.onNextEvents[0])
    }

    @Test
    fun testOneWayResultPrice() {
        val railViewModel = RailViewModel(RuntimeEnvironment.application)
        val legOption = buildMockLegOption()

        val testSub = TestSubscriber<String>()
        railViewModel.priceObservable.subscribe(testSub)
        railViewModel.legOptionObservable.onNext(legOption)
        railViewModel.cheapestLegPriceObservable.onNext(null)

        assertEquals(testFormattedPrice, testSub.onNextEvents[0])
    }

    @Test
    fun testRoundTripResultPrice() {
        val railViewModel = RailViewModel(RuntimeEnvironment.application)
        val legOption = buildMockLegOption()

        val testSub = TestSubscriber<String>()
        railViewModel.priceObservable.subscribe(testSub)
        railViewModel.legOptionObservable.onNext(legOption)
        railViewModel.cheapestLegPriceObservable.onNext(Money("10", "USD"))

        assertEquals(testRoundTripFormattedPrice, testSub.onNextEvents[0])
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
}