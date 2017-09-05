package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.vm.AbstractFlightOverviewViewModel
import rx.subjects.BehaviorSubject

class FlightOverviewViewModel(context: Context) : AbstractFlightOverviewViewModel(context) {

    init {
        selectedFlightLegSubject.subscribe { selectedFlight ->
            updateOBFees(selectedFlight)
        }
    }

    override val showBundlePriceSubject = BehaviorSubject.create(true)
    override val showEarnMessage = BehaviorSubject.create(false)
    override val showSeatClassAndBookingCode = BehaviorSubject.create(false)

    override fun pricePerPersonString(selectedFlight: FlightLeg): String {
        return selectedFlight.packageOfferModel.price.differentialPriceFormatted
    }

    override fun shouldShowBasicEconomyMessage(selectedFlight: FlightLeg): Boolean {
        return false
    }

    fun updateOBFees(selectedFlight: FlightLeg) {
        if (selectedFlight.airlineMessageModel?.hasAirlineWithCCfee ?: false || selectedFlight.mayChargeObFees) {
            val paymentFeeText = context.resources.getString(R.string.payment_and_baggage_fees_may_apply)
            chargesObFeesTextSubject.onNext(paymentFeeText)
            val hasAirlineFeeLink = !selectedFlight.airlineMessageModel?.airlineFeeLink.isNullOrBlank()
            if (hasAirlineFeeLink){
                obFeeDetailsUrlObservable.onNext(e3EndpointUrl + selectedFlight.airlineMessageModel.airlineFeeLink)
            } else {
                obFeeDetailsUrlObservable.onNext("")
            }
        }
        else{
            chargesObFeesTextSubject.onNext("")
            obFeeDetailsUrlObservable.onNext("")
        }
    }
}