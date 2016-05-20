package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.PackageFlightUtils
import rx.subjects.PublishSubject

class SelectedOutboundFlightViewModel(outboundFlightSelectedSubject: PublishSubject<FlightLeg>, context: Context) {

    // outputs
    val airlineNameObservable = PublishSubject.create<String>()
    val arrivalDepartureTimeObservable = PublishSubject.create<String>()

    init {
        outboundFlightSelectedSubject.subscribe({ flightLeg ->
            val airlineName = flightLeg.airlines[0].airlineName
            airlineNameObservable.onNext(airlineName)

            val arrivalDepartureTime = PackageFlightUtils.getFlightDepartureArrivalTimeAndDays(context, flightLeg)
            val flightDuration = PackageFlightUtils.getFlightDurationString(context, flightLeg)
            arrivalDepartureTimeObservable.onNext("$arrivalDepartureTime ($flightDuration)")
        })
    }
}
