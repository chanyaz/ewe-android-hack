package com.expedia.vm.packages

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.SuggestionStrUtils
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class SuggestionViewModel(isCustomerSelectingOrigin: Boolean) {

    // Outputs
    val titleObservable = BehaviorSubject.create<String>()
    val subtitleObservable = BehaviorSubject.create<String>()
    val isChildObservable = BehaviorSubject.create<Boolean>()
    val iconObservable = BehaviorSubject.create<Int>()
    val suggestionSelected = PublishSubject.create<SuggestionV4>()

    // Inputs
    val suggestionObserver = BehaviorSubject.create<SuggestionV4>()

    init {
        suggestionObserver.subscribe { suggestion ->

            if (isCustomerSelectingOrigin) {
                titleObservable.onNext(
                        HtmlCompat.stripHtml(SuggestionStrUtils.formatAirportName(suggestion.regionNames.displayName)))
                subtitleObservable.onNext(
                        StrUtils.formatCityStateName(HtmlCompat.stripHtml(suggestion.regionNames.displayName)))
            } else {
                titleObservable.onNext(HtmlCompat.stripHtml(suggestion.regionNames.displayName))
                subtitleObservable.onNext("")
            }

            isChildObservable.onNext(suggestion.hierarchyInfo?.isChild ?: false)

            iconObservable.onNext(
                    if (suggestion.iconType == SuggestionV4.IconType.HISTORY_ICON) {
                        R.drawable.recents
                    } else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
                        R.drawable.ic_suggest_current_location
                    } else if (isCustomerSelectingOrigin) {
                        R.drawable.airport_suggest
                    } else {
                        R.drawable.search_type_icon
                    }
            )
        }
    }
}