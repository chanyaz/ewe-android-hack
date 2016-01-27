package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.util.endlessObserver
import org.joda.time.LocalDate
import rx.subjects.PublishSubject

class RailSearchViewModel(val context: Context) {
    private val paramsBuilder = RailSearchRequest.Builder()

    // Outputs
    val searchParamsObservable = PublishSubject.create<RailSearchRequest>()

    val searchObserver = endlessObserver<Unit> {
        paramsBuilder.origin(SuggestionV4())
        paramsBuilder.destination(SuggestionV4())
        paramsBuilder.departDate(LocalDate.now()) //TODO bind these to the actual UI

        var searchParams = paramsBuilder.build()
        searchParamsObservable.onNext(searchParams)
    }
}
