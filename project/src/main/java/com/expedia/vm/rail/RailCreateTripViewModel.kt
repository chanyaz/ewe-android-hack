package com.expedia.vm.rail

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.trips.TripBucketItemRails
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.tracking.RailTracking
import com.expedia.bookings.utils.RetrofitUtils
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.BehaviorSubject

class RailCreateTripViewModel(val railServices: RailServices) {
    val offerTokensSelected = PublishSubject.create<List<String>>()

    // outputs
    val tripResponseObservable = BehaviorSubject.create<RailCreateTripResponse>()
    val createTripErrorObservable = PublishSubject.create<ApiError>()
    val createTripCallTriggeredObservable = PublishSubject.create<Unit>()
    val showNoInternetRetryDialog = PublishSubject.create<Unit>()
    val retryObservable = PublishSubject.create<Unit>()
    val priceChangeObservable = PublishSubject.create<RailCreateTripResponse>()
    var createTripTokens: List<String> = emptyList()

    init {
        offerTokensSelected.subscribe { offerTokens ->
            createTripTokens = offerTokens
            railServices.railCreateTrip(offerTokens, makeCreateTripResponseObserver())
            createTripCallTriggeredObservable.onNext(Unit)
        }

        retryObservable.subscribe {
            offerTokensSelected.onNext(createTripTokens)
        }
    }

    fun makeCreateTripResponseObserver(): Observer<RailCreateTripResponse> {
        return object : DisposableObserver<RailCreateTripResponse>() {
            override fun onNext(response: RailCreateTripResponse) {
                if (response.isErrorResponse && !response.hasPriceChange()) {
                    RailTracking().trackCreateTripUnknownError()
                    createTripErrorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                } else {
                    if (response.hasPriceChange()) {
                        priceChangeObservable.onNext(response)
                    } else {
                        Db.getTripBucket().clearRails()
                        Db.getTripBucket().add(TripBucketItemRails(response))
                        tripResponseObservable.onNext(response)
                    }
                }
            }

            override fun onError(e: Throwable) {
                if (RetrofitUtils.isNetworkError(e)) {
                    showNoInternetRetryDialog.onNext(Unit)
                } else {
                    createTripErrorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                }
                RailTracking().trackCreateTripApiNoResponseError()
            }

            override fun onComplete() {
            }
        }
    }
}