package com.expedia.vm.packages

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemPackages
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.services.PackageServices
import rx.Observable
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class PackageCreateTripViewModel(val packageServices: PackageServices) {

    val tripParams = PublishSubject.create<PackageCreateTripParams>()
    val performCreateTrip = PublishSubject.create<Unit>()
    val tripResponseObservable = BehaviorSubject.create<PackageCreateTripResponse>()
    val showCreateTripDialogObservable = PublishSubject.create<Boolean>()
    val createTripErrorObservable = PublishSubject.create<ApiError>()

    init {
        Observable.combineLatest(tripParams, performCreateTrip, { params, createTrip ->
            showCreateTripDialogObservable.onNext(true)
            packageServices.createTrip(params).subscribe(makeCreateTripResponseObserver())
        }).subscribe()
    }

    fun makeCreateTripResponseObserver(): Observer<PackageCreateTripResponse> {
        return object : Observer<PackageCreateTripResponse> {
            override fun onNext(response: PackageCreateTripResponse) {
                showCreateTripDialogObservable.onNext(false)
                if (response.hasErrors() && !response.hasPriceChange()) {
                    if (response.firstError.errorCode == ApiError.Code.UNKNOWN_ERROR) {
                        createTripErrorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                    }
                } else {
                    Db.getTripBucket().clearPackages()
                    Db.getTripBucket().add(TripBucketItemPackages(response))
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