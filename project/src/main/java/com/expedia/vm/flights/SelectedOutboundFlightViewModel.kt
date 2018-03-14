package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.FlightV2Utils
import io.reactivex.subjects.PublishSubject

class SelectedOutboundFlightViewModel(outboundFlightSelectedSubject: PublishSubject<FlightLeg>, context: Context, lob: LineOfBusiness) {

    // outputs
    val airlineNameObservable = PublishSubject.create<String>()
    val arrivalDepartureTimeObservable = PublishSubject.create<String>()
    val pricePerPersonObservable = PublishSubject.create<String>()

    init {
        outboundFlightSelectedSubject.subscribe({ flightLeg ->
            val airlineName = flightLeg.airlines[0].airlineName
            airlineNameObservable.onNext(airlineName)

            val arrivalDepartureTime = FlightV2Utils.getFlightDepartureArrivalTimeAndDays(context, flightLeg)
            val flightDuration = FlightV2Utils.getFlightDurationString(context, flightLeg)
            arrivalDepartureTimeObservable.onNext("$arrivalDepartureTime ($flightDuration)")

            val price = flightLeg.packageOfferModel.price.averageTotalPricePerTicket
            if (lob == LineOfBusiness.FLIGHTS_V2) {
                pricePerPersonObservable.onNext(Money.getFormattedMoneyFromAmountAndCurrencyCode(price.roundedAmount,
                        price.currencyCode, Money.F_NO_DECIMAL))
            }
        })
    }
}
