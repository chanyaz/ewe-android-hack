package com.expedia.vm.test.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.data.rail.responses.RailCardSelected
import com.expedia.bookings.data.rail.responses.RailCardsResponse
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.RailCardPickerViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.Observer
import rx.Subscription
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class RailCardPickerViewModelTest {

    var railServicesRule = ServicesRule(RailServices::class.java)
        @Rule get

    val context: Context = RuntimeEnvironment.application

    var viewModel by Delegates.notNull<RailCardPickerViewModel>()
    val testSubscriber = TestSubscriber.create<List<RailCard>>()

    @Before
    fun before() {
        viewModel = RailCardPickerViewModel(railServicesRule.services!!, context)
        viewModel.railCardTypes.subscribe(testSubscriber)
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)
    }
    @Test
    fun testCardSelectionUpdates() {

        viewModel.addClickSubject.onNext(Unit)
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(0, mockRailCardTypeOne(), 1))
        assertEquals(viewModel.cardsAndQuantitySelectionDetails.size, 1)
        assertEquals(1, viewModel.cardsAndQuantitySelectionDetails[0]!!.quantity)
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(0, mockRailCardTypeOne(), 2))
        assertEquals(viewModel.cardsAndQuantitySelectionDetails.size, 1)
        assertEquals(2, viewModel.cardsAndQuantitySelectionDetails[0]!!.quantity)

        val cardsListForSearchParamsTestSubscriber = TestSubscriber.create<List<RailCard>>()
        val validationSuccessTestSubscriber = TestSubscriber.create<Unit>()
        val selectedCardsTextSubscriber = TestSubscriber.create<String>()

        viewModel.cardsListForSearchParams.subscribe(cardsListForSearchParamsTestSubscriber)
        viewModel.validationSuccess.subscribe(validationSuccessTestSubscriber)
        viewModel.cardsSelectedTextObservable.subscribe(selectedCardsTextSubscriber)

        viewModel.numberOfTravelers.onNext(2)
        viewModel.doneClickedSubject.onNext(Unit)
        cardsListForSearchParamsTestSubscriber.assertValueCount(1)
        validationSuccessTestSubscriber.assertValueCount(1)
        assertEquals(2, cardsListForSearchParamsTestSubscriber.onNextEvents[0].size)
        assertEquals("2 Railcards", selectedCardsTextSubscriber.onNextEvents[0])

        viewModel.addClickSubject.onNext(Unit)
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(1, mockRailCardTypeTwo(), 1))
        assertEquals(viewModel.cardsAndQuantitySelectionDetails.size, 2)
        assertEquals(1, viewModel.cardsAndQuantitySelectionDetails[1]!!.quantity)

        viewModel.numberOfTravelers.onNext(3)
        viewModel.doneClickedSubject.onNext(Unit)
        cardsListForSearchParamsTestSubscriber.assertValueCount(2)
        validationSuccessTestSubscriber.assertValueCount(2)
        assertEquals(3, cardsListForSearchParamsTestSubscriber.onNextEvents[1].size)
        assertEquals("3 Railcards", selectedCardsTextSubscriber.onNextEvents[1])
    }

    @Test
    fun testCardRemoval() {
        viewModel.addClickSubject.onNext(Unit)
        viewModel.addClickSubject.onNext(Unit)
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(0, mockRailCardTypeOne(), 1))
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(1, mockRailCardTypeTwo(), 1))
        assertEquals(2, viewModel.cardsAndQuantitySelectionDetails.size)

        viewModel.removeClickSubject.onNext(Unit)
        assertEquals(1, viewModel.cardsAndQuantitySelectionDetails.size)
        assertNull(viewModel.cardsAndQuantitySelectionDetails[1])
    }

    @Test
    fun testRailCardsCountGreaterThanTravelers() {
        val incorrectCardsMessage = context.resources.getString(R.string.error_rail_cards_greater_than_number_travelers)
        val testErrorSubscriber = TestSubscriber.create<String>()
        viewModel.validationError.subscribe(testErrorSubscriber)

        viewModel.addClickSubject.onNext(Unit)

        viewModel.numberOfTravelers.onNext(1)

        // Travelers = 1, RailCards = 3. Error
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(0, mockRailCardTypeOne(), 3))
        viewModel.doneClickedSubject.onNext(Unit)
        testErrorSubscriber.assertValueCount(1)
        assertEquals(incorrectCardsMessage, testErrorSubscriber.onNextEvents[0])

        // Travelers = 1, RailCards = 2. Error
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(0, mockRailCardTypeOne(), 2))
        viewModel.doneClickedSubject.onNext(Unit)
        testErrorSubscriber.assertValueCount(2)
        assertEquals(incorrectCardsMessage, testErrorSubscriber.onNextEvents[1])
    }

    @Test
    fun testRailCardTypeNotSelected() {
        val incompleteDetailsMessage = context.resources.getString(R.string.error_select_rail_card_details)
        val testErrorSubscriber = TestSubscriber.create<String>()
        viewModel.validationError.subscribe(testErrorSubscriber)

        viewModel.addClickSubject.onNext(Unit)
        viewModel.numberOfTravelers.onNext(2)
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(0, mockRailCardTypeOne(), 1))
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(1, mockEmptyRailCardType(), 1))
        viewModel.doneClickedSubject.onNext(Unit)
        testErrorSubscriber.assertValueCount(1)
        assertEquals(incompleteDetailsMessage, testErrorSubscriber.onNextEvents[0])
    }

    @Test
    fun testRailCardQuantityNotSelected() {
        val incompleteDetailsMessage = context.resources.getString(R.string.error_select_rail_card_details)
        val testErrorSubscriber = TestSubscriber.create<String>()
        viewModel.validationError.subscribe(testErrorSubscriber)

        viewModel.addClickSubject.onNext(Unit)
        viewModel.numberOfTravelers.onNext(1)
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(1, mockRailCardTypeTwo(), 0))
        viewModel.doneClickedSubject.onNext(Unit)
        testErrorSubscriber.assertValueCount(1)
        assertEquals(incompleteDetailsMessage, testErrorSubscriber.onNextEvents[0])
    }

    @Test
    fun testResetRow() {
        viewModel.addClickSubject.onNext(Unit)
        viewModel.addClickSubject.onNext(Unit)
        viewModel.railCardsSelectionChangedObservable.onNext(RailCardSelected(0, mockRailCardTypeOne(), 1))

        val testResetSubscriber = TestSubscriber.create<Unit>()
        val testRemoveRowSubscriber = TestSubscriber.create<Unit>()
        viewModel.resetClicked.subscribe(testResetSubscriber)
        viewModel.removeRow.subscribe(testRemoveRowSubscriber)

        // 2 rows present. Remove button actually removes the row.
        viewModel.removeClickSubject.onNext(Unit)
        testResetSubscriber.assertValueCount(0)
        testRemoveRowSubscriber.assertValueCount(1)

        // 1 row present. Remove button acts as reset.
        viewModel.removeClickSubject.onNext(Unit)
        testResetSubscriber.assertValueCount(1)
        testRemoveRowSubscriber.assertValueCount(1)
    }

    @Test
    fun test404Error() {
        val mockServices = Mockito.mock(RailServices::class.java)
        Mockito.`when`(mockServices.railGetCards(Mockito.anyString(), anyObject())).thenAnswer { invocation ->
            val args = invocation.arguments
            val cardsObserver = args[1] as Observer<RailCardsResponse>
            cardsObserver.onError(Throwable("404"))
            Mockito.mock(Subscription::class.java)
        }

        val errorViewModel = RailCardPickerViewModel(mockServices, context)
        val testSub = TestSubscriber<String>()
        errorViewModel.railCardError.subscribe(testSub)
        assertEquals(context.getString(R.string.no_rail_cards_error_message), testSub.onNextEvents[0])
    }

    private fun <T> anyObject(): T {
        return Mockito.anyObject<T>()
    }

    private fun mockRailCardTypeOne() = RailCard("categoryOne", "programOne", "One")
    private fun mockRailCardTypeTwo() = RailCard("categoryTwo", "programTwo", "Two")
    private fun mockEmptyRailCardType() = RailCard("", "", "")
}