package com.expedia.bookings.widget.rail

import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.data.rail.responses.RailCardSelected
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.rail.widget.RailCardPickerRowView
import com.expedia.bookings.widget.SpinnerAdapterWithHint
import com.expedia.vm.RailCardPickerRowViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class RailCardPickerRowViewTest {

    var cardPickerRow by Delegates.notNull<RailCardPickerRowView>()

    private val mockRailCardOne = RailCard("1", "1", "1")
    private val mockRailCardTwo = RailCard("2", "2", "2")

    private val context = RuntimeEnvironment.application
    @Before
    fun before() {
        cardPickerRow = RailCardPickerRowView(context)
        val viewModel = RailCardPickerRowViewModel(0)
        cardPickerRow.viewModel = viewModel
        viewModel.cardTypesList.onNext(listOf(mockRailCardOne, mockRailCardTwo))
    }

    @Test
    fun testRowSelections() {
        assertNotNull(cardPickerRow.cardTypeSpinner.adapter)
        assertNotNull(cardPickerRow.cardQuantitySpinner.adapter)

        val testSubscriber = TestObserver.create<RailCardSelected>()
        cardPickerRow.viewModel.cardTypeQuantityChanged.subscribe(testSubscriber)

        assertEquals(context.resources.getString(R.string.select_rail_card_hint), (cardPickerRow.cardTypeSpinner.selectedItem as SpinnerAdapterWithHint.SpinnerItem).value)
        assertEquals(context.resources.getString(R.string.select_rail_card_quantity_hint), (cardPickerRow.cardQuantitySpinner.selectedItem as SpinnerAdapterWithHint.SpinnerItem).value)
        testSubscriber.assertValueCount(0)

        cardPickerRow.cardTypeSpinner.setSelection(0)
        assertSelectionDetails(testSubscriber.onNextEvents[0], 0, 1, mockRailCardOne)

        cardPickerRow.cardQuantitySpinner.setSelection(1)
        assertSelectionDetails(testSubscriber.onNextEvents[1], 0, 2, mockRailCardOne)

        cardPickerRow.cardTypeSpinner.setSelection(1)
        assertSelectionDetails(testSubscriber.onNextEvents[2], 0, 2, mockRailCardTwo)
    }

    private fun assertSelectionDetails(railCardSelectedDetails:RailCardSelected, id: Int, quantity: Int, cardType: RailCard) {
        assertEquals(id, railCardSelectedDetails.id)
        assertEquals(quantity, railCardSelectedDetails.quantity)
        assertEquals(cardType, railCardSelectedDetails.cardType)
    }
}