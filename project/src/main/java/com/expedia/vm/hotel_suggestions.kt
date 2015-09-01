package com.expedia.vm

import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.mobiata.android.Log
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class HotelSuggestionAdapterViewModel(val suggestionsService: SuggestionV4Services) {

    // Outputs
    val suggestionsObservable = PublishSubject.create<List<SuggestionV4>>()
    var suggestions: List<SuggestionV4> = emptyList()

    init {
        suggestionsObservable.subscribe {
            suggestions = it
        }
    }

    // Inputs
    val queryObserver = endlessObserver<String> { query ->
        if (query.isNotBlank() && query.length() >= 3) {
            suggestionsService.getHotelSuggestionsV4(query, generateSuggestionServiceCallback())
        } else {
            suggestionsObservable.onNext(emptyList())
        }
    }

    // Utility
    private fun generateSuggestionServiceCallback(): Observer<List<SuggestionV4>> {
        return object : Observer<List<SuggestionV4>> {
            override fun onNext(suggestions: List<SuggestionV4>) {
                suggestionsObservable.onNext(suggestions)
            }

            override fun onCompleted() {
                // ignore
            }

            override fun onError(e: Throwable?) {
                Log.e("Hotel Suggestions Error", e)
            }
        }
    }
}

public class HotelSuggestionViewModel() {

    // Outputs
    val titleObservable = BehaviorSubject.create<String>()
    val isChildObservable = BehaviorSubject.create<Boolean>()
    val iconObservable = BehaviorSubject.create<Int>()

    // Inputs
    val suggestionObserver: Observer<SuggestionV4> = endlessObserver { suggestion ->
        titleObservable.onNext(Html.fromHtml(StrUtils.formatCityName(suggestion.regionNames.displayName)).toString())

        isChildObservable.onNext(suggestion.hierarchyInfo.isChild)

        iconObservable.onNext(
                if (suggestion.iconType == SuggestionV4.IconType.HISTORY_ICON) {
                    R.drawable.recents
                } else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
                    R.drawable.ic_suggest_current_location
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