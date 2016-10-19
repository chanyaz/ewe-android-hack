package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.services.RailServices
import com.expedia.util.endlessObserver
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject

class RailOutboundResultsViewModel(context: Context, val railServices: RailServices) : BaseRailResultsViewModel(context) {
    val railResultsObservable = BehaviorSubject.create<RailSearchResponse>()

    init {
        directionHeaderSubject.onNext(context.getString(R.string.select_outbound))
        paramsSubject.subscribe(endlessObserver { params ->
            doSearch(params)
        })
    }

    private fun doSearch(params: RailSearchRequest) {
        railServices?.railSearch(RailApiSearchModel.fromSearchParams(params), object : Observer<RailSearchResponse> {
            override fun onNext(it: RailSearchResponse) {
                railResultsObservable.onNext(it) //TODO - error handing, list view
                // response may be 200, but error on body, needs research
            }

            override fun onCompleted() {

            }

            override fun onError(e: Throwable?) {
                throw OnErrorNotImplementedException(e)
            }
        })
    }
}