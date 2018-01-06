package com.expedia.vm.packages

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse
import com.expedia.bookings.data.packages.MultiItemCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.Optional
import com.expedia.vm.BaseCreateTripViewModel
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class PackageCreateTripViewModel(var packageServices: PackageServices, val context: Context) : BaseCreateTripViewModel() {

    val tripParams = BehaviorSubject.create<PackageCreateTripParams>()
    val performMultiItemCreateTripSubject = PublishSubject.create<Unit>()
    val multiItemResponseSubject = PublishSubject.create<MultiItemApiCreateTripResponse>()

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

        performMultiItemCreateTripSubject.subscribe {
            val params = MultiItemCreateTripParams.fromPackageSearchParams(Db.sharedInstance.packageParams)
            packageServices.multiItemCreateTrip(params).subscribe(makeMultiItemCreateTripResponseObserver())
        }
    }

    private fun makeMultiItemCreateTripResponseObserver(): Observer<MultiItemApiCreateTripResponse> {
        return object : Observer<MultiItemApiCreateTripResponse> {
            override fun onError(e: Throwable) {
                showCreateTripDialogObservable.onNext(false)
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        performMultiItemCreateTripSubject.onNext(Unit)
                    }
                    val cancelFun = fun() {
                        val activity = context as AppCompatActivity
                        activity.onBackPressed()
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
            }

            override fun onNext(response: MultiItemApiCreateTripResponse) {
                showCreateTripDialogObservable.onNext(false)
                multiItemResponseSubject.onNext(response)
            }

            override fun onCompleted() {
            }

        }
    }

    fun makeCreateTripResponseObserver(): Observer<PackageCreateTripResponse> {
        return object : Observer<PackageCreateTripResponse> {
            override fun onNext(response: PackageCreateTripResponse) {
                if (!isValidContext(context)) {
                    return
                }
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
                    createTripResponseObservable.onNext(Optional(response))

                    //set the hotel check in, check out dates on checkout overview from create trip response
                    val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
                    bundleDatesObservable.onNext(Phrase.from(context, R.string.start_dash_end_date_range_with_guests_TEMPLATE)
                            .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(response.packageDetails.hotel.checkInDate)))
                            .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(response.packageDetails.hotel.checkOutDate)))
                            .put("guests", StrUtils.formatGuestString(context, Db.sharedInstance.packageParams.guests))
                            .format()
                            .toString())
                }
            }

            override fun onError(e: Throwable) {
                if (!isValidContext(context)) {
                    return
                }
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