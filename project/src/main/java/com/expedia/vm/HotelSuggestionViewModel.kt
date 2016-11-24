package com.expedia.vm

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.text.HtmlCompat
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class HotelSuggestionViewModel() {

    // Outputs
    val titleObservable = BehaviorSubject.create<String>()
    val isChildObservable = BehaviorSubject.create<Boolean>()
    val iconObservable = BehaviorSubject.create<Int>()
    val suggestionSelected = PublishSubject.create<SuggestionV4>()

    // Inputs
    val suggestionObserver = BehaviorSubject.create<SuggestionV4>()

    init {
        suggestionObserver.subscribe { suggestion ->

            titleObservable.onNext(HtmlCompat.stripHtml(suggestion.regionNames.displayName))

            isChildObservable.onNext(suggestion.hierarchyInfo?.isChild ?: false)

            iconObservable.onNext(
                    if (suggestion.iconType == SuggestionV4.IconType.HISTORY_ICON) {
                        R.drawable.recents
                    } else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
                        R.drawable.ic_suggest_current_location
                    } else if (suggestion.iconType == SuggestionV4.IconType.MAGNIFYING_GLASS_ICON) {
                        R.drawable.google_search
                    } else if (suggestion.type == "HOTEL") {
                        R.drawable.hotel_suggest
                    } else if (suggestion.type == "AIRPORT") {
                        R.drawable.airport_suggest
                    } else {
                        R.drawable.search_type_icon
                    }
            )
        }
    }
}
