package com.expedia.vm.test.rail

import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.data.rail.responses.RailCardSelected
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.RailCardPickerRowViewModel
import org.junit.Test
import org.junit.runner.RunWith
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailCardPickerRowViewModelTest {

    @Test
    fun testCardSelectionUpdates() {
        val viewModel = RailCardPickerRowViewModel(0)
        val testSubscriber = TestObserver.create<RailCardSelected>()
        viewModel.cardTypeQuantityChanged.subscribe(testSubscriber)

        val mockRailCard = RailCard("category", "program", "name")
        viewModel.cardTypeSelected.onNext(mockRailCard)
        viewModel.cardQuantitySelected.onNext(3)
        testSubscriber.assertValueCount(1)
        val railCardSelectedDetails = testSubscriber.values()[0]
        assertEquals(0, railCardSelectedDetails.id)
        assertEquals(3, railCardSelectedDetails.quantity)
        assertEquals(mockRailCard, railCardSelectedDetails.cardType)
    }
}
