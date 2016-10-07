package com.expedia.vm.test.rail

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.RailViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailViewModelTest {

    @Test
    fun testOutputs() {
        val viewModel = RailViewModel(RuntimeEnvironment.application)
        val testPriceSubscriber = TestSubscriber.create<String>()
        val testStopsAndDurationSubscriber = TestSubscriber.create<String>()
        val testRailCardAppliedSubscriber = TestSubscriber.create<Boolean>()

        viewModel.priceObservable.subscribe(testPriceSubscriber)
        viewModel.formattedStopsAndDurationObservable.subscribe(testStopsAndDurationSubscriber)
        viewModel.railCardAppliedObservable.subscribe(testRailCardAppliedSubscriber)

        val railLegOption = RailLegOption()
        railLegOption.duration = "PT5H34M"
        railLegOption.noOfChanges = 2
        val bestPrice = Money()
        bestPrice.formattedPrice = "$11"
        railLegOption.bestPrice = bestPrice
        railLegOption.doesAnyOfferHasFareQualifier = true

        viewModel.legOptionObservable.onNext(railLegOption)
        assertTrue(testRailCardAppliedSubscriber.onNextEvents[0])
        assertEquals("5h 34m, 2 Changes", testStopsAndDurationSubscriber.onNextEvents[0])
        assertEquals("$11", testPriceSubscriber.onNextEvents[0])
    }
}