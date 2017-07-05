package com.expedia.vm.packages

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.mobiata.android.Log
import rx.Observer
import rx.Subscription
import rx.subjects.PublishSubject

class PackageFlightContainerViewModel(private val context: Context, private val packageServices: PackageServices) {

    private var subscription: Subscription? = null

    val performFlightSearch = PublishSubject.create<PackageSearchType>()
    val flightSearchResponseObservable = PublishSubject.create<PackageSearchResponse>()

    init {
        performFlightSearch.subscribe { type ->
            subscription = makeFlightSearchCall(type)
        }
    }

    private fun makeFlightSearchCall(type: PackageSearchType): Subscription? {
        return packageServices.packageSearch(Db.getPackageParams()).subscribe(object : Observer<PackageSearchResponse> {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                Log.i("package error: " + e?.message)
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        performFlightSearch.onNext(type)
                    }
                    val cancelFun = fun() {
                        (context as Activity).finish()
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
            }

            override fun onNext(response: PackageSearchResponse) {
                if (response.hasErrors()) {
                    onResponseError(response.firstError)
                } else if (response.getHotels().isEmpty()) {
                    onResponseError(PackageApiError.Code.search_response_null)
                } else {
                    Db.setPackageResponse(response)
                    if (type == PackageSearchType.OUTBOUND_FLIGHT) {
                        PackageResponseUtils.savePackageResponse(context, response, PackageResponseUtils.RECENT_PACKAGE_OUTBOUND_FLIGHT_FILE)
                    } else {
                        PackageResponseUtils.savePackageResponse(context, response, PackageResponseUtils.RECENT_PACKAGE_INBOUND_FLIGHT_FILE)
                    }
                    flightSearchResponseObservable.onNext(response)
                }
            }

        })
    }

    private fun onResponseError(code: PackageApiError.Code?) {
        val intent = Intent()
        intent.putExtra(Constants.PACKAGE_API_ERROR, code)
        (context as android.app.Activity).setResult(Constants.PACKAGE_API_ERROR_RESULT_CODE, intent)
        context.finish()
    }

    fun back() {
        subscription?.unsubscribe()
    }
}
