package com.expedia.vm.flights

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.utils.getFee
import com.expedia.bookings.services.FlightServices
import com.expedia.vm.BaseCreateTripViewModel
import rx.Observable
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightCreateTripViewModel(val flightServices: FlightServices, val selectedCardFeeSubject: PublishSubject<ValidFormOfPayment>) : BaseCreateTripViewModel() {
    val insuranceAvailabilityObservable = BehaviorSubject.create<Boolean>()
    val tripParams = PublishSubject.create<FlightCreateTripParams>()

    init {
        Observable.combineLatest(tripParams, performCreateTrip, { params, createTrip ->
            flightServices.createTrip(params).subscribe(makeCreateTripResponseObserver())
        }).subscribe()

        updateTripFeesOnCardSelection()
    }

    private fun updateTripFeesOnCardSelection() {
        selectedCardFeeSubject
                .filter { !it.getFee().isZero }
                .filter { it.getFee().compareTo(getCreateTripResponse().selectedCardFees) != 0 } // fee changed
                .subscribe { cardFee ->
                    // add card fee to trip response
                    val newTripResponse = getCreateTripResponse()
                    newTripResponse.selectedCardFees = cardFee.getFee()
                    tripResponseObservable.onNext(newTripResponse)
                }
    }

    private fun getCreateTripResponse(): FlightCreateTripResponse {
        return tripResponseObservable.value as FlightCreateTripResponse
    }

    private fun makeCreateTripResponseObserver(): Observer<FlightCreateTripResponse> {
        return object : Observer<FlightCreateTripResponse> {
            override fun onNext(response: FlightCreateTripResponse) {
                if (response.hasErrors() && !response.hasPriceChange()) {
                    //TODO handle errors (unhappy path story)
                }
                else {
                    val hasPriceChange = response.details.oldOffer != null
                    if (hasPriceChange) {
                        priceChangeObservable.onNext(response)
                    }
                    Db.getTripBucket().clearFlight()
                    Db.getTripBucket().add(TripBucketItemFlightV2(response))
                    insuranceAvailabilityObservable.onNext(response.availableInsuranceProducts.isNotEmpty())
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
