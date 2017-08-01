package com.expedia.vm.rail

import com.expedia.bookings.ObservableOld
import com.expedia.bookings.data.TicketDeliveryOption
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.vm.BaseCreditCardFeesViewModel
import io.reactivex.subjects.BehaviorSubject

class RailCreditCardFeesViewModel : BaseCreditCardFeesViewModel() {
    // input
    val validFormsOfPaymentSubject = BehaviorSubject.create<List<RailCreateTripResponse.RailValidFormOfPayment>>()
    val ticketDeliveryOptionSubject = BehaviorSubject.create<TicketDeliveryOption>()

    init {
        ObservableOld.combineLatest(validFormsOfPaymentSubject, ticketDeliveryOptionSubject, { vfop, tdo ->
            getValidCreditCardsAndFeesForToken(vfop, tdo.deliveryOptionToken)
        }).subscribe { cardFeeRowList ->
            cardFeesObservable.onNext(cardFeeRowList)
        }
    }

    private fun getValidCreditCardsAndFeesForToken(validPayments: List<RailCreateTripResponse.RailValidFormOfPayment>,
                                                   ticketDeliveryToken: RailCreateTripResponse.RailTicketDeliveryOptionToken): List<CardFeesRow> {
        val cardFees = arrayListOf<CardFeesRow>()
        for (validPayment in validPayments) {
            if (validPayment.fees[ticketDeliveryToken] != null) {
                cardFees.add(CardFeesRow(validPayment.name, validPayment.fees[ticketDeliveryToken]!!.feePrice.formattedPrice))
            }
        }
        return cardFees
    }

    data class CardFeesRow(val cardName: String, val fee: String)
}