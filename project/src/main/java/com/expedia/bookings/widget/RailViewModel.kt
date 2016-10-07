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
    val priceObservable = legOptionObservable.map { legOption -> legOption.bestPrice.formattedPrice }
    val formattedStopsAndDurationObservable = legOptionObservable.map { legOption ->
        Phrase.from(context, R.string.rail_time_and_stops_line_TEMPLATE)
                .put("formattedduration", DateTimeUtils.formatDuration(context.resources, legOption.durationMinutes()))
                .put("formattedchangecount", formatChangesText(context, legOption.noOfChanges)).format().toString()
    }
    val railCardAppliedObservable = legOptionObservable.map { legOption -> legOption.doesAnyOfferHasFareQualifier }

    companion object {
        @JvmStatic fun formatChangesText(context: Context, changesCount: Int): String {
            if (changesCount == 0 ) {
                return context.getString(R.string.rail_direct)
            }
            return context.resources.getQuantityString(R.plurals.rail_changes_TEMPLATE, changesCount, changesCount)
        }
    }
}