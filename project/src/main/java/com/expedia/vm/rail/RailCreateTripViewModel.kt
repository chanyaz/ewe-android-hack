package com.expedia.vm.rail

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.trips.TripBucketItemRails
import com.expedia.bookings.services.RailServices
import com.expedia.vm.packages.BaseCreateTripViewModel
import rx.Observer
import rx.subjects.PublishSubject

class RailCreateTripViewModel(val railServices: RailServices) : BaseCreateTripViewModel() {

    val offerCodeSelectedObservable = PublishSubject.create<String>()

    init {
        offerCodeSelectedObservable.subscribe { offerCode ->
            railServices.railCreateTrip(offerCode, makeCreateTripResponseObserver())
        }
    }

    fun makeCreateTripResponseObserver(): Observer<RailCreateTripResponse> {

        return object : Observer<RailCreateTripResponse> {
            override fun onNext(response: RailCreateTripResponse) {
                if (response.hasErrors() && !response.hasPriceChange()) {
                    if (response.firstError.errorCode == ApiError.Code.UNKNOWN_ERROR) {
                        createTripErrorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                    }
                } else {
                    if (response.hasPriceChange()) {
                        //TODO handle price change
                    } else {
                        Db.getTripBucket().clearRails()
                        Db.getTripBucket().add(TripBucketItemRails(response))
                        tripResponseObservable.onNext(response)
                    }
                }
            }

            override fun onError(e: Throwable?) {
                throw UnsupportedOperationException(e)
            }

            override fun onCompleted() {
            }
        }
    }


}