package com.expedia.vm.test.rail

import android.content.Context
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.data.rail.responses.RailCardSelected
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.RailCardPickerViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class RailCardPickerViewModelTest {

    var railServicesRule = ServicesRule(RailServices::class.java)
        @Rule get

    val context: Context = RuntimeEnvironment.application

    @Test
    fun testCardSelectionUpdates() {
        val viewModel = RailCardPickerViewModel(railServicesRule.services!!, context)
        val testSubscriber = TestSubscriber.create<List<RailCard>>()
        viewModel.railCardTypes.subscribe(testSubscriber)
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)
        viewModel.addClickSubject.onNext(Unit)
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(0, mockRailCardTypeOne(), 1))
        assertEquals(viewModel.cardsAndQuantitySelectionDetails.size, 1)
        assertEquals(1, viewModel.cardsAndQuantitySelectionDetails[0]!!.quantity)

        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(0, mockRailCardTypeOne(), 2))
        assertEquals(viewModel.cardsAndQuantitySelectionDetails.size, 1)
        assertEquals(2, viewModel.cardsAndQuantitySelectionDetails[0]!!.quantity)

        val cardsListForSearchParamsTestSubscriber = TestSubscriber.create<List<RailCard>>()
        val validationSuccessTestSubscriber = TestSubscriber.create<Unit>()
        viewModel.cardsListForSearchParams.subscribe(cardsListForSearchParamsTestSubscriber)
        viewModel.validationSuccessSubject.subscribe(validationSuccessTestSubscriber)

        viewModel.doneClickedSubject.onNext(Unit)
        cardsListForSearchParamsTestSubscriber.assertValueCount(1)
        validationSuccessTestSubscriber.assertValueCount(1)
        assertEquals(2, cardsListForSearchParamsTestSubscriber.onNextEvents[0].size)

        viewModel.addClickSubject.onNext(Unit)
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(1, mockRailCardTypeTwo(), 1))
        assertEquals(viewModel.cardsAndQuantitySelectionDetails.size, 2)
        assertEquals(1, viewModel.cardsAndQuantitySelectionDetails[1]!!.quantity)

        viewModel.doneClickedSubject.onNext(Unit)
        cardsListForSearchParamsTestSubscriber.assertValueCount(2)
        validationSuccessTestSubscriber.assertValueCount(2)
        assertEquals(3, cardsListForSearchParamsTestSubscriber.onNextEvents[1].size)
    }

    @Test
    fun testCardRemoval() {
        val viewModel = RailCardPickerViewModel(railServicesRule.services!!, context)
        val testSubscriber = TestSubscriber.create<List<RailCard>>()
        viewModel.railCardTypes.subscribe(testSubscriber)
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)
        viewModel.addClickSubject.onNext(Unit)
        viewModel.addClickSubject.onNext(Unit)
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(0, mockRailCardTypeOne(), 1))
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(1, mockRailCardTypeTwo(), 1))
        assertEquals(2, viewModel.cardsAndQuantitySelectionDetails.size)

        viewModel.removeClickSubject.onNext(Unit)
        assertEquals(1, viewModel.cardsAndQuantitySelectionDetails.size)
        assertNull(viewModel.cardsAndQuantitySelectionDetails[1])
    }

    private fun mockRailCardTypeOne() = RailCard("categoryOne", "programOne", "One")
    private fun mockRailCardTypeTwo() = RailCard("categoryTwo", "programTwo", "Two")
}