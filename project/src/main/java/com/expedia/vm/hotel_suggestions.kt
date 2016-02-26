package com.expedia.vm

import android.content.Context
import android.location.Location
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.SuggestionV4Utils
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class HotelSuggestionAdapterViewModel(context: Context, suggestionsService: SuggestionV4Services, locationObservable: Observable<Location>?, shouldShowCurrentLocation: Boolean, rawQueryEnabled: Boolean) : SuggestionAdapterViewModel(context, suggestionsService, locationObservable, shouldShowCurrentLocation, rawQueryEnabled) {
    override fun getSuggestionService(query: String) {
        suggestionsService.getHotelSuggestionsV4(query, ServicesUtil.generateClientId(context), generateSuggestionServiceCallback(), PointOfSale.getSuggestLocaleIdentifier())
    }

    override fun getSuggestionHistoryFile(): String {
        return SuggestionV4Utils.RECENT_HOTEL_SUGGESTIONS_FILE
    }
}

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

            titleObservable.onNext(Html.fromHtml(suggestion.regionNames.displayName).toString())

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
