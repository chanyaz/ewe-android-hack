package com.expedia.bookings.widget

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailViewModel(val context: Context) {

    //Inputs
    val legOptionObservable = PublishSubject.create<RailLegOption>()

    //Outputs
    val priceObservable = BehaviorSubject.create<String>()
    val operatorObservable = BehaviorSubject.create<String>()
    val formattedStopsAndDurationObservable = BehaviorSubject.create<String>()

    init {
        legOptionObservable.subscribe {
            val formattedStopsAndDuration = Phrase.from(context, R.string.rail_time_and_stops_line_TEMPLATE)
                    .put("formattedduration", DateTimeUtils.formatDuration(context.resources, it.durationMinutes()))
                    .put("formattedchangecount", formatChangesText(context, it.noOfChanges)).format().toString()

            formattedStopsAndDurationObservable.onNext(formattedStopsAndDuration)
            priceObservable.onNext(it.bestPrice.formattedPrice)
            operatorObservable.onNext(it.allOperators())
        }
    }

    companion object {
        @JvmStatic fun formatChangesText(context: Context, changesCount: Int): String {
            if (changesCount == 0 ) {
                return context.getString(R.string.rail_direct)
            }
            return context.resources.getQuantityString(R.plurals.rail_changes_TEMPLATE, changesCount, changesCount)
        }
    }
}