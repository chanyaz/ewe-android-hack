package com.expedia.vm.packages

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.vm.BaseCreateTripViewModel
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import rx.Observer
import rx.subjects.BehaviorSubject

class PackageCreateTripViewModel(var packageServices: PackageServices, val context: Context) : BaseCreateTripViewModel() {

    val tripParams = BehaviorSubject.create<PackageCreateTripParams>()

    init {
        tripParams.subscribe { params ->
            //When changing room, packageHotelOffers uses the old piid, with default associated flights
            //We need to update this to use the selected flights piid
            val hotel = Db.getPackageSelectedHotel()
            hotel.packageOfferModel.piid = params.productKey
        }

        performCreateTrip.subscribe {
            showCreateTripDialogObservable.onNext(true)
            packageServices.createTrip(tripParams.value).subscribe(makeCreateTripResponseObserver())
        }
    }

    fun makeCreateTripResponseObserver(): Observer<PackageCreateTripResponse> {
        return object : Observer<PackageCreateTripResponse> {
            override fun onNext(response: PackageCreateTripResponse) {
                showCreateTripDialogObservable.onNext(false)
                if (response.hasErrors() && !response.hasPriceChange()) {
                    when (response.firstError.errorCode) {
                        ApiError.Code.UNKNOWN_ERROR -> createTripErrorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                        ApiError.Code.PACKAGE_DATE_MISMATCH_ERROR -> createTripErrorObservable.onNext(ApiError(ApiError.Code.PACKAGE_DATE_MISMATCH_ERROR))
                        else -> {
                            //This should be handled
                        }
                    }
                } else {
                    Db.getTripBucket().clearPackages()
                    Db.getTripBucket().add(TripBucketItemPackages(response))
                    createTripResponseObservable.onNext(response)

                    //set the hotel check in, check out dates on checkout overview from create trip response
                    val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
                    bundleDatesObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                            .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(response.packageDetails.hotel.checkInDate)))
                            .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(response.packageDetails.hotel.checkOutDate)))
                            .put("guests", StrUtils.formatGuestString(context, Db.getPackageParams().guests))
                            .format()
                            .toString())
                }
            }

            override fun onError(e: Throwable) {
                showCreateTripDialogObservable.onNext(false)
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        performCreateTrip.onNext(Unit)
                    }
                    val cancelFun = fun() {
                        val activity = context as AppCompatActivity
                        activity.onBackPressed()
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }
}