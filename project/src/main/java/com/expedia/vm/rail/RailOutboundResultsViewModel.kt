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
import com.expedia.bookings.tracking.RailTracking
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.withLatestFrom
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class RailOutboundResultsViewModel(val context: Context, val railServices: RailServices) : BaseRailResultsViewModel(context) {
    val railResultsObservable = BehaviorSubject.create<RailSearchResponse>()
    val errorObservable = PublishSubject.create<ApiError>()
    val retryObservable = PublishSubject.create<Unit>()

    val showChildrenWarningObservable = railResultsObservable.map { response ->
        response.hasChildrenAreFreeWarning()
    }

    val legalBannerMessageObservable = paramsSubject.map { params -> getLegalBannerMessage(params.isRoundTripSearch()) }

    init {
        directionHeaderSubject.onNext(context.getString(R.string.select_outbound))
        paramsSubject.subscribe { params ->
            doSearch(params)
        }
        retryObservable.withLatestFrom(paramsSubject, { retry, params ->
            doSearch(params)
        }).subscribe()

        railResultsObservable.map { response ->
            val leg = response.outboundLeg!!
            Pair(leg.legOptionList, leg.cheapestInboundPrice)
        }.subscribe(legOptionsAndCheapestPriceSubject)

        railResultsObservable.withLatestFrom(paramsSubject, { railSearchResponse, searchRequest ->
            val outboundLeg = railSearchResponse.outboundLeg
            if (outboundLeg!!.legOptionList.size > 0) {
                if (searchRequest.isRoundTripSearch()) {
                    RailTracking().trackRailRoundTripOutbound(outboundLeg, searchRequest)
                } else {
                    RailTracking().trackRailOneWaySearch(outboundLeg, searchRequest)
                }
            }
        }).subscribe()
    }

    private fun doSearch(params: RailSearchRequest) {
        railServices.railSearch(RailApiSearchModel.fromSearchParams(params), object : DisposableObserver<RailSearchResponse>() {
            override fun onNext(response: RailSearchResponse) {
                if (response.hasError()) {
                    if (response.responseStatus.statusCategory == RailsApiStatusCodes.STATUS_CATEGORY_NO_PRODUCT) {
                        errorObservable.onNext(ApiError(ApiError.Code.RAIL_SEARCH_NO_RESULTS))
                        RailTracking().trackRailSearchNoResults()
                    } else {
                        errorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                        RailTracking().trackSearchUnknownError()
                    }
                } else {
                    railResultsObservable.onNext(response)
                }
            }

            override fun onComplete() {

            }

            override fun onError(e: Throwable) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        retryObservable.onNext(Unit)
                    }
                    val cancelFun = fun() {
                        noNetworkObservable.onNext(Unit)
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
                RailTracking().trackSearchApiNoResponseError()
            }
        })
    }

    private fun getLegalBannerMessage(isRoundTrip: Boolean): String {
        return if (isRoundTrip) context.getString(R.string.rail_search_legal_banner_round_trip) else context.getString(R.string.rail_search_legal_banner_one_way)
    }
}