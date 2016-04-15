package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.services.RailServices
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.squareup.phrase.Phrase
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailResultsViewModel(val context: Context, private val railServices: RailServices?) {
    // Inputs
    val paramsSubject = BehaviorSubject.create<RailSearchRequest>()

    // Outputs
    val railResultsObservable = PublishSubject.create<RailSearchResponse>()
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = BehaviorSubject.create<CharSequence>()

    var searchViewModel: RailSearchViewModel by notNullAndObservable {
        val title = "${it.railOriginObservable.value.regionNames.shortName} - ${it.railDestinationObservable.value.regionNames.shortName}"
        titleSubject.onNext(title)
    }

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            doSearch(params)
        })

        paramsSubject.subscribe {
            val travelerPart = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, paramsSubject.value.guests, paramsSubject.value.guests)
            val subtitle = Phrase.from(context, R.string.rail_results_toolbar_subtitle_TEMPLATE)
                    .put("searchdates", searchViewModel.dateTextObservable.value)
                    .put("travelerspart", travelerPart).format().toString()
            subtitleSubject.onNext(subtitle)
        }
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