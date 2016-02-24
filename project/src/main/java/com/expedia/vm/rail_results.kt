package com.expedia.vm

import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.services.RailServices
import com.expedia.util.endlessObserver
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public class RailResultsViewModel(private val railServices: RailServices?) {
    // Inputs
    val paramsSubject = BehaviorSubject.create<RailSearchRequest>()

    // Outputs
    val railResultsObservable = PublishSubject.create<RailSearchResponse>()

    init {
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
