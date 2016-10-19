package com.expedia.vm.rail

import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.vm.BaseCreditCardFeesViewModel
import rx.subjects.BehaviorSubject

class RailCreditCardFeesViewModel : BaseCreditCardFeesViewModel() {
    // input
    val validFormsOfPaymentObservable = BehaviorSubject.create<Pair<List<RailCreateTripResponse.RailValidFormOfPayment>, RailCreateTripResponse.RailTicketDeliveryOptionToken>>()

    init {
        validFormsOfPaymentObservable.subscribe { pair ->
            cardFeesObservable.onNext(getValidCreditCardsAndFeesForToken(pair.first, pair.second))
        }
    }

    private fun getValidCreditCardsAndFeesForToken(validPayments: List<RailCreateTripResponse.RailValidFormOfPayment>,
                                                   ticketDeliveryToken: RailCreateTripResponse.RailTicketDeliveryOptionToken): List<CardFeesRow> {
        val token = ticketDeliveryToken ?: RailCreateTripResponse.RailTicketDeliveryOptionToken.PICK_UP_AT_TICKETING_OFFICE_NONE
        val cardFees = arrayListOf<CardFeesRow>()
        for (validPayment in validPayments) {
            if (validPayment.fees[token] != null) {
                cardFees.add(CardFeesRow(validPayment.name, validPayment.fees[token]!!.feePrice.formattedPrice))
            }
        }
        return cardFees
    }

    data class CardFeesRow(val cardName: String, val fee: String)
}