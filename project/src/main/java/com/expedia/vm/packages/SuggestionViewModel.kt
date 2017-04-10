package com.expedia.vm.packages

import com.expedia.bookings.R
import com.expedia.bookings.data.SearchSuggestion
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
    val suggestionSelected = PublishSubject.create<SearchSuggestion>()

    // Inputs
    val suggestionObserver = BehaviorSubject.create<SuggestionV4>()

    init {
        suggestionObserver.subscribe { suggestion ->

            val isChild = suggestion.hierarchyInfo?.isChild ?: false
            val isHistory = suggestion.iconType == SuggestionV4.IconType.HISTORY_ICON
            val displayName = HtmlCompat.stripHtml(suggestion.regionNames.displayName)
            val shortName = HtmlCompat.stripHtml(suggestion.regionNames.shortName)

            if (isCustomerSelectingOrigin) {
                val originTitle = SuggestionStrUtils.formatAirportName(if (isChild && isHistory) shortName else displayName)
                titleObservable.onNext(originTitle)
                subtitleObservable.onNext(StrUtils.formatCityStateName(displayName))
            } else {
                titleObservable.onNext(if (isChild && isHistory) SuggestionStrUtils.formatDashWithoutSpace(shortName) else displayName)
                subtitleObservable.onNext("")
            }

            isChildObservable.onNext(isChild && !isHistory)

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