package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.data.rail.responses.RailCardSelected
import com.expedia.bookings.data.rail.responses.RailCardsResponse
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.widget.RailCardPickerRowView
import com.mobiata.android.util.SettingUtils
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.HashMap

class RailCardPickerViewModel(val railServices: RailServices, val context: Context) {

    val railCardsSelectionChangedObservable: PublishSubject<RailCardSelected> = PublishSubject.create()
    val doneClickedSubject: PublishSubject<Unit> = PublishSubject.create()

    val validationSuccessSubject: PublishSubject<Unit> = PublishSubject.create()

    val cardsAndQuantitySelectionDetails = HashMap<Int, RailCardSelected>()
    val cardsListForSearchParams: PublishSubject<List<RailCard>> = PublishSubject.create()

    val railCardTypes = PublishSubject.create<List<RailCard>>()
    val addClickSubject = PublishSubject.create<Unit>()
    val removeClickSubject = PublishSubject.create<Unit>()

    val addView = PublishSubject.create<RailCardPickerRowView>()
    val removeButtonEnableState = PublishSubject.create<Boolean>()
    var rowId = 0

    init {
        railCardsSelectionChangedObservable.subscribe { railCardSelected ->
            cardsAndQuantitySelectionDetails.put(railCardSelected.id, railCardSelected)
        }

        doneClickedSubject.subscribe {
            // TODO validate and show errors.
            val selectedRailCards = ArrayList<RailCard>()

            // Api search request does not accept quantity for each card type.
            // Instead, it requires us to send a separate object if there are more than one card of same type
            cardsAndQuantitySelectionDetails.map { railCardAndQuantitySelected -> railCardAndQuantitySelected.value }.forEach { card ->
                for (i in 1..card.quantity) {
                    selectedRailCards.add(card.cardType)
                }
            }

            // On validation success, set search params and close dialog
            cardsListForSearchParams.onNext(selectedRailCards)
            validationSuccessSubject.onNext(Unit)
        }

        addClickSubject.withLatestFrom(railCardTypes, {x, y -> y}).subscribe {
            addRow(it)
        }

        removeClickSubject.subscribe {
            rowId--
            cardsAndQuantitySelectionDetails.remove(rowId)
            removeButtonEnableState.onNext(rowId != 1)
        }
        fetchRailCards()
    }

    private fun fetchRailCards() {
        railServices.railGetCards(PointOfSale.getPointOfSale().localeIdentifier, object: Observer<RailCardsResponse> {
            override fun onError(e: Throwable?) {
                throw OnErrorNotImplementedException(e)
            }

            override fun onNext(response: RailCardsResponse?) {
                // TODO Remove this check once the api format is fixed.
                // https://jira/jira/browse/EWERAILS-1507
                val selectedEnv = SettingUtils.get(context, context.getString(R.string.preference_which_api_to_use_key), "")
                if (selectedEnv.equals("Mock Mode")) {
                    railCardTypes.onNext(response!!.railCards)
                }
                else {
                    railCardTypes.onNext(listOf(RailCard("", "", "Test")))
                }
            }

            override fun onCompleted() {
                // Ignore
            }

        })
    }

    fun addRow(railCards: List<RailCard>) {
        val railCardPickerViewModel = RailCardPickerRowViewModel(rowId)
        val row = RailCardPickerRowView(context)
        row.viewModel = railCardPickerViewModel

        railCardPickerViewModel.cardTypesList.onNext(railCards)
        railCardPickerViewModel.cardTypeQuantityChanged.subscribe(railCardsSelectionChangedObservable)

        addView.onNext(row)
        rowId++
        removeButtonEnableState.onNext(rowId != 1)
    }
}