package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.util.endlessObserver
import rx.subjects.PublishSubject

class RailInboundResultsViewModel(context: Context) : BaseRailResultsViewModel(context) {
    val legOptionListSubject =  PublishSubject.create<List<RailLegOption>>()

    val resultsReturnedObserver = endlessObserver<RailSearchResponse> { searchResponse ->
        legOptionListSubject.onNext(searchResponse.legList[1].legOptionList)
    }

    init {
        directionHeaderSubject.onNext(context.getString(R.string.select_return))
    }
}