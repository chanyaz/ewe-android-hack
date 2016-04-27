package com.expedia.vm.packages

import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class SuggestionViewModel() {

    // Outputs
    val titleObservable = BehaviorSubject.create<String>()
    val isChildObservable = BehaviorSubject.create<Boolean>()
    val iconObservable = BehaviorSubject.create<Int>()
    val suggestionSelected = PublishSubject.create<SuggestionV4>()

    // Inputs
    val suggestionObserver = BehaviorSubject.create<SuggestionV4>()

    init {
        suggestionObserver.subscribe { suggestion ->

            titleObservable.onNext(Html.fromHtml(suggestion.regionNames.displayName).toString())

            isChildObservable.onNext(suggestion.hierarchyInfo?.isChild ?: false)

            iconObservable.onNext(
                    if (suggestion.iconType == SuggestionV4.IconType.HISTORY_ICON) {
                        R.drawable.recents
                    } else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
                        R.drawable.ic_suggest_current_location
                    } else {
                        R.drawable.airport_suggest
                    }
            )
        }
    }
}
