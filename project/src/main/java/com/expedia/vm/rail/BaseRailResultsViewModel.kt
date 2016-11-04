package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.utils.DateFormatUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

open class BaseRailResultsViewModel(context: Context) {
    //input
    val paramsSubject = PublishSubject.create<RailSearchRequest>()

    //outputs
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = BehaviorSubject.create<CharSequence>()
    val directionHeaderSubject = BehaviorSubject.create<CharSequence>()
    val priceHeaderSubject = BehaviorSubject.create<CharSequence>()

    init {
        paramsSubject.subscribe { params ->
            val title = "${params.origin?.regionNames?.shortName} - ${params.destination?.regionNames?.shortName}"
            titleSubject.onNext(title)

            val travelerPart = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, params.guests,
                    params.guests)
            val dateString = DateFormatUtils.formatRailDateRange(context, params.startDate, params.endDate)
            val subtitle = Phrase.from(context, R.string.rail_results_toolbar_subtitle_TEMPLATE)
                    .put("searchdates", dateString)
                    .put("travelerspart", travelerPart).format().toString()
            subtitleSubject.onNext(subtitle)

            val priceHeader = if (params.returnDate != null) context.getString(R.string.total_from) else context.getString(R.string.one_way_from)
            priceHeaderSubject.onNext(priceHeader)
        }
    }
}