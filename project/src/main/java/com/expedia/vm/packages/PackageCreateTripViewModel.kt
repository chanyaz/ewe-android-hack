package com.expedia.vm.packages

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse
import com.expedia.bookings.data.packages.MultiItemCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.util.Optional
import com.expedia.util.PackageUtil
import com.expedia.vm.BaseCreateTripViewModel
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException

class PackageCreateTripViewModel(var packageServices: PackageServices, val context: Context) : BaseCreateTripViewModel() {

    val tripParams = BehaviorSubject.create<PackageCreateTripParams>()
    val performMultiItemCreateTripSubject = PublishSubject.create<Unit>()
    val cancelMultiItemCreateTripSubject = PublishSubject.create<Unit>()
    val multiItemResponseSubject = PublishSubject.create<MultiItemApiCreateTripResponse>()
    val midCreateTripErrorObservable = PublishSubject.create<String>()

    private var multiItemCreateTripDisposable: Disposable? = null

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
            multiItemCreateTripDisposable = packageServices.multiItemCreateTrip(params).subscribeObserver(makeMultiItemCreateTripResponseObserver())
        }

        cancelMultiItemCreateTripSubject.subscribe {
            multiItemCreateTripDisposable?.dispose()
            multiItemCreateTripDisposable = null
        }
    }

    @VisibleForTesting
    fun makeMultiItemCreateTripResponseObserver(): Observer<MultiItemApiCreateTripResponse> {
        return object : DisposableObserver<MultiItemApiCreateTripResponse>() {
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
                } else if (e is HttpException) {
                    createTripErrorObservable.onNext(ApiError(ApiError.Code.MID_COULD_NOT_FIND_RESULTS))
                }
            }

            override fun onNext(response: MultiItemApiCreateTripResponse) {
                if (response.errors != null) {
                    midCreateTripErrorObservable.onNext(response.errors!![0].key)
                } else {
                    showCreateTripDialogObservable.onNext(false)
                    multiItemResponseSubject.onNext(response)
                }
            }
            override fun onComplete() {
            }
        }
    }

    fun makeCreateTripResponseObserver(): Observer<PackageCreateTripResponse> {
        return object : DisposableObserver<PackageCreateTripResponse>() {
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
                    createTripResponseObservable.onNext(Optional(response))
                    //set the hotel check in, check out dates on checkout overview from create trip response
                    bundleDatesObservable.onNext(PackageUtil.getBundleHotelDatesAndGuestsText(context, response.packageDetails.hotel.checkInDate, response.packageDetails.hotel.checkOutDate, Db.sharedInstance.packageParams.guests))
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

            override fun onComplete() {
                // ignore
            }
        }
    }
}
