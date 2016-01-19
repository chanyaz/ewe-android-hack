package com.expedia.vm

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemPackages
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.services.PackageServices
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public class PackageCreateTripViewModel(val packageServices: PackageServices) {

    val tripParams = PublishSubject.create<PackageCreateTripParams>()
    val tripResponseObservable = BehaviorSubject.create<PackageCreateTripResponse>()

    init {
        tripParams.subscribe { params ->
            packageServices.createTrip(params).subscribe(makeCreateTripResponseObserver())
        }
    }

    fun makeCreateTripResponseObserver(): Observer<PackageCreateTripResponse> {
        return object : Observer<PackageCreateTripResponse> {
            override fun onNext(response: PackageCreateTripResponse) {
                if (response.hasErrors()) {
                    //TODO handle errors (unhappy path story)
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
