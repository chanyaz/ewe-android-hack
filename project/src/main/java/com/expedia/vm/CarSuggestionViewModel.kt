package com.expedia.vm

import android.content.Context
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.utils.SuggestionStrUtils
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class CarSuggestionViewModel(val context: Context) {

    // Outputs
    val titleObservable = BehaviorSubject.create<String>()
    val cityNameObservable = BehaviorSubject.create<String>()
    val iconObservable = BehaviorSubject.create<Int>()
    val cityNameVisibility = BehaviorSubject.create<Boolean>()
    val suggestionSelected = PublishSubject.create<SuggestionV4>()

    // Inputs
    val suggestionObserver = BehaviorSubject.create<SuggestionV4>()

    init {
        suggestionObserver.subscribe { suggestion ->

            if (SuggestionV4.CURRENT_LOCATION_ID.equals(suggestion.regionNames.displayName, ignoreCase = true)) {
                titleObservable.onNext(context.getString(R.string.current_location))
                cityNameVisibility.onNext(false)
            } else if (suggestion.isMajorAirport) {
                titleObservable.onNext(Html.fromHtml(SuggestionStrUtils.formatCityName(suggestion.regionNames.displayName)).toString())
                cityNameObservable.onNext(SuggestionStrUtils.formatAirportName(suggestion.regionNames.shortName))
                cityNameVisibility.onNext(true)
            } else {
                titleObservable.onNext(Html.fromHtml(SuggestionStrUtils.formatCityName(suggestion.regionNames.displayName)).toString())
                cityNameObservable.onNext(suggestion.regionNames.shortName)
                cityNameVisibility.onNext(true)
            }

            iconObservable.onNext(
                    if (suggestion.iconType == SuggestionV4.IconType.HISTORY_ICON) {
                        R.drawable.recents
                    } else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
                        R.drawable.ic_suggest_current_location
                    } else {
                        if (suggestion.isMajorAirport) R.drawable.ic_suggest_airport else R.drawable.search_type_icon
                    })
        }
    }
}