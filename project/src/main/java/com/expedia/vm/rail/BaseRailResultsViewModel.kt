package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.rail.util.RailUtils
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

open class BaseRailResultsViewModel(context: Context) {
    //input
    val paramsSubject = PublishSubject.create<RailSearchRequest>()
    val noNetworkObservable = PublishSubject.create<Unit>()

    //outputs
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = BehaviorSubject.create<CharSequence>()
    val directionHeaderSubject = BehaviorSubject.create<CharSequence>()
    val priceHeaderSubject = BehaviorSubject.create<CharSequence>()
    val legOptionsAndCheapestPriceSubject = BehaviorSubject.create<Pair<List<RailLegOption>, Money?>>()

    init {
        paramsSubject.subscribe { params ->
            val title = RailUtils.getToolbarTitleFromSearchRequest(params)
            titleSubject.onNext(title)

            val subtitle = RailUtils.getToolbarSubtitleFromSearchRequest(context, params)
            subtitleSubject.onNext(subtitle)

            val priceHeader = if (params.returnDate != null) context.getString(R.string.total_from) else context.getString(R.string.one_way_from)
            priceHeaderSubject.onNext(priceHeader)
        }
    }
}
