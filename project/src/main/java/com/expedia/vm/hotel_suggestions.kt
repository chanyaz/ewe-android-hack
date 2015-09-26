package com.expedia.vm

import android.content.Context
import android.location.Location
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.util.endlessObserver
import com.mobiata.android.Log
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

class HotelSuggestionAdapterViewModel(val context: Context, val suggestionsService: SuggestionV4Services, val locationObservable: Observable<Location>) {

    // Outputs
    val suggestionsObservable = PublishSubject.create<List<SuggestionV4>>()
    var suggestions: List<SuggestionV4> = emptyList()
    var nearby: List<SuggestionV4> = emptyList()

    init {
        locationObservable.subscribe(generateLocationServiceCallback());

        suggestionsObservable.subscribe {
            suggestions = it
        }
    }

    // Inputs
    val queryObserver = endlessObserver<String> { query ->
        if (query.isNotBlank() && query.length() >= 3) {
            suggestionsService.getHotelSuggestionsV4(query, generateSuggestionServiceCallback())
        } else {
            suggestionsObservable.onNext(suggestionsListWithNearby())
        }
    }

    private fun suggestionsListWithDummyAutofillItem(): MutableList<SuggestionV4> {
        var suggestionsWithDummyAutofillItem: MutableList<SuggestionV4> = ArrayList()
        suggestionsWithDummyAutofillItem.add(SuggestionV4())
        return suggestionsWithDummyAutofillItem
    }

    private fun suggestionsListWithNearby(): MutableList<SuggestionV4> {
        var suggestionsWithDummyAutofillItem = suggestionsListWithDummyAutofillItem()
        suggestionsWithDummyAutofillItem.addAll(nearby)
        suggestionsWithDummyAutofillItem.addAll(loadRecentSuggestions())
        return suggestionsWithDummyAutofillItem
    }

    private fun loadRecentSuggestions(): MutableList<SuggestionV4> {
        return SuggestionV4Utils.loadSuggestionHistory(context, SuggestionV4Utils.RECENT_HOTEL_SUGGESTIONS_FILE)
    }

    private fun getNearbySuggestions(latlong: String) {
        suggestionsService
                .suggestNearbyV1(com.expedia.bookings.data.pos.PointOfSale.getSuggestLocaleIdentifier(), latlong, com.expedia.bookings.data.pos.PointOfSale.getPointOfSale().getSiteId())
                .doOnNext { nearbySuggestions -> if (nearbySuggestions.size() < 1) throw com.expedia.bookings.data.cars.ApiError(com.expedia.bookings.data.cars.ApiError.Code.SUGGESTIONS_NO_RESULTS) }
                .doOnNext { nearbySuggestions -> nearbySuggestions.first().regionNames.displayName = context.getString(com.expedia.bookings.R.string.current_location) }
                .doOnNext { nearby = it }
                .subscribe { suggestionsObservable.onNext(suggestionsListWithNearby()) }
    }

    // Utility
    private fun generateLocationServiceCallback(): Observer<Location> {
        return object : Observer<Location> {
            override fun onNext(location: Location) {
                val latlong = "" + location.latitude + "|" + location.longitude;
                getNearbySuggestions(latlong)
            }

            override fun onCompleted() {
                // ignore
            }

            override fun onError(e: Throwable?) {
                Log.e("Current Location Error", e)
            }
        }
    }

    private fun generateSuggestionServiceCallback(): Observer<List<SuggestionV4>> {
        return object : Observer<List<SuggestionV4>> {
            override fun onNext(suggestions: List<SuggestionV4>) {
                val suggestionsWithDummyAutofillItem: MutableList<SuggestionV4> = suggestionsListWithDummyAutofillItem()
                suggestionsWithDummyAutofillItem.addAll(suggestions)
                suggestionsObservable.onNext(suggestionsWithDummyAutofillItem)
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