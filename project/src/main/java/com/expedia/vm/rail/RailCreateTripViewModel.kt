package com.expedia.vm.rail

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.trips.TripBucketItemRails
import com.expedia.bookings.services.RailServices
import com.expedia.util.endlessObserver
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailCreateTripViewModel(val railServices: RailServices) {
    val offerTokensSelected = PublishSubject.create<List<String>>()

    // outputs
    val tripResponseObservable = BehaviorSubject.create<RailCreateTripResponse>()
    val createTripErrorObservable = PublishSubject.create<ApiError>()
    val createTripCallTriggeredObservable = PublishSubject.create<Unit>()

    init {
        offerTokensSelected.subscribe { offerTokens ->
            railServices.railCreateTrip(offerTokens, makeCreateTripResponseObserver())
            createTripCallTriggeredObservable.onNext(Unit)
        }
    }

    fun makeCreateTripResponseObserver(): Observer<RailCreateTripResponse> {

        return object : Observer<RailCreateTripResponse> {
            override fun onNext(response: RailCreateTripResponse) {
                if (response.hasErrors() && !response.hasPriceChange()) {
                    when (response.firstError.errorCode) {
                        ApiError.Code.UNKNOWN_ERROR -> {
                            createTripErrorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                        }
                        ApiError.Code.RAIL_PRODUCT_LOOKUP_ERROR -> {
                            createTripErrorObservable.onNext(ApiError(ApiError.Code.RAIL_PRODUCT_LOOKUP_ERROR))
                        }
                        else -> createTripErrorObservable.onNext(ApiError(response.firstError.errorCode))
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