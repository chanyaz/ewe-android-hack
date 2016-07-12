package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.vm.BaseCreateTripViewModel
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import rx.Observable
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.PublishSubject

class PackageCreateTripViewModel(val packageServices: PackageServices, val context: Context) : BaseCreateTripViewModel() {

    val tripParams = PublishSubject.create<PackageCreateTripParams>()

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

                    //set the hotel check in, check out dates on checkout overview from create trip response
                    val dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
                    bundleDatesObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                            .put("startdate", DateUtils.localDateToMMMd(dtf.parseLocalDate(response.packageDetails.hotel.checkInDate)))
                            .put("enddate", DateUtils.localDateToMMMd(dtf.parseLocalDate(response.packageDetails.hotel.checkOutDate)))
                            .put("guests", StrUtils.formatGuestString(context, Db.getPackageParams().guests))
                            .format()
                            .toString())
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