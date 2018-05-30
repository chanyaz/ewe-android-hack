package com.expedia.bookings.packages.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse
import com.expedia.bookings.data.packages.MultiItemCreateTripParams
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.vm.BaseCreateTripViewModel
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException

class PackageCreateTripViewModel(var packageServices: PackageServices, val context: Context) : BaseCreateTripViewModel() {

    val performMultiItemCreateTripSubject = PublishSubject.create<Unit>()
    val cancelMultiItemCreateTripSubject = PublishSubject.create<Unit>()
    val multiItemResponseSubject = PublishSubject.create<MultiItemApiCreateTripResponse>()
    val midCreateTripErrorObservable = PublishSubject.create<String>()

    private var multiItemCreateTripDisposable: Disposable? = null

    init {

        performCreateTrip.subscribe {
            showCreateTripDialogObservable.onNext(true)
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
                } else {
                    createTripErrorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                }
            }

            override fun onNext(response: MultiItemApiCreateTripResponse) {
                val errorKey = response.errors?.firstOrNull()?.key
                if (errorKey != null) {
                    midCreateTripErrorObservable.onNext(errorKey)
                } else {
                    showCreateTripDialogObservable.onNext(false)
                    multiItemResponseSubject.onNext(response)
                }
            }

            override fun onComplete() {
            }
        }
    }
}
