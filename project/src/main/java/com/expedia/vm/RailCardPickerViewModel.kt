package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.data.rail.responses.RailCardSelected
import com.expedia.bookings.data.rail.responses.RailCardsResponse
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.tracking.RailTracking
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.rail.widget.RailCardPickerRowView
import com.expedia.bookings.extensions.withLatestFrom
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList
import java.util.HashMap

class RailCardPickerViewModel(val railServices: RailServices, val context: Context) {

    val railCardsSelectionChangedObservable: PublishSubject<RailCardSelected> = PublishSubject.create()
    val numberOfTravelers: PublishSubject<Int> = PublishSubject.create()
    val doneClickedSubject: PublishSubject<Unit> = PublishSubject.create()

    val validationError: PublishSubject<String> = PublishSubject.create()
    val validationSuccess: PublishSubject<Unit> = PublishSubject.create()

    val cardsAndQuantitySelectionDetails = HashMap<Int, RailCardSelected>()
    val cardsListForSearchParams: PublishSubject<List<RailCard>> = PublishSubject.create()

    val cardsSelectedTextObservable = PublishSubject.create<String>()

    val railCardTypes = PublishSubject.create<List<RailCard>>()
    val railCardError = BehaviorSubject.create<String>()
    val addClickSubject = PublishSubject.create<Unit>()
    val removeClickSubject = PublishSubject.create<Unit>()

    val removeRow = PublishSubject.create<Unit>()
    val resetClicked = PublishSubject.create<Unit>()

    val addView = PublishSubject.create<RailCardPickerRowView>()
    val addButtonEnableState = PublishSubject.create<Boolean>()
    val removeButtonEnableState = PublishSubject.create<Boolean>()
    var rowId = 0
    val MAX_ROWS = 8

    val doneClickedWithNumberOfTravelers = doneClickedSubject.withLatestFrom(numberOfTravelers, { _, numberOfTravelers -> numberOfTravelers })

    init {
        railCardsSelectionChangedObservable.subscribe { railCardSelected ->
            cardsAndQuantitySelectionDetails.put(railCardSelected.id, railCardSelected)
        }

        railCardsSelectionChangedObservable.filter { it.id == 0 }.subscribe { railCardSelected ->
            removeButtonEnableState.onNext(railCardSelected.isResetState())
        }

        doneClickedWithNumberOfTravelers.filter { validate(it) }.subscribe {
            val selectedRailCards = ArrayList<RailCard>()

            // Api search request does not accept quantity for each card type.
            // Instead, it requires us to send a separate object if there are more than one card of same type
            cardsAndQuantitySelectionDetails.map { railCardAndQuantitySelected -> railCardAndQuantitySelected.value }
                    .filter { it.quantity != 0 }
                    .forEach { card ->
                        for (i in 1..card.quantity) {
                            selectedRailCards.add(card.cardType)
                        }
                    }
            // On validation success, set search params and close dialog
            cardsListForSearchParams.onNext(selectedRailCards)
            cardsSelectedTextObservable.onNext(getRailcardsString(selectedRailCards.size))

            validationSuccess.onNext(Unit)
        }

        addClickSubject.withLatestFrom(railCardTypes, { _, railCard -> railCard }).subscribe { railCard ->
            addRow(railCard)
            RailTracking().trackRailCardPicker("Add")
        }

        removeClickSubject.subscribe {
            RailTracking().trackRailCardPicker("Remove")
            addButtonEnableState.onNext(true)
            cardsAndQuantitySelectionDetails.remove(rowId - 1)
            if (rowId == 1) {
                resetClicked.onNext(Unit)
                removeButtonEnableState.onNext(false)
            } else {
                rowId--
                removeRow.onNext(Unit)
                setRemoveButtonState()
            }
        }
        fetchRailCards()
    }

    private fun getRailcardsString(total: Int): String {
        if (total == 0) {
            return context.getString(R.string.add_rail_card)
        }
        return StrUtils.formatRailcardString(context, total)
    }

    private fun setRemoveButtonState() {
        if (rowId == 1 && isFirstRowInResetState()) {
            removeButtonEnableState.onNext(false)
        } else {
            removeButtonEnableState.onNext(true)
        }
    }

    private fun fetchRailCards() {
        railServices.railGetCards(PointOfSale.getPointOfSale().localeIdentifier, object : DisposableObserver<RailCardsResponse>() {
            override fun onError(e: Throwable) {
                railCardError.onNext(context.getString(R.string.no_rail_cards_error_message))
                RailTracking().trackRailCardsApiNoResponseError()
            }

            override fun onNext(response: RailCardsResponse) {
                railCardTypes.onNext(response.railCards)
            }

            override fun onComplete() {
                // Ignore
            }
        })
    }

    fun addRow(railCards: List<RailCard>) {
        if (rowId < MAX_ROWS) {
            val railCardPickerViewModel = RailCardPickerRowViewModel(rowId)
            railCardPickerViewModel.cardTypeQuantityChanged.subscribe(railCardsSelectionChangedObservable)

            val row = RailCardPickerRowView(context)
            row.viewModel = railCardPickerViewModel

            railCardPickerViewModel.cardTypesList.onNext(railCards)

            resetClicked.subscribe(railCardPickerViewModel.resetRow)

            addView.onNext(row)
            rowId++
            removeButtonEnableState.onNext(rowId != 1)
            if (rowId == MAX_ROWS) {
                addButtonEnableState.onNext(false)
            }
        }
    }

    private fun validate(numberOfTravelers: Int): Boolean {
        val numberOfCardsSelected = cardsAndQuantitySelectionDetails.map { it.value.quantity }.sum()
        if (numberOfCardsSelected > numberOfTravelers) {
            validationError.onNext(context.resources.getString(R.string.error_rail_cards_greater_than_number_travelers))
            return false
        }

        // When there is only first row and both card type and quantity are not selected, close dialog.
        if (cardsAndQuantitySelectionDetails.size == 1 && isFirstRowInResetState()) {
            return true
        }

        // If partially filled rows, show error.
        else if (cardsAndQuantitySelectionDetails.filter { railCardHashMap -> railCardHashMap.value.isSelectionPartial() }.size > 0) {
            validationError.onNext(context.resources.getString(R.string.error_select_rail_card_details))
            return false
        }
        return true
    }

    private fun isFirstRowInResetState(): Boolean {
        val firstRowCompletelyEmpty = (cardsAndQuantitySelectionDetails[0] as RailCardSelected).isSelectionEmpty()
        return firstRowCompletelyEmpty
    }
}
