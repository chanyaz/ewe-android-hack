package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.SuggestionStrUtils
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class LXSuggestionViewModel(val context: Context) {

    // Outputs
    val titleObservable = BehaviorSubject.create<String>()
    val cityNameObservable = BehaviorSubject.create<String>()
    val iconObservable = BehaviorSubject.create<Int>()
    val cityNameVisibility = BehaviorSubject.create<Boolean>()
    val suggestionSelected = PublishSubject.create<SearchSuggestion>()

    // Inputs
    val suggestionObserver = BehaviorSubject.create<SuggestionV4>()

    init {
        suggestionObserver.subscribe { suggestion ->
            cityNameObservable.onNext(SuggestionStrUtils.formatAirportName(suggestion.regionNames.shortName))
            titleObservable.onNext(HtmlCompat.stripHtml(SuggestionStrUtils.formatCityName(suggestion.regionNames.displayName)))
            if (suggestion.iconType == SuggestionV4.IconType.HISTORY_ICON) {
                cityNameVisibility.onNext(true)
                iconObservable.onNext(R.drawable.recents)
            } else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON){
                //TODO
                iconObservable.onNext(R.drawable.ic_suggest_current_location)
                cityNameVisibility.onNext(false)
            } else {
                cityNameVisibility.onNext(true)
                iconObservable.onNext(R.drawable.search_type_icon)
            }
        }
    }
}
