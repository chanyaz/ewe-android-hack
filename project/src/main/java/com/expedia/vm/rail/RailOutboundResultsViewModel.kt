package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.data.rail.responses.RailsApiStatusCodes
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.util.endlessObserver
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailOutboundResultsViewModel(val context: Context, val railServices: RailServices) : BaseRailResultsViewModel(context) {
    val railResultsObservable = BehaviorSubject.create<RailSearchResponse>()
    val errorObservable = PublishSubject.create<ApiError>()
    val retryObservable = PublishSubject.create<Unit>()

    val showChildrenWarningObservable = railResultsObservable.map {response ->
        response.hasChildrenAreFreeWarning()
    }

    init {
        directionHeaderSubject.onNext(context.getString(R.string.select_outbound))
        paramsSubject.subscribe { params ->
            doSearch(params)
        }
        retryObservable.withLatestFrom(paramsSubject, { retry, params ->
            doSearch(params)
        }).subscribe()
    }

    private fun doSearch(params: RailSearchRequest) {
        railServices?.railSearch(RailApiSearchModel.fromSearchParams(params), object : Observer<RailSearchResponse> {
            override fun onNext(response: RailSearchResponse) {
                if (response.hasError()) {
                    if (response.responseStatus.statusCategory == RailsApiStatusCodes.STATUS_CATEGORY_NO_PRODUCT) {
                        errorObservable.onNext(ApiError(ApiError.Code.RAIL_SEARCH_NO_RESULTS))
                    }
                    else {
                        errorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                    }
                }
                railResultsObservable.onNext(response)
            }

            override fun onCompleted() {

            }

            override fun onError(e: Throwable?) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        retryObservable.onNext(Unit)
                    }
                    val cancelFun = fun() {
                        noNetworkObservable.onNext(Unit)
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
            }
        })
    }
}