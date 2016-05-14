package com.expedia.vm.flights

import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.services.FlightServices
import com.expedia.vm.packages.BaseCreateTripViewModel
import rx.Observable
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.PublishSubject

class FlightCreateTripViewModel(val flightServices: FlightServices) : BaseCreateTripViewModel() {

    val tripParams = PublishSubject.create<FlightCreateTripParams>()

    init {
        tripParams.subscribe {
            performCreateTrip.onNext(Unit)
        }

        Observable.combineLatest(tripParams, performCreateTrip, { params, createTrip ->
            flightServices.createTrip(params).subscribe(makeCreateTripResponseObserver())
        }).subscribe()
    }

    fun makeCreateTripResponseObserver(): Observer<FlightCreateTripResponse> {
        return object : Observer<FlightCreateTripResponse> {
            override fun onNext(response: FlightCreateTripResponse) {
                if (response.hasErrors() && !response.hasPriceChange()) {
                    //TODO handle errors (unhappy path story)
                } else {
                    tripResponseObservable.onNext(response)
                }
            }

            override fun onError(e: Throwable) {
                throw OnErrorNotImplementedException(e)
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }
}