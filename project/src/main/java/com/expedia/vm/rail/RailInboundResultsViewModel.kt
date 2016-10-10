package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.util.endlessObserver
import rx.subjects.PublishSubject

class RailInboundResultsViewModel(context: Context) : BaseRailResultsViewModel(context) {
    val legSubject =  PublishSubject.create<RailSearchResponse.RailLeg>()

    val resultsReturnedObserver = endlessObserver<RailSearchResponse> { searchResponse ->
        legSubject.onNext(searchResponse.legList[1])
    }

    init {
        directionHeaderSubject.onNext(context.getString(R.string.select_return))
    }
}